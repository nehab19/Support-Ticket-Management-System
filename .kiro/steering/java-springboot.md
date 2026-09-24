---
inclusion: auto
---

# Java/Spring Boot Coding Guidelines

**Project**: Support Ticket Management System  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Purpose

These guidelines establish consistent coding standards for Java/Spring Boot development in this project. All code—whether written by humans or AI—must follow these conventions.

**Scope**: Backend Java code (Spring Boot application)  
**Authority**: Complements PROJECT_CONSTITUTION.md with implementation-level guidance

---

## Technology Versions

- **Java**: 21 (LTS)
- **Spring Boot**: 3.x
- **Build Tool**: Gradle 8.x
- **JPA Provider**: Hibernate 6.x
- **Database**: PostgreSQL 15+ (production), H2 2.x (tests)
- **Migration Tool**: Flyway 9.x

---

## Layered Architecture

### Layer Responsibilities

```
┌─────────────────────────────────────┐
│  Controller Layer (@RestController) │  → HTTP/REST concerns only
└────────────┬────────────────────────┘
             │ DTOs
┌────────────▼────────────────────────┐
│  Service Layer (@Service)           │  → Business logic, validation, orchestration
└────────────┬────────────────────────┘
             │ Entities
┌────────────▼────────────────────────┐
│  Repository Layer (JpaRepository)   │  → Data access, queries
└────────────┬────────────────────────┘
             │ SQL
┌────────────▼────────────────────────┐
│  Database (PostgreSQL/H2)           │  → Persistence
└─────────────────────────────────────┘
```

**Controller Layer**:
- Handle HTTP requests and responses
- Convert between DTOs and domain entities
- Delegate business logic to services
- Return appropriate HTTP status codes
- **NO business logic in controllers**

**Service Layer**:
- Implement business rules and validation
- Orchestrate operations across multiple repositories
- Define transactional boundaries
- Convert entities to DTOs for responses
- Throw business exceptions (not HTTP exceptions)

**Repository Layer**:
- Extend `JpaRepository<Entity, ID>`
- Define custom query methods
- **NO business logic in repositories**
- Use Spring Data JPA method name derivation where possible

---

## Package Organization

### Standard Package Structure

```
com.example.supportticket/
├── SupportTicketApplication.java          # Main application class
│
├── controller/                             # REST endpoints
│   ├── TicketController.java
│   └── CommentController.java
│
├── service/                                # Business logic
│   ├── TicketService.java
│   ├── CommentService.java
│   └── TicketStateMachine.java           # Business rule components
│
├── repository/                             # Data access
│   ├── TicketRepository.java
│   └── CommentRepository.java
│
├── model/                                  # JPA entities and enums
│   ├── Ticket.java
│   ├── Comment.java
│   ├── TicketStatus.java                 # Enum
│   └── Priority.java                     # Enum
│
├── dto/                                    # Data Transfer Objects
│   ├── request/
│   │   ├── CreateTicketRequest.java
│   │   ├── UpdateTicketRequest.java
│   │   └── StatusTransitionRequest.java
│   └── response/
│       ├── TicketSummaryResponse.java
│       ├── TicketDetailResponse.java
│       ├── CommentResponse.java
│       └── ErrorResponse.java
│
├── exception/                              # Custom exceptions
│   ├── TicketNotFoundException.java
│   ├── InvalidStatusTransitionException.java
│   └── GlobalExceptionHandler.java       # @RestControllerAdvice
│
└── config/                                 # Configuration classes
    ├── SecurityConfig.java
    └── DatabaseConfig.java
```

**Rules**:
- One top-level class per file
- File name matches class name
- Package names are lowercase, singular nouns
- Group related classes in same package
- Keep packages focused (single responsibility)

---

## Java 21 Features

### Use Modern Java Features

**Records** (for DTOs and immutable data):
```java
// ✅ GOOD: Use records for DTOs
public record CreateTicketRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title,
    
    @NotBlank(message = "Description is required")
    String description,
    
    Priority priority  // Optional, defaults to MEDIUM in service
) {}

// ✅ GOOD: Use records for response DTOs
public record TicketSummaryResponse(
    Long id,
    String title,
    TicketStatus status,
    Priority priority,
    Instant createdAt
) {}
```

**Pattern Matching** (for instanceof checks):
```java
// ✅ GOOD: Use pattern matching
if (exception instanceof TicketNotFoundException notFound) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(notFound.getMessage()));
}

// ❌ BAD: Old-style casting
if (exception instanceof TicketNotFoundException) {
    TicketNotFoundException notFound = (TicketNotFoundException) exception;
    // ...
}
```

**Switch Expressions**:
```java
// ✅ GOOD: Use switch expressions
String statusMessage = switch (ticket.getStatus()) {
    case OPEN -> "Ticket is awaiting assignment";
    case IN_PROGRESS -> "Ticket is being worked on";
    case RESOLVED -> "Ticket work is complete";
    case CLOSED -> "Ticket is finalized";
    case CANCELLED -> "Ticket was cancelled";
};

// ❌ BAD: Traditional switch with breaks
String statusMessage;
switch (ticket.getStatus()) {
    case OPEN:
        statusMessage = "Ticket is awaiting assignment";
        break;
    case IN_PROGRESS:
        // ...
}
```

**Text Blocks** (for multi-line strings):
```java
// ✅ GOOD: Use text blocks for SQL, JSON templates
String query = """
    SELECT t FROM Ticket t
    WHERE t.status = :status
    AND (LOWER(t.title) LIKE LOWER(:keyword)
         OR LOWER(t.description) LIKE LOWER(:keyword))
    ORDER BY t.createdAt DESC
    """;
```

---

## Dependency Injection

### Constructor Injection (Required)

**Always use constructor injection, never field injection.**

```java
// ✅ GOOD: Constructor injection with final fields
@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketStateMachine stateMachine;
    
    public TicketService(
        TicketRepository ticketRepository,
        TicketStateMachine stateMachine
    ) {
        this.ticketRepository = ticketRepository;
        this.stateMachine = stateMachine;
    }
    
    // Methods...
}

// ✅ EVEN BETTER: Use Lombok @RequiredArgsConstructor
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketStateMachine stateMachine;
    
    // Methods...
}

// ❌ BAD: Field injection
@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;  // Hard to test, hidden dependencies
}

// ❌ BAD: Setter injection
@Service
public class TicketService {
    private TicketRepository ticketRepository;
    
    @Autowired
    public void setTicketRepository(TicketRepository repo) {
        this.ticketRepository = repo;
    }
}
```

**Why Constructor Injection**:
- Dependencies are explicit and visible
- Immutable fields (final)
- Easier to test (can construct without Spring)
- Prevents circular dependencies
- Fails fast if dependencies missing

---

## DTOs vs Entities

### Never Expose JPA Entities

**Rule**: Controllers MUST use DTOs for request/response bodies, never JPA entities.

```java
// ✅ GOOD: Controller uses DTOs
@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;
    
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }
    
    @PostMapping
    public ResponseEntity<TicketDetailResponse> createTicket(
        @Valid @RequestBody CreateTicketRequest request
    ) {
        TicketDetailResponse response = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailResponse> getTicket(@PathVariable Long id) {
        TicketDetailResponse response = ticketService.getTicketById(id);
        return ResponseEntity.ok(response);
    }
}

// ❌ BAD: Exposing entity directly
@PostMapping
public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
    return ResponseEntity.ok(ticketRepository.save(ticket));  // WRONG!
}
```

### DTO Conversion

**Service layer converts between entities and DTOs.**

```java
// ✅ GOOD: Service handles conversion
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    
    @Transactional
    public TicketDetailResponse createTicket(CreateTicketRequest request) {
        // Convert request DTO to entity
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority() != null ? request.priority() : Priority.MEDIUM);
        ticket.setStatus(TicketStatus.OPEN);
        
        // Save entity
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Convert entity to response DTO
        return toDetailResponse(savedTicket);
    }
    
    private TicketDetailResponse toDetailResponse(Ticket ticket) {
        return new TicketDetailResponse(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getStatus(),
            ticket.getPriority(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt(),
            ticket.getComments().stream()
                .map(this::toCommentResponse)
                .toList()
        );
    }
}
```

**Why DTOs**:
- Decouples API contract from database schema
- Prevents exposing internal entity structure
- Avoids Jackson serialization issues (lazy loading, circular references)
- Allows different views (summary vs. detail)
- Enables API versioning without entity changes

---

## Validation

### Use Jakarta Bean Validation

**Annotate request DTOs with validation constraints.**

```java
// ✅ GOOD: Validation annotations on DTO
public record CreateTicketRequest(
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    String title,
    
    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    String description,
    
    // Optional field, no @NotNull
    Priority priority
) {}

// Controller validates with @Valid
@PostMapping
public ResponseEntity<TicketDetailResponse> createTicket(
    @Valid @RequestBody CreateTicketRequest request  // @Valid triggers validation
) {
    TicketDetailResponse response = ticketService.createTicket(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### Common Validation Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@NotNull` | Field cannot be null | `@NotNull String title` |
| `@NotBlank` | String cannot be null, empty, or whitespace | `@NotBlank String title` |
| `@NotEmpty` | Collection/array cannot be null or empty | `@NotEmpty List<String> tags` |
| `@Size` | String/collection size constraints | `@Size(min=1, max=255)` |
| `@Min` / `@Max` | Numeric minimum/maximum | `@Min(0) Integer quantity` |
| `@Pattern` | String matches regex | `@Pattern(regexp="^[A-Z]{2}$")` |
| `@Email` | Valid email format | `@Email String email` |
| `@Valid` | Cascade validation to nested objects | `@Valid Address address` |

### Custom Validation

**Create custom validators for business rules.**

```java
// ✅ GOOD: Custom validator annotation
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StatusTransitionValidator.class)
public @interface ValidStatusTransition {
    String message() default "Invalid status transition";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Validator implementation
public class StatusTransitionValidator 
    implements ConstraintValidator<ValidStatusTransition, StatusTransitionRequest> {
    
    @Autowired
    private TicketStateMachine stateMachine;
    
    @Override
    public boolean isValid(StatusTransitionRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;  // @NotNull handles null check
        
        Ticket currentTicket = // ... fetch from repository
        return stateMachine.isValidTransition(
            currentTicket.getStatus(),
            request.newStatus()
        );
    }
}
```

---

## Exception Handling

### Custom Exceptions

**Create domain-specific exceptions for business rule violations.**

```java
// ✅ GOOD: Domain exception hierarchy
public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(Long id) {
        super("Ticket with id " + id + " not found");
    }
}

public class InvalidStatusTransitionException extends RuntimeException {
    private final TicketStatus from;
    private final TicketStatus to;
    
    public InvalidStatusTransitionException(TicketStatus from, TicketStatus to) {
        super("Cannot transition from " + from + " to " + to);
        this.from = from;
        this.to = to;
    }
    
    public TicketStatus getFrom() { return from; }
    public TicketStatus getTo() { return to; }
}
```

### Global Exception Handler

**Use @RestControllerAdvice for centralized exception handling.**

```java
// ✅ GOOD: Centralized exception handling
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTicketNotFound(
        TicketNotFoundException ex,
        WebRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            Instant.now(),
            request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(
        InvalidStatusTransitionException ex,
        WebRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            ex.getMessage(),
            Instant.now(),
            request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
        MethodArgumentNotValidException ex,
        WebRequest request
    ) {
        List<FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new FieldError(
                error.getField(),
                error.getDefaultMessage()
            ))
            .toList();
        
        ValidationErrorResponse error = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            Instant.now(),
            request.getDescription(false),
            fieldErrors
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        WebRequest request
    ) {
        log.error("Unexpected error", ex);  // Log full stack trace
        
        // Return generic message (don't expose internals)
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred",
            Instant.now(),
            request.getDescription(false)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

**Exception Handling Rules**:
- Throw exceptions in service layer (not HTTP responses)
- Let GlobalExceptionHandler map exceptions to HTTP responses
- Log unexpected exceptions with full stack trace
- Never expose stack traces or internal details to clients
- Use appropriate HTTP status codes

---

## Transactional Boundaries

### Use @Transactional on Service Methods

**Service methods define transactional boundaries.**

```java
// ✅ GOOD: Transaction on service method
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    
    @Transactional  // Write operation: requires transaction
    public TicketDetailResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setStatus(TicketStatus.OPEN);
        
        Ticket savedTicket = ticketRepository.save(ticket);
        return toDetailResponse(savedTicket);
    }
    
    @Transactional(readOnly = true)  // Read-only: optimization hint
    public TicketDetailResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new TicketNotFoundException(id));
        return toDetailResponse(ticket);
    }
    
    @Transactional  // Multiple operations in single transaction
    public CommentResponse addComment(Long ticketId, CreateCommentRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));
        
        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setContent(request.content());
        
        Comment savedComment = commentRepository.save(comment);
        
        // Update ticket timestamp (in same transaction)
        ticket.setUpdatedAt(Instant.now());
        ticketRepository.save(ticket);
        
        return toCommentResponse(savedComment);
    }
}

// ❌ BAD: No transaction on write operation
public TicketDetailResponse createTicket(CreateTicketRequest request) {
    // Without @Transactional, each repository call is separate transaction
    Ticket ticket = new Ticket();
    // ... set fields
    Ticket savedTicket = ticketRepository.save(ticket);  // Transaction 1
    
    Comment comment = new Comment();
    // ... set fields
    commentRepository.save(comment);  // Transaction 2 - could fail leaving orphan ticket!
    
    return toDetailResponse(savedTicket);
}
```

**Transaction Rules**:
- Use `@Transactional` on service methods (not controllers or repositories)
- Default: `readOnly = false` (write operations)
- Use `@Transactional(readOnly = true)` for read-only operations (optimization)
- Avoid transactions spanning multiple service calls (keep granular)
- Let Spring manage transaction boundaries (don't use manual EntityManager)

---

## JPA/Hibernate Guidelines

### Entity Classes

```java
// ✅ GOOD: Well-structured entity
@Entity
@Table(name = "ticket", indexes = {
    @Index(name = "idx_ticket_status", columnList = "status"),
    @Index(name = "idx_ticket_created_at", columnList = "created_at")
})
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)  // ALWAYS use STRING, never ORDINAL
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.OPEN;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Getters and setters...
}
```

### JPA Best Practices

**1. Enums**:
```java
// ✅ GOOD: Use EnumType.STRING
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private TicketStatus status;

// ❌ BAD: EnumType.ORDINAL (fragile, breaks if enum order changes)
@Enumerated(EnumType.ORDINAL)
private TicketStatus status;
```

**2. Relationships**:
```java
// ✅ GOOD: Bidirectional with proper cascade
@Entity
public class Ticket {
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
    
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setTicket(this);  // Maintain bidirectional link
    }
}

@Entity
public class Comment {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
}

// ❌ BAD: CascadeType.ALL without careful consideration
@OneToMany(cascade = CascadeType.ALL)  // Could accidentally delete related entities!
private List<Comment> comments;
```

**3. Lazy Loading**:
```java
// ✅ GOOD: Use LAZY for associations
@ManyToOne(fetch = FetchType.LAZY)
private Ticket ticket;

// ❌ BAD: EAGER loading (N+1 problem)
@ManyToOne(fetch = FetchType.EAGER)  // Loads everything upfront
private Ticket ticket;
```

**4. Timestamps**:
```java
// ✅ GOOD: Use @PrePersist and @PreUpdate
@PrePersist
protected void onCreate() {
    createdAt = Instant.now();
    updatedAt = Instant.now();
}

@PreUpdate
protected void onUpdate() {
    updatedAt = Instant.now();
}

// ✅ ALTERNATIVE: Use Spring Data JPA auditing
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Ticket {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
```

---

## Repository Guidelines

### Use Spring Data JPA

```java
// ✅ GOOD: Extend JpaRepository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    // Method name query derivation
    List<Ticket> findByStatus(TicketStatus status);
    
    Optional<Ticket> findByIdAndStatus(Long id, TicketStatus status);
    
    // Custom query with @Query
    @Query("""
        SELECT t FROM Ticket t
        WHERE (:status IS NULL OR t.status = :status)
        AND (:keyword IS NULL 
             OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY t.createdAt DESC
        """)
    List<Ticket> findByStatusAndKeyword(
        @Param("status") TicketStatus status,
        @Param("keyword") String keyword
    );
    
    // Native query (when JPQL insufficient)
    @Query(value = """
        SELECT * FROM ticket
        WHERE status = :status
        AND created_at > :since
        ORDER BY created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Ticket> findRecentTickets(
        @Param("status") String status,
        @Param("since") Instant since,
        @Param("limit") int limit
    );
}
```

### Repository Best Practices

**1. Method Naming**:
```java
// ✅ GOOD: Descriptive, follows Spring Data conventions
List<Ticket> findByStatusAndPriorityOrderByCreatedAtDesc(TicketStatus status, Priority priority);
Optional<Ticket> findFirstByStatusOrderByCreatedAtAsc(TicketStatus status);
long countByStatus(TicketStatus status);
boolean existsByTitle(String title);

// ❌ BAD: Custom method names without @Query
List<Ticket> getTickets();  // What criteria? Ambiguous
```

**2. Query Optimization**:
```java
// ✅ GOOD: Use projections for read-only views
public interface TicketSummaryProjection {
    Long getId();
    String getTitle();
    TicketStatus getStatus();
    Instant getCreatedAt();
}

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<TicketSummaryProjection> findAllByStatus(TicketStatus status);
}

// ❌ BAD: Fetching full entities when only need few fields
List<Ticket> findAllByStatus(TicketStatus status);  // Loads all fields, relations
```

**3. Keep Repositories Simple**:
```java
// ✅ GOOD: Repository only does data access
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(TicketStatus status);
}

// ❌ BAD: Business logic in repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    default boolean isValidTransition(Ticket ticket, TicketStatus newStatus) {
        // Business logic belongs in service!
        return switch (ticket.getStatus()) {
            case OPEN -> newStatus == TicketStatus.IN_PROGRESS || newStatus == TicketStatus.CANCELLED;
            // ...
        };
    }
}
```

---

## Database Migrations (Flyway)

### Migration File Structure

**Location**: `src/main/resources/db/migration/`

**Naming**: `V{version}__{description}.sql`

**Examples**:
- `V1__create_ticket_table.sql`
- `V2__create_comment_table.sql`
- `V3__add_priority_to_ticket.sql`
- `V4__add_status_index.sql`

### Migration Best Practices

```sql
-- ✅ GOOD: V1__create_ticket_table.sql
CREATE TABLE ticket (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_created_at ON ticket(created_at DESC);

-- ✅ GOOD: V2__create_comment_table.sql
CREATE TABLE comment (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id)
        REFERENCES ticket(id) ON DELETE CASCADE
);

CREATE INDEX idx_comment_ticket_id ON comment(ticket_id);

-- ✅ GOOD: V3__add_priority_to_ticket.sql (idempotent)
ALTER TABLE ticket
ADD COLUMN IF NOT EXISTS priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';

-- ❌ BAD: Non-idempotent migration
ALTER TABLE ticket ADD COLUMN priority VARCHAR(20);  -- Fails if re-run
```

### Migration Rules

1. **Never modify existing migrations** (once applied to any environment)
2. **Always use IF NOT EXISTS / IF EXISTS** where possible
3. **Test migrations on H2 AND PostgreSQL** (H2 for tests, PostgreSQL for production)
4. **Use explicit column types** (don't rely on defaults)
5. **Create indexes for frequently queried columns**
6. **Use foreign key constraints** for referential integrity
7. **Use TIMESTAMP WITH TIME ZONE** for timestamps
8. **Default values in database** (don't rely solely on application defaults)

---

## Logging

### Use SLF4J with Logback

```java
// ✅ GOOD: SLF4J logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class TicketService {
    private static final Logger log = LoggerFactory.getLogger(TicketService.class);
    
    private final TicketRepository ticketRepository;
    
    @Transactional
    public TicketDetailResponse createTicket(CreateTicketRequest request) {
        log.debug("Creating ticket with title: {}", request.title());
        
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setStatus(TicketStatus.OPEN);
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        log.info("Created ticket with id: {}", savedTicket.getId());
        
        return toDetailResponse(savedTicket);
    }
    
    @Transactional
    public void transitionStatus(Long ticketId, TicketStatus newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));
        
        TicketStatus oldStatus = ticket.getStatus();
        
        if (!stateMachine.isValidTransition(oldStatus, newStatus)) {
            log.warn("Invalid status transition attempted: {} -> {} for ticket {}",
                oldStatus, newStatus, ticketId);
            throw new InvalidStatusTransitionException(oldStatus, newStatus);
        }
        
        ticket.setStatus(newStatus);
        ticketRepository.save(ticket);
        
        log.info("Transitioned ticket {} from {} to {}", ticketId, oldStatus, newStatus);
    }
}
```

### Logging Levels

| Level | Use Case | Examples |
|-------|----------|----------|
| **ERROR** | Critical failures requiring immediate attention | Database connection lost, payment processing failed |
| **WARN** | Potentially harmful situations | Invalid input detected, deprecated API used, retry attempts |
| **INFO** | Important business events | User created, order placed, status transition |
| **DEBUG** | Detailed diagnostic information | Method entry/exit, intermediate values, query parameters |
| **TRACE** | Very detailed diagnostic information | Full object dumps, all method calls |

### Logging Best Practices

```java
// ✅ GOOD: Parameterized logging (efficient)
log.debug("Searching tickets with status: {} and keyword: {}", status, keyword);

// ❌ BAD: String concatenation (wastes CPU even if debug disabled)
log.debug("Searching tickets with status: " + status + " and keyword: " + keyword);

// ✅ GOOD: Log exceptions with full stack trace
try {
    ticketRepository.save(ticket);
} catch (DataAccessException e) {
    log.error("Failed to save ticket: {}", ticket.getId(), e);  // Exception as last param
    throw e;
}

// ❌ BAD: Log exception message only (loses stack trace)
catch (DataAccessException e) {
    log.error("Failed to save ticket: " + e.getMessage());  // Stack trace lost!
}

// ✅ GOOD: Don't log sensitive information
log.info("User {} logged in", user.getUsername());

// ❌ BAD: Logging sensitive data
log.info("User logged in with password: {}", password);  // NEVER log passwords!
```

---

## Naming Conventions

### Classes

```java
// ✅ GOOD: Descriptive, follows conventions
@Entity
public class Ticket { }                     // Entity: noun

@RestController
public class TicketController { }           // Controller: noun + Controller suffix

@Service
public class TicketService { }              // Service: noun + Service suffix

public interface TicketRepository { }       // Repository: noun + Repository suffix

public class TicketStateMachine { }         // Business component: descriptive noun

public record CreateTicketRequest(...) { }  // Request DTO: verb + noun + Request
public record TicketDetailResponse(...) { } // Response DTO: noun + adjective + Response

public class TicketNotFoundException { }    // Exception: noun + action + Exception

// ❌ BAD: Generic, unclear names
public class Manager { }
public class Helper { }
public class Util { }
```

### Methods

```java
// ✅ GOOD: Verb-based, describes action
public TicketDetailResponse createTicket(CreateTicketRequest request) { }
public TicketDetailResponse getTicketById(Long id) { }
public void updateTicketStatus(Long id, TicketStatus newStatus) { }
public void deleteTicket(Long id) { }
public List<TicketSummaryResponse> searchTickets(String keyword) { }
public boolean isValidTransition(TicketStatus from, TicketStatus to) { }

// ❌ BAD: Unclear action
public TicketDetailResponse ticket(CreateTicketRequest request) { }
public void process(Long id) { }  // Process what? How?
```

### Variables

```java
// ✅ GOOD: Descriptive, clear intent
Ticket ticket = ticketRepository.findById(id).orElseThrow();
List<Comment> comments = ticket.getComments();
TicketStatus currentStatus = ticket.getStatus();
boolean isValidTransition = stateMachine.isValidTransition(currentStatus, newStatus);

// ❌ BAD: Abbreviated, unclear
Ticket t = repo.findById(id).orElseThrow();
List<Comment> c = t.getComments();
TicketStatus cs = t.getStatus();
boolean ivt = sm.isValidTransition(cs, ns);
```

### Constants

```java
// ✅ GOOD: UPPER_SNAKE_CASE
public static final String DEFAULT_PRIORITY = "MEDIUM";
public static final int MAX_TITLE_LENGTH = 255;
public static final long CACHE_EXPIRY_SECONDS = 3600L;

// ❌ BAD: Wrong case
public static final String defaultPriority = "MEDIUM";
public static final int maxTitleLength = 255;
```

---

## Avoid Unnecessary Abstractions

### YAGNI Principle

**"You Aren't Gonna Need It" — Don't add abstraction layers until you actually need them.**

```java
// ✅ GOOD: Simple, direct implementation
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    
    @Transactional
    public TicketDetailResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setStatus(TicketStatus.OPEN);
        
        Ticket savedTicket = ticketRepository.save(ticket);
        return toDetailResponse(savedTicket);
    }
}

// ❌ BAD: Unnecessary interface layer (unless multiple implementations exist)
public interface TicketService {  // Only one implementation exists!
    TicketDetailResponse createTicket(CreateTicketRequest request);
}

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {  // Unnecessary
    private final TicketRepository ticketRepository;
    
    @Override
    public TicketDetailResponse createTicket(CreateTicketRequest request) {
        // ...
    }
}

// ❌ BAD: Over-engineered factory pattern
public interface TicketFactory {
    Ticket createTicket(CreateTicketRequest request);
}

public class DefaultTicketFactory implements TicketFactory {
    // Unnecessary abstraction for simple object creation
}
```

### When to Use Interfaces

**Use interfaces when**:
- You have multiple implementations (polymorphism)
- You need to mock for testing (though concrete classes work fine too)
- You're defining a public API for external use

**Don't use interfaces when**:
- You have only one implementation
- "Just in case" we need another implementation later (YAGNI)
- Following outdated "every class needs interface" dogma

---

## Clean Code Principles

### Keep Methods Short

```java
// ✅ GOOD: Short, focused methods
@Transactional
public TicketDetailResponse createTicket(CreateTicketRequest request) {
    Ticket ticket = buildTicketFromRequest(request);
    Ticket savedTicket = ticketRepository.save(ticket);
    return toDetailResponse(savedTicket);
}

private Ticket buildTicketFromRequest(CreateTicketRequest request) {
    Ticket ticket = new Ticket();
    ticket.setTitle(request.title());
    ticket.setDescription(request.description());
    ticket.setPriority(request.priority() != null ? request.priority() : Priority.MEDIUM);
    ticket.setStatus(TicketStatus.OPEN);
    return ticket;
}

// ❌ BAD: Long method doing too much
@Transactional
public TicketDetailResponse createTicket(CreateTicketRequest request) {
    // Validation (should be in @Valid annotation)
    if (request.title() == null || request.title().isBlank()) {
        throw new IllegalArgumentException("Title is required");
    }
    if (request.title().length() > 255) {
        throw new IllegalArgumentException("Title too long");
    }
    // ... 20 more lines of validation
    
    // Build entity (should be separate method)
    Ticket ticket = new Ticket();
    ticket.setTitle(request.title());
    ticket.setDescription(request.description());
    // ... 10 more lines
    
    // Save (should be concise)
    Ticket savedTicket = ticketRepository.save(ticket);
    
    // Convert to DTO (should be separate method)
    TicketDetailResponse response = new TicketDetailResponse(/* ... */);
    // ... 15 more lines
    
    return response;
}
```

### Single Responsibility Principle

```java
// ✅ GOOD: Each class has one responsibility
@Service
public class TicketService {
    // Responsible for ticket business logic
}

@Component
public class TicketStateMachine {
    // Responsible for state transition validation
}

@Component
public class TicketNotificationService {
    // Responsible for sending notifications
}

// ❌ BAD: God class doing everything
@Service
public class TicketService {
    public void createTicket() { }
    public void updateTicket() { }
    public void deleteTicket() { }
    public void validateStateTransition() { }  // Should be separate component
    public void sendEmailNotification() { }   // Should be separate component
    public void generateReport() { }          // Should be separate component
}
```

### Readable Code > Clever Code

```java
// ✅ GOOD: Clear and explicit
public boolean isValidTransition(TicketStatus from, TicketStatus to) {
    Set<TicketStatus> allowedTransitions = ALLOWED_TRANSITIONS.get(from);
    if (allowedTransitions == null) {
        return false;  // No transitions allowed from this state
    }
    return allowedTransitions.contains(to);
}

// ❌ BAD: Clever but hard to understand
public boolean isValidTransition(TicketStatus from, TicketStatus to) {
    return Optional.ofNullable(ALLOWED_TRANSITIONS.get(from))
        .map(set -> set.contains(to))
        .orElse(false);  // Why is this better? It's not.
}
```

---

## Code Review Checklist

Before submitting code, verify:

### Architecture & Design
- [ ] Follows layered architecture (Controller → Service → Repository)
- [ ] Business logic in service layer (not controller)
- [ ] DTOs used for API contracts (entities not exposed)
- [ ] Constructor injection used (not field injection)
- [ ] No unnecessary abstractions (interfaces only when needed)

### Java & Spring Boot
- [ ] Java 21 features used appropriately (records, pattern matching, switch expressions)
- [ ] `@Transactional` on service methods (write operations)
- [ ] `@Transactional(readOnly = true)` on read-only methods
- [ ] Validation annotations on request DTOs
- [ ] `@Valid` on controller method parameters

### JPA & Database
- [ ] Entities use `@Enumerated(EnumType.STRING)` (never ORDINAL)
- [ ] Relationships use `FetchType.LAZY` (avoid EAGER)
- [ ] Flyway migrations created for schema changes
- [ ] Migration files follow naming convention: `V{version}__{description}.sql`
- [ ] Indexes created for frequently queried columns

### Exception Handling
- [ ] Domain exceptions thrown from service layer
- [ ] GlobalExceptionHandler maps exceptions to HTTP responses
- [ ] Appropriate HTTP status codes used
- [ ] Human-readable error messages provided
- [ ] Stack traces NOT exposed to clients

### Logging
- [ ] SLF4J logger used (not System.out or printStackTrace)
- [ ] Parameterized logging used (not string concatenation)
- [ ] Appropriate log levels (ERROR, WARN, INFO, DEBUG)
- [ ] No sensitive information logged (passwords, tokens)
- [ ] Exceptions logged with full stack trace

### Code Quality
- [ ] Methods are short and focused (single responsibility)
- [ ] Descriptive names (classes, methods, variables)
- [ ] No magic numbers (use constants)
- [ ] No code duplication (DRY principle)
- [ ] Comments explain "why", not "what"

### Testing (Separate Document)
- [ ] Unit tests for service methods
- [ ] Integration tests for controllers
- [ ] Property-based tests for business rules
- [ ] Test coverage for edge cases and error conditions

---

## Anti-Patterns to Avoid

### ❌ God Classes
**Problem**: One class doing too much
**Solution**: Split into focused, single-responsibility classes

### ❌ Anemic Domain Model
**Problem**: Entities with only getters/setters, all logic in services
**Solution**: Add behavior to entities where appropriate (but keep persistence separate)

### ❌ Service Layer Bypass
**Problem**: Controllers calling repositories directly
**Solution**: Always go through service layer for business logic

### ❌ Primitive Obsession
**Problem**: Using primitives instead of value objects
**Solution**: Create record types for domain concepts (e.g., `EmailAddress`, `PhoneNumber`)

### ❌ Exception Swallowing
**Problem**: Catching exceptions without logging or rethrowing
**Solution**: Log exceptions with full context, rethrow or wrap appropriately

### ❌ N+1 Query Problem
**Problem**: Fetching entities in loop causing many queries
**Solution**: Use `JOIN FETCH` in JPQL or projections to fetch in single query

---

## Summary

**Key Principles**:
1. **Layered Architecture**: Controller → Service → Repository → Database
2. **DTOs Everywhere**: Never expose JPA entities in API
3. **Constructor Injection**: Always, everywhere
4. **Validation**: Jakarta Bean Validation on DTOs
5. **Transactions**: On service methods, readOnly where applicable
6. **Enums**: Always STRING, never ORDINAL
7. **Exceptions**: Domain exceptions + GlobalExceptionHandler
8. **Logging**: SLF4J with appropriate levels
9. **Naming**: Descriptive, conventional, consistent
10. **Simplicity**: No unnecessary abstractions (YAGNI)

**Remember**: These guidelines complement the PROJECT_CONSTITUTION.md. When in doubt, consult both documents.

---

**Version**: 1.0  
**Last Updated**: 2026-09-23  
**Maintained By**: Project Team
