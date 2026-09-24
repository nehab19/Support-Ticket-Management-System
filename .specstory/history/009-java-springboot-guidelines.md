# Prompt 009: Java/Spring Boot Coding Guidelines

**Date**: 2026-09-23  
**Session**: 2  
**Tool**: Kiro AI

---

## User Prompt

```
Create .kiro/steering/java-springboot.md
Define reusable coding guidelines for this project that AI assistants 
should automatically follow when generating code.
```

---

## Context

After establishing PROJECT_CONSTITUTION.md (Prompt 008), the project needed detailed implementation guidelines that AI would automatically follow.

**Requirements**:
- Auto-included in AI context (`inclusion: auto`)
- Comprehensive Java/Spring Boot standards
- Good and bad examples
- Integration with PROJECT_CONSTITUTION.md
- Practical, actionable rules

---

## AI Response

### Created: `.kiro/steering/java-springboot.md`

**Size**: ~600 lines

**Purpose**: Establish coding standards that AI assistants automatically follow

---

## Content Overview

### 1. Java 21 Modern Features
- Records for DTOs
- Pattern matching
- Switch expressions
- Text blocks for SQL/JSON
- Sealed classes

### 2. Spring Boot Best Practices
- Component scanning
- Dependency injection (constructor only)
- Application properties organization
- Profile management

### 3. Layered Architecture Standards

**Controller Layer**:
```java
// ✅ GOOD: HTTP concerns only
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    
    @GetMapping("/{id}")
    public TicketDetailResponse getTicket(@PathVariable Long id) {
        return ticketService.getTicket(id);
    }
}

// ❌ BAD: Business logic in controller
@GetMapping("/{id}")
public Ticket getTicket(@PathVariable Long id) {
    Ticket ticket = ticketRepository.findById(id).orElseThrow();
    if (ticket.getStatus() == TicketStatus.CLOSED) {
        // Business logic doesn't belong here!
    }
    return ticket;  // Exposing entity!
}
```

**Service Layer**:
```java
// ✅ GOOD: Business logic in service
@Service
@RequiredArgsConstructor
@Transactional
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketStateMachine stateMachine;
    
    public TicketDetailResponse transitionStatus(Long id, TicketStatus newStatus) {
        Ticket ticket = findTicketById(id);
        stateMachine.validateTransition(ticket.getStatus(), newStatus);
        ticket.setStatus(newStatus);
        return TicketMapper.toDetailResponse(ticketRepository.save(ticket));
    }
}
```

### 4. DTOs vs Entities

**Critical Rule**: NEVER expose JPA entities

```java
// ✅ GOOD: DTO for API response
public record TicketDetailResponse(
    Long id,
    String title,
    String description,
    TicketStatus status,
    Priority priority,
    Instant createdAt,
    List<CommentResponse> comments
) {}

// ❌ BAD: Exposing entity
@GetMapping("/{id}")
public Ticket getTicket(@PathVariable Long id) {
    return ticketRepository.findById(id).orElseThrow();  // DON'T!
}
```

### 5. Constructor Injection

```java
// ✅ GOOD: Constructor injection with Lombok
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final CommentService commentService;
}

// ❌ BAD: Field injection
@Service
public class TicketService {
    @Autowired  // DON'T!
    private TicketRepository ticketRepository;
}
```

### 6. Validation

```java
// ✅ GOOD: Jakarta Bean Validation
public record CreateTicketRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title too long")
    String title,
    
    @NotBlank(message = "Description is required")
    @Size(max = 5000)
    String description,
    
    @NotNull
    Priority priority
) {}

// Controller
@PostMapping
public TicketDetailResponse create(@Valid @RequestBody CreateTicketRequest request) {
    return ticketService.createTicket(request);
}
```

### 7. Exception Handling

```java
// ✅ GOOD: GlobalExceptionHandler
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TicketNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, ex.getMessage(), Instant.now(), null));
    }
    
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorResponse(422, ex.getMessage(), Instant.now(), null));
    }
}
```

### 8. Transaction Management

```java
// ✅ GOOD: @Transactional on write operations
@Service
@RequiredArgsConstructor
public class TicketService {
    
    @Transactional(readOnly = true)
    public TicketDetailResponse getTicket(Long id) {
        // Read-only for performance
    }
    
    @Transactional
    public TicketDetailResponse updateTicket(Long id, UpdateTicketRequest request) {
        // Write operation
    }
}
```

### 9. JPA/Hibernate Standards

```java
// ✅ GOOD: Enum as STRING
@Entity
@Table(name = "ticket")
public class Ticket {
    
    @Enumerated(EnumType.STRING)  // ALWAYS STRING!
    @Column(nullable = false)
    private TicketStatus status;
    
    @Enumerated(EnumType.STRING)
    private Priority priority;
}

// ❌ BAD: Enum as ORDINAL
@Enumerated(EnumType.ORDINAL)  // DON'T! Breaks if enum order changes
private TicketStatus status;
```

### 10. Flyway Migrations

```sql
-- V1__create_ticket_table.sql
CREATE TABLE ticket (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(5000) NOT NULL,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_priority ON ticket(priority);
```

**Rules**:
- Version format: `V{N}__{description}.sql`
- Never modify existing migrations
- Always test on H2 and PostgreSQL
- Idempotent when possible

---

## Deliverables

**Created**: `.kiro/steering/java-springboot.md` (~600 lines)

**Content Includes**:
- 10 major guideline categories
- 30+ code examples (good and bad)
- Complete architecture patterns
- Testing guidelines reference
- Code review checklist

---

## Impact Assessment

**Immediate Impact**:
- ✅ AI will automatically follow these standards
- ✅ Consistent code generation
- ✅ Fewer code review issues
- ✅ Reduced AI mistakes

**Long-Term Impact**:
- Higher code quality
- Easier maintenance
- Better onboarding (reference for humans too)
- Consistent patterns across project

**Integration**:
- Complements PROJECT_CONSTITUTION.md
- Referenced by `.kiro/steering/testing.md` (Prompt 011)
- Used by all code-generating skills

---

## Next Steps

1. ✅ Steering file created and active
2. ⏸️ Test AI code generation with new guidelines
3. ⏸️ Create additional steering files (API, testing, docs)
4. ⏸️ Update based on usage feedback

---

## Lessons Learned

**What Went Well**:
- Comprehensive coverage with examples
- Good/bad examples clarify intent
- Integration with constitution
- Practical, actionable guidelines

**Key Takeaway**:
- Steering files dramatically improve AI code quality
- `inclusion: auto` ensures guidelines always active
- Examples are more valuable than rules alone
- Project-specific context makes guidelines effective

---

## References

- **PROJECT_CONSTITUTION.md** - Supreme governance
- `.kiro/steering/` - Steering file directory
- Spring Boot documentation
- Java 21 documentation

---

**Prompt Number**: 009  
**Status**: Complete  
**Related Prompts**: 
- Prompt 008: Project constitution
- Prompt 010: Project structure migration (next)
