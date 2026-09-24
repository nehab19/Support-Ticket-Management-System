---
inclusion: auto
---

# Testing Guidelines

**Project**: Support Ticket Management System  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Purpose

These guidelines establish comprehensive testing standards for the Support Ticket Management System. All code—whether written by humans or AI—must follow these testing practices.

**Scope**: Backend testing (JUnit 5, jqwik, Spring Boot Test)  
**Authority**: Complements PROJECT_CONSTITUTION.md with testing-specific guidance

---

## Testing Philosophy

### Core Principles

1. **Tests are first-class code** — Maintain tests with the same rigor as production code
2. **Test behavior, not implementation** — Focus on what the system does, not how
3. **Fast feedback loops** — Tests should run quickly and provide clear diagnostics
4. **Comprehensive coverage** — Combine unit, integration, and property-based tests
5. **Fail fast** — Tests should detect problems as early as possible

---

## Testing Pyramid

```
       ┌─────────────┐
       │     E2E     │  Small (expensive, slow, brittle)
       └─────────────┘
      ┌───────────────┐
      │  Integration  │  Medium (moderate cost, moderate speed)
      └───────────────┘
     ┌─────────────────┐
     │ Property-Based  │  Medium (comprehensive, algorithmic)
     └─────────────────┘
    ┌───────────────────┐
    │       Unit        │  Large (cheap, fast, focused)
    └───────────────────┘
```

**Golden Ratio**: 70% unit, 20% property-based, 8% integration, 2% E2E

---

## Unit Testing

### JUnit 5 Standards

**When to Use**: Testing individual components in isolation

**Framework**: JUnit 5 with Mockito for mocking

**Naming Convention**:
```java
// ✅ GOOD: Descriptive test names
@Test
void createTicket_validRequest_returnsTicketWithOpenStatus() { }

@Test
void createTicket_blankTitle_throwsValidationException() { }

// ❌ BAD: Vague test names
@Test
void testCreateTicket() { }

@Test
void test1() { }
```

**Test Structure (Given-When-Then)**:
```java
// ✅ GOOD: Clear test structure
@Test
void updateTicket_partialUpdate_preservesUntouchedFields() {
    // Given: Existing ticket
    Ticket ticket = new Ticket();
    ticket.setTitle("Original Title");
    ticket.setDescription("Original Description");
    ticket.setPriority(Priority.LOW);
    when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
    
    // When: Update only title
    UpdateTicketRequest request = new UpdateTicketRequest(
        "New Title", 
        null,  // Don't update description
        null,  // Don't update priority
        null   // Don't update assignee
    );
    TicketDetailResponse result = ticketService.updateTicket(1L, request);
    
    // Then: Only title changed
    assertThat(result.title()).isEqualTo("New Title");
    assertThat(result.description()).isEqualTo("Original Description");
    assertThat(result.priority()).isEqualTo(Priority.LOW);
}
```

### Mocking Best Practices

**Use Mockito for External Dependencies**:
```java
// ✅ GOOD: Mock external dependencies
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    @Mock
    private TicketRepository ticketRepository;
    
    @Mock
    private TicketStateMachine stateMachine;
    
    @InjectMocks
    private TicketService ticketService;
    
    @Test
    void transitionStatus_validTransition_updatesStatus() {
        // Arrange
        Ticket ticket = createTestTicket();
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(stateMachine.isValidTransition(OPEN, IN_PROGRESS)).thenReturn(true);
        when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // Act
        TicketDetailResponse result = ticketService.transitionStatus(1L, IN_PROGRESS);
        
        // Assert
        assertThat(result.status()).isEqualTo(IN_PROGRESS);
        verify(stateMachine).validateTransition(OPEN, IN_PROGRESS);
        verify(ticketRepository).save(any(Ticket.class));
    }
}
```

**Don't Mock Value Objects or DTOs**:
```java
// ✅ GOOD: Use real value objects
CreateTicketRequest request = new CreateTicketRequest("Title", "Description", Priority.HIGH);

// ❌ BAD: Mocking value objects
CreateTicketRequest mockRequest = mock(CreateTicketRequest.class);
when(mockRequest.title()).thenReturn("Title");
```

---

## Integration Testing

### Spring Boot Test Configuration

**When to Use**: Testing with full Spring context and database

**Annotations**:
```java
// ✅ GOOD: Full integration test setup
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional  // Rollback after each test
class TicketControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void createTicket_validRequest_returns201WithCreatedTicket() throws Exception {
        // Arrange
        CreateTicketRequest request = new CreateTicketRequest(
            "Login issue",
            "Users cannot log in",
            Priority.HIGH
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("Login issue"))
            .andExpect(jsonPath("$.status").value("OPEN"));
    }
}
```

### Database Test Best Practices

**Use H2 for Tests**:
```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=true
```

**Transaction Rollback**:
```java
// ✅ GOOD: Each test gets clean state
@Transactional  // Automatic rollback after test
@Test
void testSomething() {
    // Test runs in transaction, automatically rolled back
}

// ❌ BAD: Manual cleanup
@Test
void testSomething() {
    // Do test
    ticketRepository.deleteAll();  // Manual cleanup is error-prone
}
```

**Test Data Setup**:
```java
// ✅ GOOD: Use @BeforeEach for common setup
@BeforeEach
void setUp() {
    ticketRepository.deleteAll();  // Clean slate
    
    // Create common test data
    ticket1 = createTestTicket("Title 1", OPEN);
    ticket2 = createTestTicket("Title 2", IN_PROGRESS);
}

// Or use @Sql for complex data
@Test
@Sql("/test-data/tickets.sql")
void testWithPreloadedData() {
    // Test with data from SQL file
}
```

---

## Property-Based Testing

### jqwik Configuration

**When to Use**: Testing universal properties across input ranges

**Framework**: jqwik (Java property-based testing)

**Basic Structure**:
```java
// ✅ GOOD: Property test with domain-specific generators
class TicketCreationPropertyTest {
    
    @Property(tries = 100)
    void ticketCreation_alwaysProducesOpenStatus(
        @ForAll @StringLength(min = 1, max = 255) String title,
        @ForAll @StringLength(min = 1, max = 5000) String description,
        @ForAll("validPriorities") Priority priority
    ) {
        // Arrange
        CreateTicketRequest request = new CreateTicketRequest(title, description, priority);
        
        // Act
        TicketDetailResponse ticket = ticketService.createTicket(request);
        
        // Assert
        assertThat(ticket.status()).isEqualTo(TicketStatus.OPEN);
        assertThat(ticket.id()).isNotNull();
        assertThat(ticket.createdAt()).isNotNull();
    }
    
    @Provide
    Arbitrary<Priority> validPriorities() {
        return Arbitraries.of(Priority.class);
    }
}
```

### Property Test Guidelines

**Tag with Feature and Property Number**:
```java
// Feature: support-ticket-management, Property 1: Ticket creation invariants
@Property(tries = 100)
void ticketCreationInvariants(...) { }
```

**Minimum 100 Iterations**:
```java
// ✅ GOOD: Sufficient iterations
@Property(tries = 100)

// ❌ BAD: Too few iterations
@Property(tries = 10)
```

**Use Assumptions to Filter Invalid Inputs**:
```java
@Property
void someProperty(@ForAll String input) {
    Assume.that(!input.isBlank());  // Skip blank inputs
    
    // Test with non-blank input
}
```

**Custom Generators**:
```java
@Provide
Arbitrary<CreateTicketRequest> validTicketRequests() {
    Arbitrary<String> titles = Arbitraries.strings()
        .withCharRange('a', 'z')
        .ofMinLength(1)
        .ofMaxLength(255);
    
    Arbitrary<String> descriptions = Arbitraries.strings()
        .ofMinLength(1)
        .ofMaxLength(5000);
    
    Arbitrary<Priority> priorities = Arbitraries.of(Priority.class);
    
    return Combinators.combine(titles, descriptions, priorities)
        .as(CreateTicketRequest::new);
}
```

---

## Test Organization

### Directory Structure

```
src/test/java/com/example/supportticket/
├── unit/                           # Unit tests
│   ├── service/
│   │   ├── TicketServiceTest.java
│   │   └── CommentServiceTest.java
│   ├── validator/
│   │   └── TicketValidatorTest.java
│   └── util/
│       └── DateUtilsTest.java
│
├── integration/                    # Integration tests
│   ├── controller/
│   │   ├── TicketControllerIntegrationTest.java
│   │   └── CommentControllerIntegrationTest.java
│   └── repository/
│       └── TicketRepositoryTest.java
│
├── property/                       # Property-based tests
│   ├── TicketCreationPropertyTest.java
│   ├── StateMachinePropertyTest.java
│   ├── KeywordSearchPropertyTest.java
│   └── ... (13 property tests total)
│
└── fixtures/                       # Test data builders
    ├── TicketFixtures.java
    └── CommentFixtures.java
```

### Test Class Naming

| Type | Suffix | Example |
|------|--------|---------|
| Unit Test | `Test` | `TicketServiceTest` |
| Integration Test | `IntegrationTest` | `TicketControllerIntegrationTest` |
| Property Test | `PropertyTest` | `StateMachinePropertyTest` |
| Repository Test | `RepositoryTest` | `TicketRepositoryTest` |

---

## Assertion Libraries

### AssertJ (Preferred)

**Use AssertJ for Fluent Assertions**:
```java
// ✅ GOOD: Fluent AssertJ assertions
assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
assertThat(ticket.getId()).isNotNull();
assertThat(ticket.getTitle()).isNotBlank();
assertThat(ticket.getComments()).hasSize(2);
assertThat(result).isInstanceOf(TicketDetailResponse.class);

// ❌ BAD: Traditional JUnit assertions
assertEquals(TicketStatus.OPEN, ticket.getStatus());
assertNotNull(ticket.getId());
assertTrue(ticket.getTitle().length() > 0);
```

**Exception Assertions**:
```java
// ✅ GOOD: AssertJ exception assertions
assertThatThrownBy(() -> ticketService.getTicket(999L))
    .isInstanceOf(TicketNotFoundException.class)
    .hasMessageContaining("999");

// ❌ BAD: JUnit 4 style
@Test(expected = TicketNotFoundException.class)
public void testNotFound() {
    ticketService.getTicket(999L);
}
```

---

## Test Data Management

### Test Fixtures / Builders

**Use Builder Pattern for Test Data**:
```java
// ✅ GOOD: Reusable test data builder
public class TicketFixtures {
    
    public static Ticket.TicketBuilder aTicket() {
        return Ticket.builder()
            .title("Test Ticket")
            .description("Test Description")
            .status(TicketStatus.OPEN)
            .priority(Priority.MEDIUM)
            .createdAt(Instant.now())
            .updatedAt(Instant.now());
    }
    
    public static Ticket anOpenTicket() {
        return aTicket().status(TicketStatus.OPEN).build();
    }
    
    public static Ticket aClosedTicket() {
        return aTicket().status(TicketStatus.CLOSED).build();
    }
}

// Usage in tests
@Test
void testSomething() {
    Ticket ticket = aTicket()
        .title("Custom Title")
        .priority(Priority.HIGH)
        .build();
    
    // Test with ticket
}
```

**Avoid Magic Numbers and Strings**:
```java
// ✅ GOOD: Named constants
private static final String VALID_TITLE = "Login Issue";
private static final String VALID_DESCRIPTION = "Users cannot log in";
private static final Long EXISTING_TICKET_ID = 1L;

// ❌ BAD: Magic values
mockMvc.perform(get("/api/tickets/1"))  // What is 1?
```

---

## Test Coverage

### Coverage Targets

| Coverage Type | Target | Tool |
|---------------|--------|------|
| Line Coverage | >80% | JaCoCo |
| Branch Coverage | >70% | JaCoCo |
| Method Coverage | >85% | JaCoCo |

### What to Exclude from Coverage

```xml
<!-- pom.xml: JaCoCo exclusions -->
<configuration>
    <excludes>
        <exclude>**/dto/**</exclude>
        <exclude>**/config/**</exclude>
        <exclude>**/Application.class</exclude>
    </excludes>
</configuration>
```

**Rationale**:
- DTOs are data containers (no logic to test)
- Configuration classes are declarative
- Main application class is entry point

---

## Testing Anti-Patterns

### ❌ DON'T: Test Implementation Details

```java
// ❌ BAD: Testing private methods
@Test
void testPrivateMethod() {
    Method method = TicketService.class.getDeclaredMethod("validateTitle");
    method.setAccessible(true);
    // ...
}

// ✅ GOOD: Test through public interface
@Test
void createTicket_invalidTitle_throwsException() {
    CreateTicketRequest request = new CreateTicketRequest("", "Description", null);
    assertThatThrownBy(() -> ticketService.createTicket(request))
        .isInstanceOf(ValidationException.class);
}
```

### ❌ DON'T: Over-Mock

```java
// ❌ BAD: Mocking everything
@Test
void testSomething() {
    Ticket mockTicket = mock(Ticket.class);
    when(mockTicket.getTitle()).thenReturn("Title");
    when(mockTicket.getStatus()).thenReturn(TicketStatus.OPEN);
    // ... 20 more lines of mocking
}

// ✅ GOOD: Use real objects for simple data
@Test
void testSomething() {
    Ticket ticket = aTicket().build();  // Real object
    // Mock only external dependencies like repositories
}
```

### ❌ DON'T: Flaky Tests

```java
// ❌ BAD: Depends on system time
@Test
void testTimestamp() {
    Ticket ticket = ticketService.createTicket(request);
    assertThat(ticket.getCreatedAt()).isEqualTo(Instant.now());  // Flaky!
}

// ✅ GOOD: Verify range or use Clock
@Test
void testTimestamp() {
    Instant before = Instant.now();
    Ticket ticket = ticketService.createTicket(request);
    Instant after = Instant.now();
    
    assertThat(ticket.getCreatedAt())
        .isAfterOrEqualTo(before)
        .isBeforeOrEqualTo(after);
}
```

### ❌ DON'T: Shared Mutable State

```java
// ❌ BAD: Shared mutable field
class TicketServiceTest {
    private Ticket sharedTicket = new Ticket();  // Dangerous!
    
    @Test
    void test1() {
        sharedTicket.setTitle("Test 1");
        // ...
    }
    
    @Test
    void test2() {
        sharedTicket.setTitle("Test 2");  // Affects test1!
        // ...
    }
}

// ✅ GOOD: Fresh state per test
class TicketServiceTest {
    private Ticket ticket;
    
    @BeforeEach
    void setUp() {
        ticket = aTicket().build();  // New instance each test
    }
}
```

---

## Performance Testing

### Test Execution Speed

**Fast Tests (<100ms per test)**:
- Unit tests with mocks
- Property tests with simple generators

**Medium Tests (100ms-1s per test)**:
- Integration tests with H2
- Property tests with complex generators

**Slow Tests (>1s per test)**:
- Full context tests with external systems
- E2E tests

**Guideline**: Run fast tests frequently, slow tests before commit/push

### Parallel Execution

```xml
<!-- pom.xml: Enable parallel test execution -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        <threadCount>4</threadCount>
    </configuration>
</plugin>
```

---

## Continuous Integration

### Test Execution in CI

**GitHub Actions Example**:
```yaml
- name: Run tests
  run: ./mvnw test
  
- name: Generate coverage report
  run: ./mvnw jacoco:report
  
- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
```

**Fail Fast**: Stop build on first test failure

**Test Reports**: Generate and archive JUnit XML reports

---

## Test Documentation

### JavaDoc for Test Classes

```java
/**
 * Unit tests for {@link TicketService}.
 * 
 * Tests cover:
 * - Ticket creation with various valid/invalid inputs
 * - Status transitions with state machine validation
 * - Partial updates preserving untouched fields
 */
class TicketServiceTest {
    // Tests...
}
```

### Test Method Documentation

```java
/**
 * Verifies that attempting to transition a ticket from CLOSED to OPEN
 * (a forbidden transition) throws InvalidStatusTransitionException
 * with both status names in the error message.
 */
@Test
void transitionStatus_fromClosedToOpen_throwsInvalidTransitionException() {
    // Test implementation
}
```

---

## Summary Checklist

Before merging code, ensure:

- [ ] All tests pass locally
- [ ] New features have unit tests
- [ ] New features have integration tests
- [ ] Business logic has property tests (if applicable)
- [ ] Coverage meets targets (>80% lines)
- [ ] No flaky tests (run suite 3 times)
- [ ] Test names are descriptive
- [ ] Tests use AssertJ for assertions
- [ ] Mocks are used appropriately (only external dependencies)
- [ ] Tests are fast (<5 seconds for entire suite)
- [ ] CI pipeline passes

---

**Version**: 1.0  
**Last Updated**: 2026-09-23
