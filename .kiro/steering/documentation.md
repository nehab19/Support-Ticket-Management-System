---
inclusion: auto
---

# Documentation Guidelines

**Project**: Support Ticket Management System  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Purpose

These guidelines establish comprehensive documentation standards for the Support Ticket Management System. All documentation—whether written by humans or AI—must follow these conventions.

**Scope**: Code documentation, API docs, README files, architectural documentation  
**Authority**: Complements PROJECT_CONSTITUTION.md with documentation-specific guidance

---

## Core Principles

1. **Document Why, Not What** — Code shows what; docs explain why
2. **Keep It Current** — Outdated docs are worse than no docs
3. **Write for Humans** — Clear, concise, helpful
4. **Examples Over Descriptions** — Show, don't just tell
5. **Progressive Disclosure** — Start simple, add details as needed

---

## Code Documentation

### JavaDoc Standards

**When to Use JavaDoc**:
- All public classes, interfaces, and enums
- All public methods
- Complex private methods that need explanation
- Constants with non-obvious purposes

**When NOT to Use JavaDoc**:
- Self-explanatory getters/setters
- Overridden methods (unless behavior differs)
- Test methods (use descriptive names instead)

### Class-Level JavaDoc

```java
/**
 * Service for managing support tickets through their lifecycle.
 * 
 * <p>This service handles ticket creation, updates, status transitions,
 * and retrieval. All status transitions are validated by {@link TicketStateMachine}
 * before being persisted.</p>
 * 
 * <p>Business rules enforced:
 * <ul>
 *   <li>New tickets always start with status {@link TicketStatus#OPEN}</li>
 *   <li>Only valid state transitions are allowed (see {@link TicketStateMachine})</li>
 *   <li>Partial updates preserve untouched fields</li>
 * </ul>
 * 
 * @see TicketStateMachine
 * @see Ticket
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class TicketService {
    // Implementation
}
```

### Method-Level JavaDoc

**Good Examples**:
```java
/**
 * Creates a new support ticket with the provided details.
 * 
 * <p>The ticket is created with status {@link TicketStatus#OPEN} and
 * timestamps are automatically set. If no priority is provided,
 * defaults to {@link Priority#MEDIUM}.
 * 
 * @param request the ticket creation request containing title, description, and optional priority
 * @return the created ticket with generated ID and timestamps
 * @throws IllegalArgumentException if request is null or contains invalid data
 * @see CreateTicketRequest
 */
@Transactional
public TicketDetailResponse createTicket(CreateTicketRequest request) {
    // Implementation
}

/**
 * Transitions a ticket to a new status.
 * 
 * <p>The transition is validated by {@link TicketStateMachine} before
 * being persisted. Only the following transitions are allowed:
 * <ul>
 *   <li>OPEN → IN_PROGRESS</li>
 *   <li>IN_PROGRESS → RESOLVED</li>
 *   <li>RESOLVED → CLOSED</li>
 *   <li>OPEN → CANCELLED</li>
 *   <li>IN_PROGRESS → CANCELLED</li>
 * </ul>
 * 
 * @param ticketId the ID of the ticket to transition
 * @param newStatus the target status
 * @return the updated ticket with new status
 * @throws TicketNotFoundException if ticket with given ID doesn't exist
 * @throws InvalidStatusTransitionException if transition is not allowed
 * @see TicketStateMachine#validateTransition
 */
@Transactional
public TicketDetailResponse transitionStatus(Long ticketId, TicketStatus newStatus) {
    // Implementation
}
```

**Bad Examples**:
```java
// ❌ BAD: Restating method signature
/**
 * Gets a ticket by ID.
 * @param id the id
 * @return the ticket
 */
public Ticket getTicket(Long id) { }

// ❌ BAD: No useful information
/**
 * Updates the ticket.
 */
public void updateTicket(Ticket ticket) { }
```

### Parameter Documentation

**Use @param Tags**:
```java
/**
 * @param title the ticket title, must be 1-255 characters, not blank
 * @param status the target status, must be a valid TicketStatus enum value
 * @param id the ticket identifier, must correspond to an existing ticket
 */
```

**Don't State the Obvious**:
```java
// ❌ BAD: Obvious from parameter name/type
/**
 * @param title the title
 * @param id the id
 */
```

### Return Value Documentation

**Use @return Tags**:
```java
/**
 * @return the created ticket with generated ID and timestamps, never null
 * @return empty Optional if no ticket with given ID exists
 * @return list of matching tickets ordered by createdAt descending, empty list if no matches
 */
```

### Exception Documentation

**Document All Thrown Exceptions**:
```java
/**
 * @throws TicketNotFoundException if ticket with given ID doesn't exist
 * @throws InvalidStatusTransitionException if the requested transition violates state machine rules
 * @throws IllegalArgumentException if request is null or contains invalid data
 */
```

---

## Inline Comments

### When to Use Inline Comments

**Good Reasons**:
- Explain complex algorithms or business logic
- Document workarounds for known issues
- Clarify non-obvious decisions
- Provide context for external constraints

**Bad Reasons**:
- Restate what the code does (code should be self-explanatory)
- Apologize for bad code (refactor instead)
- Comment out code (use version control)

### Good Inline Comments

```java
// ✅ GOOD: Explains WHY
// We use ILIKE instead of full-text search because H2 (test database)
// doesn't support PostgreSQL's tsvector. This is acceptable for v1 scale.
String query = """
    SELECT t FROM Ticket t
    WHERE LOWER(t.title) LIKE LOWER(:keyword)
    """;

// ✅ GOOD: Documents business rule
// Terminal states (CLOSED, CANCELLED) cannot transition to any other state.
// This is a business requirement to prevent reopening finalized tickets.
if (currentStatus == CLOSED || currentStatus == CANCELLED) {
    throw new InvalidStatusTransitionException(currentStatus, newStatus);
}

// ✅ GOOD: Explains workaround
// Thread.sleep is used here to ensure distinct timestamps for testing.
// In production, timestamps have sufficient precision naturally.
Thread.sleep(10);
```

### Bad Inline Comments

```java
// ❌ BAD: States the obvious
// Increment counter
counter++;

// ❌ BAD: Repeats code
// Get ticket by id
Ticket ticket = ticketRepository.findById(id);

// ❌ BAD: Apology instead of fix
// FIXME: This is a hack, should refactor later
// TODO: Clean this up
```

---

## README Documentation

### Project README Structure

```markdown
# Support Ticket Management System

Brief description of the project (1-2 sentences).

## Features

- Bullet list of key features
- What makes this project useful

## Tech Stack

- Java 21
- Spring Boot 3.x
- PostgreSQL / H2
- React / Next.js

## Getting Started

### Prerequisites

- JDK 21+
- Maven 3.8+
- PostgreSQL 15+ (or use H2 for testing)

### Installation

```bash
# Clone repository
git clone https://github.com/org/support-ticket-system.git

# Navigate to backend
cd backend

# Run tests
./mvnw test

# Run application
./mvnw spring-boot:run
```

### Configuration

Environment variables required:
- `DB_URL`: Database connection string
- `DB_USERNAME`: Database user
- `DB_PASSWORD`: Database password

See `.env.example` for complete list.

## Usage

Examples of common operations...

## Testing

```bash
# Run all tests
./mvnw test

# Run specific test
./mvnw test -Dtest=TicketServiceTest
```

## Contributing

See CONTRIBUTING.md for guidelines.

## License

MIT License - see LICENSE file.
```

### Component-Level README

For complex components:
```markdown
# Component Name

## Purpose

What this component does and why it exists.

## Architecture

Diagram or description of internal structure.

## Usage

```java
// Example code
TicketService service = new TicketService(...);
service.createTicket(request);
```

## Configuration

Any configuration options or environment variables.

## Testing

How to test this component.
```

---

## API Documentation

### Endpoint Documentation Format

**In Code (Controller)**:
```java
/**
 * Creates a new support ticket.
 * 
 * <p>Request body must include:
 * <ul>
 *   <li>title: 1-255 characters, required</li>
 *   <li>description: 1-5000 characters, required</li>
 *   <li>priority: LOW|MEDIUM|HIGH|CRITICAL, optional (defaults to MEDIUM)</li>
 * </ul>
 * 
 * <p>Example request:
 * <pre>{@code
 * POST /api/tickets
 * Content-Type: application/json
 * 
 * {
 *   "title": "Login page error",
 *   "description": "Users cannot log in",
 *   "priority": "HIGH"
 * }
 * }</pre>
 * 
 * <p>Success response (201 Created):
 * <pre>{@code
 * {
 *   "id": 1,
 *   "title": "Login page error",
 *   "status": "OPEN",
 *   "priority": "HIGH",
 *   "createdAt": "2024-06-01T10:00:00Z"
 * }
 * }</pre>
 * 
 * @param request the ticket creation request
 * @return the created ticket with HTTP 201 status
 */
@PostMapping
public ResponseEntity<TicketDetailResponse> createTicket(
    @Valid @RequestBody CreateTicketRequest request
) {
    // Implementation
}
```

### API Documentation File

**Location**: `spec/api-contract.md`

**Structure**:
```markdown
## Endpoint: Create Ticket

**URL**: `POST /api/tickets`

**Request Body**:
```json
{
  "title": "string (required, 1-255 chars)",
  "description": "string (required)",
  "priority": "enum (optional): LOW|MEDIUM|HIGH|CRITICAL"
}
```

**Success Response** (201 Created):
```json
{
  "id": 1,
  "title": "Login page error",
  "status": "OPEN",
  "priority": "HIGH",
  "createdAt": "2024-06-01T10:00:00Z"
}
```

**Error Responses**:
- 400 Bad Request: Validation failure
- 500 Internal Server Error: Unexpected error
```

---

## Architecture Documentation

### Architecture Decision Records (ADRs)

**Location**: `docs/architecture-decisions.md`

**Format**:
```markdown
## ADR-XXX: Decision Title

**Status**: Proposed | Accepted | Deprecated | Superseded  
**Date**: YYYY-MM-DD

### Context

What factors are at play? What problem are we solving?

### Decision

What did we decide to do?

### Consequences

**Positive**:
- Benefit 1
- Benefit 2

**Negative**:
- Tradeoff 1
- Tradeoff 2
```

**Example**:
```markdown
## ADR-001: Use Separate Status Transition Endpoint

**Status**: Accepted  
**Date**: 2026-09-23

### Context

Status transitions are governed by a strict state machine. We need to
decide whether to handle status changes through the generic update
endpoint or create a separate endpoint.

### Decision

We will create a separate endpoint: PATCH /tickets/{id}/status

### Consequences

**Positive**:
- State machine enforcement is explicit
- Different error codes (400 vs 422)
- Easier to audit transitions

**Negative**:
- One more endpoint to maintain
```

---

## Specification Documents

### Requirements Documentation

**Location**: `spec/requirements.md`

**Format**:
```markdown
### Requirement N: Feature Name

**User Story**: As a [role], I want [capability], so that [benefit].

#### Acceptance Criteria

1. WHEN [condition], THE [system] SHALL [behavior]
2. IF [condition], THEN THE [component] SHALL [behavior]
3. WHERE [condition], THE [system] SHALL [behavior]

**Priority**: High | Medium | Low  
**Validates**: [Related property or test]
```

### Design Documentation

**Location**: `spec/architecture.md`, `spec/data-model.md`, etc.

**Key Sections**:
- Overview
- Architecture diagrams
- Component descriptions
- Design decisions with rationale
- Examples

---

## Test Documentation

### Test Class Documentation

```java
/**
 * Unit tests for {@link TicketService}.
 * 
 * <p>Tests cover:
 * <ul>
 *   <li>Ticket creation with valid and invalid inputs</li>
 *   <li>Status transitions with state machine validation</li>
 *   <li>Partial updates preserving untouched fields</li>
 *   <li>Error handling for not-found and validation failures</li>
 * </ul>
 * 
 * <p>Uses Mockito to mock repository and state machine dependencies.
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    // Tests
}
```

### Test Method Naming

**Use Descriptive Names** (No JavaDoc needed):
```java
// ✅ GOOD: Self-documenting test names
@Test
void createTicket_validRequest_returnsTicketWithOpenStatus() { }

@Test
void createTicket_blankTitle_throwsValidationException() { }

@Test
void transitionStatus_fromOpenToClosed_throwsInvalidTransitionException() { }

// ❌ BAD: Vague names
@Test
void testCreateTicket() { }

@Test
void test1() { }
```

### Property Test Documentation

```java
/**
 * Property-based tests for ticket creation invariants.
 * 
 * <p>Validates Property 1 from design document: For any valid
 * CreateTicketRequest, the created ticket SHALL have:
 * <ul>
 *   <li>status == OPEN</li>
 *   <li>non-null id</li>
 *   <li>non-null createdAt</li>
 * </ul>
 * 
 * <p>Runs 100 iterations with randomly generated inputs.
 */
class TicketCreationPropertyTest {
    
    // Feature: support-ticket-management, Property 1: Ticket creation invariants
    @Property(tries = 100)
    void ticketCreationInvariants(...) { }
}
```

---

## Markdown Standards

### Heading Hierarchy

```markdown
# Document Title (H1) — Only one per document

## Major Section (H2)

### Subsection (H3)

#### Minor Subsection (H4)

##### Detail (H5) — Rarely needed
```

### Code Blocks

**Always Specify Language**:
````markdown
```java
public class Example {
    // Code
}
```

```json
{
  "key": "value"
}
```

```bash
./mvnw test
```
````

### Tables

**Use for Structured Data**:
```markdown
| Column 1 | Column 2 | Column 3 |
|----------|----------|----------|
| Value 1  | Value 2  | Value 3  |
| Value 4  | Value 5  | Value 6  |
```

### Lists

**Bullet Lists** (unordered):
```markdown
- Item 1
- Item 2
  - Nested item
- Item 3
```

**Numbered Lists** (ordered):
```markdown
1. First step
2. Second step
3. Third step
```

---

## Documentation Maintenance

### Review Schedule

| Document Type | Review Frequency | Owner |
|---------------|------------------|-------|
| README | Every release | Tech Lead |
| API Docs | When API changes | Backend Team |
| Architecture Docs | Quarterly | Architect |
| Code Comments | During code review | Author + Reviewer |
| ADRs | When decision made | Decision makers |

### Deprecation Notices

**Mark Outdated Sections**:
```markdown
> **⚠️ DEPRECATED**: This approach is deprecated as of v2.0.
> Use [new approach](#new-section) instead.
```

### Version Tracking

**Include Version in Docs**:
```markdown
---
**Version**: 1.0  
**Last Updated**: 2026-09-23  
**Status**: Active
---
```

---

## Tools

### Documentation Generation

**JavaDoc Generation**:
```bash
./mvnw javadoc:javadoc
# Output: target/site/apidocs/
```

**OpenAPI / Swagger** (Future):
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```

Access at: `http://localhost:8080/swagger-ui.html`

### Markdown Linting

**Use markdownlint** for consistent formatting:
```bash
npm install -g markdownlint-cli
markdownlint **/*.md
```

---

## Examples

### Complete Class Documentation

```java
/**
 * Validates and enforces ticket status transitions according to business rules.
 * 
 * <p>The state machine allows only 5 transitions out of 25 possible combinations:
 * <ul>
 *   <li>OPEN → IN_PROGRESS (start work)</li>
 *   <li>OPEN → CANCELLED (cancel before work)</li>
 *   <li>IN_PROGRESS → RESOLVED (complete work)</li>
 *   <li>IN_PROGRESS → CANCELLED (cancel during work)</li>
 *   <li>RESOLVED → CLOSED (verify and finalize)</li>
 * </ul>
 * 
 * <p>All other transitions are rejected with {@link InvalidStatusTransitionException}.
 * Terminal states (CLOSED, CANCELLED) cannot transition to any other state.
 * 
 * <p>Example usage:
 * <pre>{@code
 * TicketStateMachine machine = new TicketStateMachine();
 * 
 * // Check if transition is valid
 * boolean isValid = machine.isValidTransition(OPEN, IN_PROGRESS);  // true
 * 
 * // Validate and throw if invalid
 * machine.validateTransition(OPEN, CLOSED);  // throws InvalidStatusTransitionException
 * 
 * // Get all valid next states
 * Set<TicketStatus> nextStates = machine.getAllowedNextStates(OPEN);  // [IN_PROGRESS, CANCELLED]
 * }</pre>
 * 
 * @see TicketStatus
 * @see InvalidStatusTransitionException
 * @since 1.0
 */
@Component
public class TicketStateMachine {
    
    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
        TicketStatus.OPEN, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
        TicketStatus.IN_PROGRESS, Set.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED),
        TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED),
        TicketStatus.CLOSED, Set.of(),      // Terminal state
        TicketStatus.CANCELLED, Set.of()    // Terminal state
    );
    
    /**
     * Checks whether a status transition is valid.
     * 
     * @param from the current status
     * @param to the target status
     * @return true if transition is allowed, false otherwise
     * @throws IllegalArgumentException if from or to is null
     */
    public boolean isValidTransition(TicketStatus from, TicketStatus to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Status values cannot be null");
        }
        if (from == to) {
            return false;  // No self-transitions
        }
        return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }
    
    // Additional methods...
}
```

---

## Documentation Checklist

Before merging code:

- [ ] Public classes have JavaDoc
- [ ] Public methods have JavaDoc with @param, @return, @throws
- [ ] Complex logic has inline comments explaining WHY
- [ ] README is up to date
- [ ] API changes documented in spec/api-contract.md
- [ ] Architecture changes have ADR
- [ ] Examples are provided for non-trivial features
- [ ] No spelling/grammar errors
- [ ] Code snippets are syntactically correct
- [ ] Links are valid (no 404s)

---

**Version**: 1.0  
**Last Updated**: 2026-09-23
