# Design Document — Support Ticket Management System

## Overview

The Support Ticket Management System is a full-stack web application for creating, tracking, and managing support tickets. The backend is a Spring Boot 3 REST API backed by PostgreSQL (H2 for tests), schema-managed with Flyway. The frontend is a React / Next.js single-page app that communicates exclusively through the REST API. A strictly enforced state machine governs all ticket-status transitions, and a centralised exception handler ensures consistent, structured error responses across the API.

### Goals

- Provide a reliable CRUD lifecycle for tickets and comments.
- Enforce a predictable ticket state machine with clear rejection semantics (HTTP 422).
- Support keyword search (case-insensitive, title + description) and status filtering.
- Keep secrets out of the repository; surface them only through environment variables.
- Make the system straightforward to test: in-memory H2 for the unit/integration test suite, real PostgreSQL for production.

---

## Architecture

The system follows a layered, three-tier architecture:

```
┌─────────────────────────────────────────┐
│          React / Next.js  (UI)           │  Browser
└────────────────┬────────────────────────┘
                 │  HTTP / JSON
┌────────────────▼────────────────────────┐
│         Spring Boot REST API             │  JVM / Java 21
│  ┌──────────┐  ┌──────────┐  ┌────────┐ │
│  │Controller│  │ Service  │  │  Repo  │ │
│  └──────────┘  └──────────┘  └───┬────┘ │
│                                   │      │
└───────────────────────────────────┼──────┘
                                    │  JDBC / JPA
           ┌────────────────────────▼──────────────┐
           │  PostgreSQL (prod) / H2 (test)          │
           └───────────────────────────────────────┘
```

### Layer responsibilities

| Layer | Responsibility |
|---|---|
| Controller | Parse HTTP requests, delegate to Service, serialize responses |
| Service | Business logic: validation, state-machine enforcement, orchestration |
| Repository | Data access via Spring Data JPA |
| Database | Persistence; schema managed by Flyway |
| UI | Present data, capture user actions, call REST API |

### Key design decisions

- **Separate status-transition endpoint** (`PATCH /tickets/{id}/status`) — keeps field-update concerns distinct from lifecycle concerns and makes state-machine enforcement obvious.
- **Single global exception handler** (`@RestControllerAdvice`) — maps every known exception type to a consistent JSON error body; unknown exceptions map to HTTP 500 without leaking stack traces.
- **Spring profiles for datasource** — `test` profile activates H2, default/prod profile reads PostgreSQL credentials from environment variables. No credentials appear in committed property files.
- **Flyway for migrations** — migration scripts are versioned SQL files included in the source tree; Flyway applies them automatically on startup.

---

## Components and Interfaces

### REST Endpoints

#### Tickets

| Method | Path | Description | Success |
|---|---|---|---|
| `POST` | `/api/tickets` | Create ticket | 201 |
| `GET` | `/api/tickets` | List all tickets (optional `?status=` filter, optional `?q=` search) | 200 |
| `GET` | `/api/tickets/{id}` | Get ticket detail with comments | 200 |
| `PATCH` | `/api/tickets/{id}` | Update ticket fields | 200 |
| `PATCH` | `/api/tickets/{id}/status` | Transition ticket status | 200 |

#### Comments

| Method | Path | Description | Success |
|---|---|---|---|
| `POST` | `/api/tickets/{ticketId}/comments` | Add comment to ticket | 201 |

#### Search and Filter

Search (`?q=keyword`) and status filter (`?status=OPEN`) are query parameters on `GET /api/tickets`. Providing both simultaneously narrows results by both criteria.

---

### Request / Response Shapes

**Create Ticket — Request**
```json
{
  "title": "Login page throws 500",
  "description": "Reproducible on Chrome 124. Steps: ...",
  "priority": "HIGH"
}
```

**Ticket — Response (list item)**
```json
{
  "id": 1,
  "title": "Login page throws 500",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": null,
  "createdAt": "2024-06-01T10:00:00Z"
}
```

**Ticket — Response (detail)**
```json
{
  "id": 1,
  "title": "Login page throws 500",
  "description": "Reproducible on Chrome 124. Steps: ...",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": null,
  "createdAt": "2024-06-01T10:00:00Z",
  "updatedAt": "2024-06-01T10:00:00Z",
  "comments": [
    {
      "id": 1,
      "author": "alice",
      "body": "Investigating now.",
      "createdAt": "2024-06-01T11:00:00Z"
    }
  ]
}
```

**Update Ticket — Request (partial)**
```json
{
  "title": "Login page throws 500 on Chrome",
  "assignee": "alice"
}
```

**Status Transition — Request**
```json
{
  "status": "IN_PROGRESS"
}
```

**Create Comment — Request**
```json
{
  "author": "alice",
  "body": "Investigating now."
}
```

**Error Response**
```json
{
  "status": 422,
  "message": "Transition from OPEN to CLOSED is not allowed",
  "errors": []
}
```
For validation errors, `errors` is an array of field-level messages:
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    { "field": "title", "message": "must not be blank" }
  ]
}
```

---

### Spring Boot Component Map

```
com.example.supportticket
├── controller
│   ├── TicketController          # CRUD + status transition
│   └── CommentController         # POST /tickets/{id}/comments
├── service
│   ├── TicketService             # Business logic, orchestration
│   ├── CommentService            # Comment persistence
│   └── TicketStateMachine        # Validates status transitions
├── repository
│   ├── TicketRepository          # Spring Data JPA
│   └── CommentRepository         # Spring Data JPA
├── model
│   ├── Ticket                    # JPA entity
│   ├── Comment                   # JPA entity
│   ├── TicketStatus              # Enum: OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED
│   └── Priority                  # Enum: LOW, MEDIUM, HIGH, CRITICAL
├── dto
│   ├── CreateTicketRequest
│   ├── UpdateTicketRequest
│   ├── StatusTransitionRequest
│   ├── CreateCommentRequest
│   ├── TicketSummaryResponse
│   ├── TicketDetailResponse
│   └── CommentResponse
├── exception
│   ├── TicketNotFoundException
│   ├── InvalidStatusTransitionException
│   └── GlobalExceptionHandler    # @RestControllerAdvice
└── config
    └── SecurityConfig            # (placeholder; no auth required in v1)
```

---

### State Machine

The `TicketStateMachine` component encodes the allowed transitions as a static map and throws `InvalidStatusTransitionException` (HTTP 422) for any other transition attempt.

```
OPEN ──────────► IN_PROGRESS ──► RESOLVED ──► CLOSED
  │                  │
  └──► CANCELLED ◄───┘
```

Allowed transitions table:

| From | To |
|---|---|
| OPEN | IN_PROGRESS |
| OPEN | CANCELLED |
| IN_PROGRESS | RESOLVED |
| IN_PROGRESS | CANCELLED |
| RESOLVED | CLOSED |

All other transitions (including any transition from CLOSED or CANCELLED) are rejected.

---

### Frontend Component Map

```
pages/
  index.tsx                 # Ticket list page
  tickets/[id].tsx          # Ticket detail page

components/
  TicketTable.tsx            # Tabular ticket list with sort
  TicketCard.tsx             # Card-layout ticket summary
  CreateTicketForm.tsx       # Modal/drawer for ticket creation
  UpdateTicketForm.tsx       # Edit ticket fields inline
  StatusTransitionPanel.tsx  # Shows valid next-status actions
  CommentList.tsx            # Chronological comment display
  CommentForm.tsx            # Add comment form
  SearchBar.tsx              # Keyword search input
  StatusFilter.tsx           # Status dropdown filter
  ErrorNotification.tsx      # Displays API error messages

lib/
  api.ts                    # Typed API client (fetch wrappers)
  types.ts                  # Shared TypeScript types
```

---

## Data Models

### Entity: Ticket

| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGINT` | PK, generated |
| `title` | `VARCHAR(255)` | NOT NULL |
| `description` | `TEXT` | NOT NULL |
| `priority` | `VARCHAR(20)` | NOT NULL, default `'MEDIUM'` |
| `status` | `VARCHAR(20)` | NOT NULL, default `'OPEN'` |
| `assignee` | `VARCHAR(255)` | NULL |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | NOT NULL, default now() |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | NOT NULL, default now() |

### Entity: Comment

| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGINT` | PK, generated |
| `ticket_id` | `BIGINT` | FK → ticket(id), NOT NULL |
| `author` | `VARCHAR(255)` | NOT NULL |
| `body` | `TEXT` | NOT NULL |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | NOT NULL, default now() |

### Indexes

- `ticket(status)` — supports status filter queries.
- `ticket(created_at DESC)` — supports default ordering.
- Full-text / ILIKE search on `title` and `description` is performed with `LOWER(title) LIKE LOWER(:keyword)` or a PostgreSQL `ILIKE` expression; no additional index is required at v1 scale but a `GIN` index on a `tsvector` column is noted as a future optimisation.

### Flyway Migration

```
src/main/resources/db/migration/
  V1__create_ticket_table.sql
  V2__create_comment_table.sql
```

### JPA Entity (Ticket — abbreviated)

```java
@Entity
@Table(name = "ticket")
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.OPEN;

    @Column(length = 255)
    private String assignee;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    void onCreate() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }
}
```

### Spring Profile — Datasource

`application.properties` (committed, no secrets):
```properties
spring.application.name=support-ticket-management
```

`application-prod.properties` (committed, no secrets):
```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

`application-test.properties` (committed, safe):
```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Ticket creation invariants

*For any* valid `CreateTicketRequest` (non-empty title ≤ 255 chars, non-empty description, valid or absent priority), calling `createTicket()` SHALL produce a ticket where `status == OPEN`, `id` is non-null, and `createdAt` is non-null.

**Validates: Requirements 1.1**

---

### Property 2: Unique ticket identifiers

*For any* N valid ticket creation requests submitted sequentially or concurrently, the resulting N tickets SHALL each have a distinct `id`.

**Validates: Requirements 1.2**

---

### Property 3: Title length validation (create and update)

*For any* string whose length exceeds 255 characters submitted as `title` in either a creation or an update request, the API SHALL reject the request with HTTP 400.

**Validates: Requirements 1.5, 4.3**

---

### Property 4: Priority enum validation (create and update)

*For any* string that is not one of `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` submitted as `priority` in either a creation or an update request, the API SHALL reject the request with HTTP 400.

**Validates: Requirements 1.6, 4.4**

---

### Property 5: Ticket list ordering

*For any* set of N tickets with distinct `createdAt` timestamps, the list returned by `GET /api/tickets` SHALL be ordered such that for every adjacent pair, `list[i].createdAt >= list[i+1].createdAt` (descending).

**Validates: Requirements 2.1, 8.1**

---

### Property 6: Ticket list field completeness

*For any* ticket in the system, its representation in the list response (including search and filter results) SHALL include all of `id`, `title`, `priority`, `status`, `assignee`, and `createdAt`.

**Validates: Requirements 2.2, 7.4**

---

### Property 7: Partial update preserves untouched fields

*For any* existing ticket and any partial update request containing a non-empty strict subset of `{title, description, priority, assignee}`, the API SHALL update only the specified fields and leave all other fields unchanged.

**Validates: Requirements 4.5**

---

### Property 8: State machine — allowed transitions succeed; disallowed transitions are rejected with HTTP 422

*For any* `(from, to)` status pair: if the pair is in `{(OPEN, IN_PROGRESS), (OPEN, CANCELLED), (IN_PROGRESS, RESOLVED), (IN_PROGRESS, CANCELLED), (RESOLVED, CLOSED)}` the transition SHALL succeed with HTTP 200; for every other pair the State_Machine SHALL reject the request with HTTP 422 and the error message SHALL contain both the `from` and `to` status names.

**Validates: Requirements 5.1, 5.2, 5.3**

---

### Property 9: Comment creation round-trip

*For any* existing ticket and any valid `CreateCommentRequest` (non-empty `author`, non-empty `body`), the created comment SHALL have a non-null `id`, a non-null `createdAt`, and `author` / `body` values that exactly match the request.

**Validates: Requirements 6.1**

---

### Property 10: Keyword search correctness

*For any* keyword and any corpus of tickets, the search response SHALL contain exactly the set of tickets whose `title` or `description` contains the keyword using a case-insensitive match — no more, no fewer — or an empty array when no tickets match.

**Validates: Requirements 7.1, 7.2**

---

### Property 11: Blank keyword rejection

*For any* string composed entirely of whitespace (including the empty string) submitted as the search keyword `q`, the API SHALL return HTTP 400.

**Validates: Requirements 7.3**

---

### Property 12: Status filter correctness

*For any* valid `TicketStatus` value and any corpus of tickets with mixed statuses, the filter response SHALL contain only tickets whose `status` equals the requested value, ordered by `createdAt` descending, or an empty array when none match.

**Validates: Requirements 8.1, 8.3**

---

### Property 13: Error response completeness

*For any* request that fails validation (HTTP 400), not-found (HTTP 404), or a business rule violation (HTTP 422), the response body SHALL contain a `message` field with a human-readable description, a `status` field matching the HTTP response code, and — for HTTP 400 responses with multiple violations — an `errors` array that includes an entry for every failing field.

**Validates: Requirements 10.1, 10.2**

---

## Error Handling

### Exception-to-HTTP mapping

| Exception | HTTP Status | Notes |
|---|---|---|
| `MethodArgumentNotValidException` (Bean Validation) | 400 | Collect all field errors into `errors` array |
| `ConstraintViolationException` | 400 | Collect all constraint violations |
| `TicketNotFoundException` | 404 | Message: "Ticket {id} not found" |
| `InvalidStatusTransitionException` | 422 | Message: "Transition from {from} to {to} is not allowed" |
| `MethodArgumentTypeMismatchException` (e.g. bad enum) | 400 | Descriptive message; no stack trace |
| `Exception` (catch-all) | 500 | Generic message; stack trace suppressed |

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
            .toList();
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(400, "Validation failed", errors));
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TicketNotFoundException ex) {
        return ResponseEntity.status(404)
            .body(new ErrorResponse(404, ex.getMessage(), List.of()));
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex) {
        return ResponseEntity.status(422)
            .body(new ErrorResponse(422, ex.getMessage(), List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        // log ex internally; do NOT include ex.getMessage() in response
        return ResponseEntity.status(500)
            .body(new ErrorResponse(500, "An unexpected error occurred", List.of()));
    }
}
```

### Error Response DTO

```java
public record ErrorResponse(int status, String message, List<FieldError> errors) {}
public record FieldError(String field, String message) {}
```

---

## Testing Strategy

### Overview

The testing strategy uses two complementary approaches:
- **Unit / integration tests** — specific examples, edge cases, error conditions; run against H2 with the `test` Spring profile.
- **Property-based tests** — universal properties from the Correctness Properties section above; run with minimum 100 iterations per property.

### Property-Based Testing Library

The project uses **[jqwik](https://jqwik.net/)** for property-based testing on the JVM (Java 21 compatible, integrates with JUnit 5).

**Dependency** (Maven):
```xml
<dependency>
    <groupId>net.jqwik</groupId>
    <artifactId>jqwik</artifactId>
    <version>1.8.4</version>
    <scope>test</scope>
</dependency>
```

Each property-based test is tagged with a comment referencing the design property:
```java
// Feature: support-ticket-management, Property 1: Ticket creation invariants
@Property(tries = 100)
void ticketCreationInvariants(...) { ... }
```

### Property Tests (one test per property)

| Property # | Test class | Description |
|---|---|---|
| 1 | `TicketCreationPropertyTest` | Valid creation always produces OPEN + non-null id/createdAt |
| 2 | `TicketIdUniquenessPropertyTest` | N creations produce N distinct ids |
| 3 | `TitleLengthValidationPropertyTest` | Title > 255 chars always returns HTTP 400 |
| 4 | `PriorityValidationPropertyTest` | Invalid priority string always returns HTTP 400 |
| 5 | `TicketListOrderingPropertyTest` | List always in descending createdAt order |
| 6 | `TicketListFieldCompletenessPropertyTest` | List/search/filter entries always have all required fields |
| 7 | `PartialUpdatePropertyTest` | Partial update preserves untouched fields |
| 8 | `StateMachinePropertyTest` | Allowed transitions succeed; disallowed return HTTP 422 with both status names |
| 9 | `CommentCreationPropertyTest` | Created comment matches request + has id + createdAt |
| 10 | `KeywordSearchPropertyTest` | Search returns exactly the case-insensitive-matching tickets |
| 11 | `BlankKeywordPropertyTest` | Blank/whitespace keyword always returns HTTP 400 |
| 12 | `StatusFilterPropertyTest` | Filter returns only matching-status tickets in descending order |
| 13 | `ErrorResponseCompletenessPropertyTest` | All error responses have message, status, and full errors array |

### Unit / Integration Tests

| Test class | What it covers |
|---|---|
| `TicketControllerTest` | `@WebMvcTest` — HTTP layer, request parsing, response serialisation |
| `TicketServiceTest` | Service logic with mocked repository |
| `TicketStateMachineTest` | All 25 (from, to) pairs; explicit examples for each allowed and disallowed transition |
| `CommentServiceTest` | Comment creation, not-found propagation |
| `GlobalExceptionHandlerTest` | Each exception type → correct HTTP status + body |
| `TicketRepositoryTest` | `@DataJpaTest` against H2: CRUD, ordering, ILIKE search |
| `TicketIntegrationTest` | `@SpringBootTest` full-stack happy paths and error paths |
| `FlywayMigrationTest` | Asserts Flyway reports zero pending migrations on startup |

### Test Configuration

- Spring profile `test` activates H2; no external database required.
- `@SpringBootTest` tests use `SpringBootTest.WebEnvironment.RANDOM_PORT`.
- `@DataJpaTest` tests use embedded H2 with Flyway migrations applied.
- Sensitive properties (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) are read from environment variables; test profile uses H2 credentials defined in `application-test.properties`.

### Frontend Testing

- **Jest + React Testing Library** — component unit tests for `TicketTable`, `StatusTransitionPanel` (renders only valid actions), `ErrorNotification`, `SearchBar`.
- **Cypress** (or Playwright) — E2E smoke tests for the create-ticket flow, status transition flow, and search flow.
