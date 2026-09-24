---
inclusion: manual
---

# Code Review Skill

**Skill Name**: Code Review  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Purpose

This skill provides a structured code review workflow following the project's constitutional requirements, architectural principles, and coding standards. Use this skill to ensure all code—whether AI-generated or human-written—meets project quality standards.

**Scope**: Backend (Java/Spring Boot) and general code quality  
**Authority**: Enforces PROJECT_CONSTITUTION.md and steering file guidelines

---

## When to Use This Skill

Invoke this skill (`#code-review`) when:

- Reviewing AI-generated code before accepting
- Conducting pull request reviews
- Performing self-reviews before committing
- Validating refactored code
- Ensuring compliance with constitutional requirements

---

## Review Workflow

### Step 1: Architecture Compliance

**Check layered architecture adherence:**

**Controllers**:
- [ ] Only handle HTTP concerns (request/response mapping)
- [ ] No business logic in controllers
- [ ] Use DTOs for all requests and responses
- [ ] Never expose JPA entities directly
- [ ] Proper HTTP status codes (200, 201, 400, 404, 422, 500)
- [ ] Use @Valid for input validation

**Services**:
- [ ] Business logic lives in service layer
- [ ] State machine validation present (if applicable)
- [ ] Input validation with Bean Validation
- [ ] No HTTP concerns (no HttpServletRequest, status codes)
- [ ] Proper exception handling
- [ ] Transactional boundaries defined (@Transactional)

**Repositories**:
- [ ] Only data access logic
- [ ] No business rules in repositories
- [ ] Custom queries use @Query when needed
- [ ] Proper return types (Optional for findBy*)

---

### Step 2: Security Review

**Critical security checks:**

- [ ] No secrets, passwords, or API keys in code
- [ ] No credentials in comments or logs
- [ ] Input validation present on all user inputs
- [ ] SQL injection prevention (parameterized queries, no string concatenation)
- [ ] No stack traces exposed to clients
- [ ] Sensitive data not logged
- [ ] Environment variables used for configuration

**Red Flags**:
- Hardcoded passwords or tokens
- Direct string concatenation in queries
- Raw exceptions returned to clients
- Logging of passwords or tokens

---

### Step 3: Code Quality

**Java/Spring Boot standards:**

- [ ] Constructor injection used (not @Autowired on fields)
- [ ] Classes have single responsibility
- [ ] Methods are focused and concise (<30 lines)
- [ ] Proper access modifiers (private by default)
- [ ] No magic numbers (use constants)
- [ ] Enums use @Enumerated(EnumType.STRING) not ORDINAL
- [ ] Proper null handling (Optional or validation)
- [ ] No code duplication

**Naming conventions:**
- [ ] Classes: PascalCase
- [ ] Methods/variables: camelCase
- [ ] Constants: UPPER_SNAKE_CASE
- [ ] Package names: lowercase

---

### Step 4: Error Handling

**Exception handling standards:**

- [ ] GlobalExceptionHandler catches all exceptions
- [ ] Consistent error response format:
  ```json
  {
    "status": 400,
    "message": "Human-readable message",
    "timestamp": "2026-09-23T10:00:00Z",
    "path": "/api/tickets"
  }
  ```
- [ ] Appropriate HTTP status codes:
  - 400: Validation failure
  - 404: Resource not found
  - 422: Business rule violation (state transition)
  - 500: Unexpected errors
- [ ] Validation errors include field details
- [ ] No stack traces in responses
- [ ] Errors logged with context

---

### Step 5: Testing Review

**Test coverage verification:**

**Unit Tests**:
- [ ] Service methods have unit tests
- [ ] Test names descriptive: `methodName_condition_expectedResult`
- [ ] Given-When-Then structure
- [ ] Mocks used for external dependencies
- [ ] AssertJ used for assertions
- [ ] Edge cases covered

**Integration Tests**:
- [ ] REST endpoints have integration tests
- [ ] @SpringBootTest with H2 database
- [ ] MockMvc for endpoint testing
- [ ] @Transactional for test isolation
- [ ] Success and error scenarios tested

**Property-Based Tests** (if applicable):
- [ ] Business logic has property tests
- [ ] Minimum 100 iterations (@Property(tries = 100))
- [ ] Tagged with feature and property number
- [ ] State machine has property tests

**Test Quality**:
- [ ] Tests are independent (no shared mutable state)
- [ ] Tests are fast (<100ms for unit tests)
- [ ] No flaky tests (deterministic)
- [ ] Test data uses builders/fixtures

---

### Step 6: Documentation Review

**Code documentation:**

- [ ] Public APIs have JavaDoc
- [ ] JavaDoc explains purpose and behavior
- [ ] Parameters and return values documented
- [ ] Exceptions documented with @throws
- [ ] Inline comments explain WHY not WHAT
- [ ] Complex logic has explanatory comments

**API documentation:**
- [ ] New endpoints documented in spec/api-contract.md
- [ ] Request/response schemas defined
- [ ] Error responses documented
- [ ] Status codes mapped

---

### Step 7: State Machine Validation (Tickets Only)

**If code touches ticket status:**

- [ ] TicketStateMachine validates all transitions
- [ ] Only 5 allowed transitions implemented:
  - OPEN → IN_PROGRESS
  - OPEN → CANCELLED
  - IN_PROGRESS → RESOLVED
  - IN_PROGRESS → CANCELLED
  - RESOLVED → CLOSED
- [ ] Terminal states (CLOSED, CANCELLED) have no outbound transitions
- [ ] Invalid transitions throw InvalidStatusTransitionException (HTTP 422)
- [ ] Property tests verify state machine correctness

---

### Step 8: Data Persistence Review

**Database and JPA standards:**

- [ ] Entities have proper annotations (@Entity, @Table)
- [ ] Primary keys defined (@Id, @GeneratedValue)
- [ ] Relationships mapped correctly (@OneToMany, @ManyToOne)
- [ ] Cascade types appropriate
- [ ] Timestamps present (createdAt, updatedAt)
- [ ] Flyway migrations exist for schema changes
- [ ] Migration files follow naming: V{N}__{description}.sql
- [ ] No schema changes in application code (use migrations)

---

## Common AI Mistakes to Watch For

### 1. Skipping Business Rule Validation

**❌ Bad**:
```java
public TicketDetailResponse createTicket(CreateTicketRequest request) {
    Ticket ticket = new Ticket();
    ticket.setTitle(request.title());
    ticket.setStatus(TicketStatus.OPEN);
    return ticketRepository.save(ticket);
}
```

**✅ Good**:
```java
public TicketDetailResponse createTicket(CreateTicketRequest request) {
    validateTicketRequest(request);  // Validation present
    
    Ticket ticket = new Ticket();
    ticket.setTitle(request.title());
    ticket.setStatus(TicketStatus.OPEN);
    return ticketRepository.save(ticket);
}
```

---

### 2. Exposing JPA Entities

**❌ Bad**:
```java
@GetMapping("/{id}")
public Ticket getTicket(@PathVariable Long id) {
    return ticketRepository.findById(id).orElseThrow();
}
```

**✅ Good**:
```java
@GetMapping("/{id}")
public TicketDetailResponse getTicket(@PathVariable Long id) {
    Ticket ticket = ticketRepository.findById(id)
        .orElseThrow(() -> new TicketNotFoundException(id));
    return TicketMapper.toDetailResponse(ticket);
}
```

---

### 3. Field Injection

**❌ Bad**:
```java
@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;
}
```

**✅ Good**:
```java
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
}
```

---

### 4. Missing @Transactional

**❌ Bad**:
```java
public void updateTicket(Long id, UpdateTicketRequest request) {
    Ticket ticket = ticketRepository.findById(id).orElseThrow();
    ticket.setTitle(request.title());
    ticketRepository.save(ticket);
}
```

**✅ Good**:
```java
@Transactional
public TicketDetailResponse updateTicket(Long id, UpdateTicketRequest request) {
    Ticket ticket = ticketRepository.findById(id)
        .orElseThrow(() -> new TicketNotFoundException(id));
    ticket.setTitle(request.title());
    return TicketMapper.toDetailResponse(ticketRepository.save(ticket));
}
```

---

### 5. EnumType.ORDINAL

**❌ Bad**:
```java
@Enumerated(EnumType.ORDINAL)  // Breaks if enum order changes
private TicketStatus status;
```

**✅ Good**:
```java
@Enumerated(EnumType.STRING)  // Safe, human-readable
private TicketStatus status;
```

---

## Review Checklist Summary

Use this quick checklist for every review:

### Critical (Must Fix)
- [ ] No secrets or credentials in code
- [ ] Business rules in service layer only
- [ ] DTOs for all API contracts (never entities)
- [ ] State machine validates transitions
- [ ] Tests exist and pass

### Important (Should Fix)
- [ ] Constructor injection used
- [ ] Proper exception handling
- [ ] JavaDoc on public APIs
- [ ] @Transactional on write operations
- [ ] EnumType.STRING for enums

### Nice to Have (Consider)
- [ ] Property tests for business logic
- [ ] Inline comments for complex logic
- [ ] Test coverage >80%
- [ ] No code duplication

---

## After Review Actions

### If Issues Found

1. **Document the issue** in docs/ai-review.md (if AI-generated)
2. **Request changes** with specific guidance
3. **Provide examples** of correct implementation
4. **Re-review** after fixes applied

### If Review Passes

1. **Approve** the code
2. **Update documentation** if needed
3. **Run tests** one more time
4. **Commit** with descriptive message

---

## Review Report Template

```markdown
## Code Review: [Feature/File Name]

**Reviewer**: [Your Name]  
**Date**: YYYY-MM-DD  
**Files Reviewed**: [List of files]

### Summary
[Brief overview of changes]

### Architecture Compliance: ✅ / ❌
[Notes on architecture adherence]

### Security: ✅ / ❌
[Security findings]

### Code Quality: ✅ / ❌
[Quality assessment]

### Testing: ✅ / ❌
[Test coverage and quality]

### Documentation: ✅ / ❌
[Documentation completeness]

### Issues Found
1. [Issue 1 with severity: Critical/Major/Minor]
2. [Issue 2]

### Recommendation
- [ ] Approve
- [ ] Approve with minor changes
- [ ] Request changes
- [ ] Reject

### Notes
[Additional comments]
```

---

## Quick Reference

| Check | Category | Severity |
|-------|----------|----------|
| No secrets in code | Security | Critical |
| Business rules in service | Architecture | Critical |
| DTOs not entities | Architecture | Critical |
| State machine validation | Business Rules | Critical |
| Tests exist | Testing | Major |
| Constructor injection | Code Quality | Major |
| JavaDoc present | Documentation | Minor |

---

**Skill Version**: 1.0  
**Last Updated**: 2026-09-23  
**Related Documents**: PROJECT_CONSTITUTION.md, .kiro/steering/java-springboot.md, .kiro/steering/testing.md

