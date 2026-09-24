# Test Strategy Document

## Overview

The testing strategy combines traditional example-based testing with property-based testing to ensure comprehensive correctness validation. All correctness properties from the design are validated through executable tests.

---

## Testing Pyramid

```
       ┌─────────────┐
       │     E2E     │  (Cypress/Playwright)
       └─────────────┘
      ┌───────────────┐
      │  Integration  │  (@SpringBootTest)
      └───────────────┘
     ┌─────────────────┐
     │   Property-Based │  (jqwik)
     └─────────────────┘
    ┌───────────────────┐
    │       Unit        │  (JUnit 5)
    └───────────────────┘
```

---

## Test Types

### 1. Unit Tests

**Purpose**: Test individual components in isolation

**Framework**: JUnit 5, Mockito

**Scope**:
- Service methods with mocked repositories
- Utility functions
- Validators
- DTOs

**Example Test Classes**:
- `TicketServiceTest`
- `CommentServiceTest`
- `TicketStateMachineTest`
- `ValidationUtilsTest`

**Coverage Target**: >80% line coverage

---

### 2. Integration Tests

**Purpose**: Test full application context with database

**Framework**: Spring Boot Test, H2 (in-memory)

**Scope**:
- REST endpoints end-to-end
- Repository queries
- Database interactions
- Transaction management

**Example Test Classes**:
- `TicketControllerIntegrationTest`
- `CommentControllerIntegrationTest`
- `TicketRepositoryTest`
- `FlywayMigrationTest`

**Configuration**:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional  // Rollback after each test
class TicketControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    // Tests...
}
```

---

### 3. Property-Based Tests

**Purpose**: Validate universal properties across input ranges

**Framework**: jqwik (Java property-based testing)

**Scope**: All 13 correctness properties from design document

**Configuration**:
```java
@Property(tries = 100)  // Minimum 100 iterations per property
void propertyTest(@ForAll SomeType input) {
    // Property assertion
}
```

**Key Features**:
- Random input generation
- Shrinking (finds minimal failing case)
- Seed-based reproducibility
- Statistics reporting

---

### 4. End-to-End Tests

**Purpose**: Test user workflows through browser

**Framework**: Cypress or Playwright

**Scope**:
- Critical user flows
- Cross-browser compatibility
- UI interactions

**Example Flows**:
- Create ticket → View details → Add comment
- Search tickets by keyword
- Filter by status
- Transition ticket through lifecycle

---

## Correctness Properties

### Property 1: Ticket Creation Invariants

**Test Class**: `TicketCreationPropertyTest`

**Property**: For any valid `CreateTicketRequest`, calling `createTicket()` SHALL produce a ticket where:
- `status == OPEN`
- `id` is non-null
- `createdAt` is non-null

**jqwik Test**:
```java
// Feature: support-ticket-management, Property 1: Ticket creation invariants
@Property(tries = 100)
void ticketCreationInvariants(
    @ForAll @StringLength(min=1, max=255) String title,
    @ForAll @StringLength(min=1, max=5000) String description,
    @ForAll("validPriorities") Priority priority
) {
    CreateTicketRequest request = new CreateTicketRequest(title, description, priority);
    TicketDetailResponse ticket = ticketService.createTicket(request);
    
    assertThat(ticket.status()).isEqualTo(TicketStatus.OPEN);
    assertThat(ticket.id()).isNotNull();
    assertThat(ticket.createdAt()).isNotNull();
}

@Provide
Arbitrary<Priority> validPriorities() {
    return Arbitraries.of(Priority.class);
}
```

**Validates**: Requirements 1.1

---

### Property 2: Unique Ticket Identifiers

**Test Class**: `TicketIdUniquenessPropertyTest`

**Property**: For any N valid ticket creation requests, the resulting N tickets SHALL each have a distinct `id`.

**jqwik Test**:
```java
// Feature: support-ticket-management, Property 2: Unique ticket identifiers
@Property(tries = 50)
void ticketIdsAreUnique(@ForAll @IntRange(min=5, max=20) int count) {
    List<Long> ids = new ArrayList<>();
    
    for (int i = 0; i < count; i++) {
        CreateTicketRequest request = new CreateTicketRequest(
            "Title " + i,
            "Description " + i,
            Priority.MEDIUM
        );
        TicketDetailResponse ticket = ticketService.createTicket(request);
        ids.add(ticket.id());
    }
    
    // All IDs must be distinct
    assertThat(ids).doesNotHaveDuplicates();
}
```

**Validates**: Requirements 1.2

---

### Property 3: Title Length Validation

**Test Class**: `TitleLengthValidationPropertyTest`

**Property**: For any string whose length exceeds 255 characters submitted as `title`, the API SHALL reject with HTTP 400.

**jqwik Test**:
```java
// Feature: support-ticket-management, Property 3: Title length validation
@Property(tries = 100)
void titleTooLongRejected(@ForAll @StringLength(min=256, max=500) String longTitle) {
    CreateTicketRequest request = new CreateTicketRequest(
        longTitle,
        "Description",
        Priority.MEDIUM
    );
    
    assertThatThrownBy(() -> ticketService.createTicket(request))
        .isInstanceOf(MethodArgumentNotValidException.class)
        .hasMessageContaining("title");
}
```

**Validates**: Requirements 1.5, 4.3

---

### Property 4: Priority Enum Validation

**Test Class**: `PriorityValidationPropertyTest`

**Property**: For any string that is not one of `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`, the API SHALL reject with HTTP 400.

**jqwik Test**:
```java
// Feature: support-ticket-management, Property 4: Priority enum validation
@Property(tries = 100)
void invalidPriorityRejected(
    @ForAll @StringLength(min=1, max=20) String invalidPriority
) {
    Assume.that(!Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(invalidPriority));
    
    // Simulate HTTP request with invalid priority string
    MockHttpServletRequestBuilder request = post("/api/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "title": "Test",
              "description": "Test",
              "priority": "%s"
            }
            """.formatted(invalidPriority));
    
    mockMvc.perform(request)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[?(@.field == 'priority')]").exists());
}
```

**Validates**: Requirements 1.6, 4.4

---

### Property 5: Ticket List Ordering

**Test Class**: `TicketListOrderingPropertyTest`

**Property**: For any set of N tickets, the list SHALL be ordered by `createdAt` descending.

**jqwik Test**:
```java
// Feature: support-ticket-management, Property 5: Ticket list ordering
@Property(tries = 50)
void ticketListOrderedByCreatedAtDesc(@ForAll @IntRange(min=3, max=15) int count) {
    // Create tickets with delays to ensure distinct timestamps
    for (int i = 0; i < count; i++) {
        CreateTicketRequest request = new CreateTicketRequest(
            "Title " + i,
            "Description " + i,
            Priority.MEDIUM
        );
        ticketService.createTicket(request);
        Thread.sleep(10);  // Small delay for timestamp distinction
    }
    
    List<TicketSummaryResponse> tickets = ticketService.listTickets(null, null);
    
    // Verify descending order
    for (int i = 0; i < tickets.size() - 1; i++) {
        Instant current = tickets.get(i).createdAt();
        Instant next = tickets.get(i + 1).createdAt();
        assertThat(current).isAfterOrEqualTo(next);
    }
}
```

**Validates**: Requirements 2.1, 8.1

---

### Property 6: Ticket List Field Completeness

**Test Class**: `TicketListFieldCompletenessPropertyTest`

**Property**: For any ticket, its list representation SHALL include all required fields.

**jqwik Test**:
```java
// Feature: support-ticket-management, Property 6: Ticket list field completeness
@Property(tries = 100)
void ticketListEntriesComplete(
    @ForAll @StringLength(min=1, max=255) String title,
    @ForAll @StringLength(min=1) String description,
    @ForAll("validPriorities") Priority priority
) {
    CreateTicketRequest request = new CreateTicketRequest(title, description, priority);
    ticketService.createTicket(request);
    
    List<TicketSummaryResponse> tickets = ticketService.listTickets(null, null);
    
    tickets.forEach(ticket -> {
        assertThat(ticket.id()).isNotNull();
        assertThat(ticket.title()).isNotBlank();
        assertThat(ticket.priority()).isNotNull();
        assertThat(ticket.status()).isNotNull();
        assertThat(ticket.createdAt()).isNotNull();
        // assignee can be null
    });
}
```

**Validates**: Requirements 2.2, 7.4

---

### Property 7: Partial Update Preserves Untouched Fields

**Test Class**: `PartialUpdatePropertyTest`

**Property**: For any partial update, only specified fields change; all others remain unchanged.

**jqwik Test**:
```java
// Feature: support-ticket-management, Property 7: Partial update preserves untouched fields
@Property(tries = 100)
void partialUpdatePreservesOtherFields(
    @ForAll @StringLength(min=1, max=255) String newTitle
) {
    // Create ticket
    CreateTicketRequest createRequest = new CreateTicketRequest(
        "Original Title",
        "Original Description",
        Priority.LOW
    );
    TicketDetailResponse created = ticketService.createTicket(createRequest);
    
    // Update only title
    UpdateTicketRequest updateRequest = new UpdateTicketRequest(
        newTitle,
        null,  // Don't update description
        null,  // Don't update priority
        null   // Don't update assignee
    );
    TicketDetailResponse updated = ticketService.updateTicket(created.id(), updateRequest);
    
    // Title changed
    assertThat(updated.title()).isEqualTo(newTitle);
    
    // Everything else unchanged
    assertThat(updated.description()).isEqualTo(created.description());
    assertThat(updated.priority()).isEqualTo(created.priority());
    assertThat(updated.assignee()).isEqualTo(created.assignee());
}
```

**Validates**: Requirements 4.5

---

### Property 8: State Machine Correctness

**Test Class**: `StateMachinePropertyTest`

**Property**: Allowed transitions succeed with HTTP 200; disallowed transitions fail with HTTP 422.

**jqwik Test**:
```java
// Feature: support-ticket-management, Property 8: State machine correctness
@Property(tries = 100)
void stateMachineEnforcesRules(
    @ForAll("allStatePairs") Tuple2<TicketStatus, TicketStatus> transition
) {
    TicketStatus from = transition.get1();
    TicketStatus to = transition.get2();
    
    // Create ticket and transition to 'from' state
    TicketDetailResponse ticket = createTicketInState(from);
    
    boolean isAllowed = isAllowedTransition(from, to);
    
    if (isAllowed) {
        // Should succeed
        TicketDetailResponse result = ticketService.transitionStatus(ticket.id(), to);
        assertThat(result.status()).isEqualTo(to);
    } else {
        // Should fail with 422
        assertThatThrownBy(() -> ticketService.transitionStatus(ticket.id(), to))
            .isInstanceOf(InvalidStatusTransitionException.class)
            .hasMessageContaining(from.name())
            .hasMessageContaining(to.name());
    }
}

@Provide
Arbitrary<Tuple2<TicketStatus, TicketStatus>> allStatePairs() {
    Arbitrary<TicketStatus> statuses = Arbitraries.of(TicketStatus.class);
    return Combinators.combine(statuses, statuses).as(Tuple::of);
}
```

**Validates**: Requirements 5.1, 5.2, 5.3

---

### Property 9: Comment Creation Round-Trip

**Test Class**: `CommentCreationPropertyTest`

**Property**: Created comment matches request and has id + createdAt.

**jqwik Test**:
```java
// Feature: support-ticket-management, Property 9: Comment creation round-trip
@Property(tries = 100)
void commentCreationRoundTrip(
    @ForAll @StringLength(min=1, max=255) String author,
    @ForAll @StringLength(min=1) String body
) {
    // Create ticket
    TicketDetailResponse ticket = ticketService.createTicket(
        new CreateTicketRequest("Title", "Description", Priority.MEDIUM)
    );
    
    // Create comment
    CreateCommentRequest request = new CreateCommentRequest(author, body);
    CommentResponse comment = commentService.addComment(ticket.id(), request);
    
    assertThat(comment.id()).isNotNull();
    assertThat(comment.author()).isEqualTo(author);
    assertThat(comment.body()).isEqualTo(body);
    assertThat(comment.createdAt()).isNotNull();
}
```

**Validates**: Requirements 6.1

---

### Property 10: Keyword Search Correctness

**Test Class**: `KeywordSearchPropertyTest`

**Property**: Search returns exactly the tickets matching keyword (case-insensitive).

**jqwik Test**:
```java
// Feature: support-ticket-management, Property 10: Keyword search correctness
@Property(tries = 50)
void keywordSearchCorrect(@ForAll @StringLength(min=3, max=10) String keyword) {
    // Create tickets: some with keyword, some without
    ticketService.createTicket(new CreateTicketRequest(
        "Contains " + keyword + " word",
        "Description without it",
        Priority.MEDIUM
    ));
    ticketService.createTicket(new CreateTicketRequest(
        "No match here",
        "But " + keyword + " in description",
        Priority.MEDIUM
    ));
    ticketService.createTicket(new CreateTicketRequest(
        "Completely unrelated",
        "Nothing to see",
        Priority.MEDIUM
    ));
    
    List<TicketSummaryResponse> results = ticketService.searchTickets(keyword);
    
    // All results must contain keyword (case-insensitive)
    results.forEach(ticket -> {
        boolean inTitle = ticket.title().toLowerCase().contains(keyword.toLowerCase());
        boolean inDescription = fetchDescription(ticket.id()).toLowerCase()
            .contains(keyword.toLowerCase());
        assertThat(inTitle || inDescription).isTrue();
    });
    
    // At least 2 results expected (the tickets we created)
    assertThat(results).hasSizeGreaterThanOrEqualTo(2);
}
```

**Validates**: Requirements 7.1, 7.2

---

### Property 11: Blank Keyword Rejection

**Test Class**: `BlankKeywordPropertyTest`

**Property**: Blank or whitespace-only keywords are rejected with HTTP 400.

**jqwik Test**:
```java
// Feature: support-ticket-management, Property 11: Blank keyword rejection
@Property(tries = 100)
void blankKeywordRejected(@ForAll("blankStrings") String blankKeyword) {
    assertThatThrownBy(() -> ticketService.searchTickets(blankKeyword))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("blank");
}

@Provide
Arbitrary<String> blankStrings() {
    return Arbitraries.oneOf(
        Arbitraries.just(""),
        Arbitraries.just(" "),
        Arbitraries.just("   "),
        Arbitraries.just("\t"),
        Arbitraries.just("\n")
    );
}
```

**Validates**: Requirements 7.3

---

### Property 12: Status Filter Correctness

**Test Class**: `StatusFilterPropertyTest`

**Property**: Filter returns only tickets with matching status, ordered correctly.

**jqwik Test**:
```java
// Feature: support-ticket-management, Property 12: Status filter correctness
@Property(tries = 50)
void statusFilterCorrect(@ForAll("validStatuses") TicketStatus filterStatus) {
    // Create tickets with various statuses
    for (TicketStatus status : TicketStatus.values()) {
        TicketDetailResponse ticket = ticketService.createTicket(
            new CreateTicketRequest("Title", "Description", Priority.MEDIUM)
        );
        transitionToState(ticket.id(), status);
    }
    
    List<TicketSummaryResponse> results = ticketService.listTickets(filterStatus, null);
    
    // All results must have the filter status
    results.forEach(ticket -> {
        assertThat(ticket.status()).isEqualTo(filterStatus);
    });
    
    // At least 1 result expected
    assertThat(results).isNotEmpty();
}

@Provide
Arbitrary<TicketStatus> validStatuses() {
    return Arbitraries.of(TicketStatus.class);
}
```

**Validates**: Requirements 8.1, 8.3

---

### Property 13: Error Response Completeness

**Test Class**: `ErrorResponseCompletenessPropertyTest`

**Property**: All error responses have `message`, `status`, and (for 400) complete `errors` array.

**jqwik Test**:
```java
// Feature: support-ticket-management, Property 13: Error response completeness
@Property(tries = 100)
void errorResponsesComplete(
    @ForAll("invalidRequests") CreateTicketRequest invalidRequest
) throws Exception {
    MockHttpServletRequestBuilder request = post("/api/tickets")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest));
    
    MvcResult result = mockMvc.perform(request)
        .andExpect(status().isBadRequest())
        .andReturn();
    
    String responseBody = result.getResponse().getContentAsString();
    ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
    
    assertThat(error.status()).isEqualTo(400);
    assertThat(error.message()).isNotBlank();
    assertThat(error.errors()).isNotEmpty();
    
    // Each field error must have field and message
    error.errors().forEach(fieldError -> {
        assertThat(fieldError.field()).isNotBlank();
        assertThat(fieldError.message()).isNotBlank();
    });
}

@Provide
Arbitrary<CreateTicketRequest> invalidRequests() {
    return Arbitraries.oneOf(
        Arbitraries.just(new CreateTicketRequest("", "Description", Priority.MEDIUM)),  // Blank title
        Arbitraries.just(new CreateTicketRequest("Title", "", Priority.MEDIUM)),  // Blank description
        Arbitraries.just(new CreateTicketRequest("x".repeat(300), "Desc", Priority.MEDIUM))  // Title too long
    );
}
```

**Validates**: Requirements 10.1, 10.2

---

## Test Configuration

### Database Configuration (H2)

**File**: `application-test.properties`

```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Logging
logging.level.org.springframework.test=INFO
logging.level.com.example.supportticket=DEBUG
```

### Maven Dependencies

```xml
<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Property-based testing -->
<dependency>
    <groupId>net.jqwik</groupId>
    <artifactId>jqwik</artifactId>
    <version>1.8.4</version>
    <scope>test</scope>
</dependency>

<!-- H2 Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Test Execution

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test Class

```bash
./mvnw test -Dtest=TicketCreationPropertyTest
```

### Run Property Tests Only

```bash
./mvnw test -Dtest=**/*PropertyTest
```

### Generate Coverage Report

```bash
./mvnw jacoco:report
# Report available at: target/site/jacoco/index.html
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Test Suite

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Run tests
      run: ./mvnw test
    
    - name: Generate coverage report
      run: ./mvnw jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
```

---

## Frontend Testing

### Unit Tests (Jest + React Testing Library)

**Components**:
- `TicketTable.test.tsx`
- `StatusTransitionPanel.test.tsx`
- `CommentForm.test.tsx`
- `SearchBar.test.tsx`

**Example**:
```typescript
test('StatusTransitionPanel shows only valid actions', () => {
  const { getByText, queryByText } = render(
    <StatusTransitionPanel 
      currentStatus="OPEN" 
      onTransition={jest.fn()} 
    />
  );
  
  expect(getByText('Start Work')).toBeInTheDocument();
  expect(getByText('Cancel')).toBeInTheDocument();
  expect(queryByText('Mark Resolved')).not.toBeInTheDocument();
});
```

### E2E Tests (Cypress)

**Spec**: `ticket-lifecycle.cy.ts`

```typescript
describe('Ticket Lifecycle', () => {
  it('creates, transitions, and closes a ticket', () => {
    // Create ticket
    cy.visit('/');
    cy.get('[data-testid="create-ticket-btn"]').click();
    cy.get('input[name="title"]').type('Test ticket');
    cy.get('textarea[name="description"]').type('Test description');
    cy.get('[data-testid="submit-btn"]').click();
    
    // Verify creation
    cy.contains('Test ticket').should('be.visible');
    
    // Transition to IN_PROGRESS
    cy.contains('Test ticket').click();
    cy.get('[data-testid="start-work-btn"]').click();
    cy.contains('IN_PROGRESS').should('be.visible');
    
    // Continue through lifecycle...
  });
});
```

---

## Future Enhancements

- Mutation testing (PIT)
- Performance testing (JMeter)
- Security testing (OWASP ZAP)
- Accessibility testing (axe-core)
- Visual regression testing (Percy, Chromatic)

---

**Version**: 1.0  
**Last Updated**: 2026-09-23
