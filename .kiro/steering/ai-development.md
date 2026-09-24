---
inclusion: auto
---

# AI-Assisted Development Guidelines

**Project**: Support Ticket Management System  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Purpose

These guidelines establish best practices for AI-assisted development in the Support Ticket Management System. They define how humans and AI collaborate effectively while maintaining code quality, security, and architectural integrity.

**Scope**: AI code generation, review process, mistake documentation, prompt preservation  
**Authority**: Complements PROJECT_CONSTITUTION.md with AI-specific guidance

---

## Core Principles

1. **Human Oversight Required** — All AI-generated code must be reviewed by humans
2. **AI is a Tool, Not a Decision Maker** — Humans make architectural and design decisions
3. **Trust but Verify** — AI suggestions are helpful but not infallible
4. **Document Learnings** — Track patterns in AI mistakes to improve over time
5. **Preserve Context** — Important AI interactions are valuable project artifacts

---

## AI Usage Policy

### When to Use AI

**✅ Appropriate Uses**:
- Generate boilerplate code (DTOs, entities, basic CRUD)
- Write unit tests for well-defined behavior
- Create property-based tests from correctness properties
- Generate documentation and JavaDoc
- Suggest implementation approaches
- Debug errors and exceptions
- Refactor code for clarity
- Generate SQL migrations from entity changes
- Write integration tests for endpoints

**⚠️ Use with Caution**:
- Complex business logic with edge cases
- State machine implementations
- Security-sensitive code (authentication, authorization)
- Database schema migrations (verify carefully)
- Performance-critical code

**❌ Don't Use AI For**:
- Making architectural decisions without human input
- Committing code without review
- Production deployments
- Handling secrets or credentials

---

## AI Code Review Process

### Mandatory Review Steps

**Every piece of AI-generated code MUST go through these steps:**

1. **Read the Code**
   - Understand what the code does
   - Check for logical errors
   - Verify it matches requirements

2. **Test the Code**
   - Run all existing tests
   - Write new tests if needed
   - Verify behavior manually

3. **Check Security**
   - No hardcoded secrets
   - Proper input validation
   - No SQL injection vulnerabilities
   - Appropriate error handling

4. **Verify Architecture**
   - Follows layered architecture
   - Uses DTOs (not entities in controllers)
   - Follows naming conventions
   - Adheres to PROJECT_CONSTITUTION.md

5. **Review Performance**
   - No obvious inefficiencies
   - Appropriate database queries
   - No N+1 query problems

6. **Check Documentation**
   - Public methods have JavaDoc
   - Complex logic has comments
   - API endpoints are documented

7. **Record Issues**
   - Document significant mistakes in `docs/ai-review.md`
   - Note patterns for future reference

### Review Checklist Template

```markdown
## AI Code Review: [Feature Name]

**Date**: YYYY-MM-DD  
**AI Tool**: [Cursor/Copilot/ChatGPT/etc.]  
**Reviewer**: [Name]

### Generated Code
- [ ] Code is syntactically correct
- [ ] Logical errors checked and fixed
- [ ] Matches requirements

### Testing
- [ ] All tests pass
- [ ] New tests added for new functionality
- [ ] Edge cases covered

### Security
- [ ] No hardcoded secrets
- [ ] Input validation present
- [ ] No injection vulnerabilities
- [ ] Error handling appropriate

### Architecture
- [ ] Follows layered architecture
- [ ] Uses DTOs correctly
- [ ] Naming conventions followed
- [ ] Complies with constitution

### Documentation
- [ ] JavaDoc complete
- [ ] Inline comments where needed
- [ ] API docs updated

### Issues Found
- Issue 1: [Description and resolution]
- Issue 2: [Description and resolution]

### Verdict
☑ Approved | ☐ Needs Revision | ☐ Rejected
```

---

## Common AI Mistakes

### Mistake Category 1: Skipping Business Rule Validation

**Problem**: AI generates endpoints that skip critical validation

**Example**:
```java
// ❌ BAD: AI-generated code without state machine validation
@PatchMapping("/{id}/status")
public ResponseEntity<TicketDetailResponse> transitionStatus(
    @PathVariable Long id,
    @RequestBody StatusTransitionRequest request
) {
    Ticket ticket = ticketRepository.findById(id).orElseThrow();
    ticket.setStatus(request.status());  // No validation!
    ticketRepository.save(ticket);
    return ResponseEntity.ok(toDetailResponse(ticket));
}
```

**Fix**:
```java
// ✅ GOOD: Validate transition before persisting
@PatchMapping("/{id}/status")
public ResponseEntity<TicketDetailResponse> transitionStatus(
    @PathVariable Long id,
    @RequestBody StatusTransitionRequest request
) {
    Ticket ticket = ticketRepository.findById(id).orElseThrow();
    
    // MUST validate state machine rules
    stateMachine.validateTransition(ticket.getStatus(), request.status());
    
    ticket.setStatus(request.status());
    ticketRepository.save(ticket);
    return ResponseEntity.ok(toDetailResponse(ticket));
}
```

**Lesson**: Always verify that AI includes business rule validation, especially for state machines.

---

### Mistake Category 2: Exposing JPA Entities

**Problem**: AI returns entities directly from controllers

**Example**:
```java
// ❌ BAD: Exposing entity
@GetMapping("/{id}")
public ResponseEntity<Ticket> getTicket(@PathVariable Long id) {
    Ticket ticket = ticketRepository.findById(id).orElseThrow();
    return ResponseEntity.ok(ticket);  // Entity exposed!
}
```

**Fix**:
```java
// ✅ GOOD: Use DTO
@GetMapping("/{id}")
public ResponseEntity<TicketDetailResponse> getTicket(@PathVariable Long id) {
    TicketDetailResponse response = ticketService.getTicketById(id);
    return ResponseEntity.ok(response);  // DTO, not entity
}
```

**Lesson**: AI often takes shortcuts. Always verify DTOs are used for API contracts.

---

### Mistake Category 3: Field Injection

**Problem**: AI uses field injection instead of constructor injection

**Example**:
```java
// ❌ BAD: Field injection
@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;  // Field injection
    
    @Autowired
    private TicketStateMachine stateMachine;
}
```

**Fix**:
```java
// ✅ GOOD: Constructor injection
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketStateMachine stateMachine;
}
```

**Lesson**: AI may use older patterns. Always enforce constructor injection.

---

### Mistake Category 4: Missing @Transactional

**Problem**: AI forgets transactional boundaries on write operations

**Example**:
```java
// ❌ BAD: No @Transactional on write operation
public TicketDetailResponse createTicket(CreateTicketRequest request) {
    Ticket ticket = new Ticket();
    ticket.setTitle(request.title());
    Ticket saved = ticketRepository.save(ticket);
    
    // If this fails, ticket is already saved (inconsistent state)
    Comment comment = new Comment();
    commentRepository.save(comment);
    
    return toDetailResponse(saved);
}
```

**Fix**:
```java
// ✅ GOOD: @Transactional ensures atomic operation
@Transactional
public TicketDetailResponse createTicket(CreateTicketRequest request) {
    Ticket ticket = new Ticket();
    ticket.setTitle(request.title());
    Ticket saved = ticketRepository.save(ticket);
    
    Comment comment = new Comment();
    commentRepository.save(comment);
    
    return toDetailResponse(saved);  // Both or neither saved
}
```

**Lesson**: Verify transactional boundaries, especially for multi-step operations.

---

### Mistake Category 5: EnumType.ORDINAL

**Problem**: AI uses ordinal enum mapping instead of string

**Example**:
```java
// ❌ BAD: Ordinal enum (fragile)
@Enumerated(EnumType.ORDINAL)
@Column(nullable = false)
private TicketStatus status;
```

**Fix**:
```java
// ✅ GOOD: String enum (safe)
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private TicketStatus status;
```

**Lesson**: AI may not know project-specific conventions. Always check enum mappings.

---

## Prompt Engineering

### Effective Prompts for Code Generation

**❌ Vague Prompts**:
```
"Create a ticket service"
"Add validation"
"Make it work"
```

**✅ Specific Prompts**:
```
"Create a TicketService class with constructor injection of TicketRepository 
and TicketStateMachine. Include a createTicket method that:
1. Takes CreateTicketRequest DTO
2. Creates ticket with status OPEN
3. Sets default priority to MEDIUM if not provided
4. Returns TicketDetailResponse DTO
Follow the existing architecture in TicketController."
```

**✅ Context-Rich Prompts**:
```
"Based on the state machine defined in PROJECT_CONSTITUTION.md (5 allowed 
transitions), implement the transitionStatus method in TicketService. 
The method must:
1. Validate transition using TicketStateMachine
2. Throw InvalidStatusTransitionException for forbidden transitions
3. Return HTTP 422 with both status names in error message
4. Use @Transactional annotation"
```

### Including Constraints

**Always Specify**:
- Architecture to follow (layered, DTOs, etc.)
- Validation requirements (Bean Validation)
- Testing expectations (unit tests, property tests)
- Error handling (GlobalExceptionHandler)
- Documentation (JavaDoc)

**Example**:
```
"Create a REST endpoint for ticket creation following these constraints:
- Use @PostMapping with /api/tickets
- Accept CreateTicketRequest DTO with @Valid
- Return 201 Created with Location header
- Use constructor injection for TicketService
- Add JavaDoc documenting request/response format
- Include unit test with MockMvc"
```

---

## AI-Generated Test Code

### Test Quality Checklist

When AI generates tests:

- [ ] Test names are descriptive
- [ ] Uses AssertJ for assertions (not JUnit)
- [ ] Mocks only external dependencies
- [ ] Uses test data builders/fixtures
- [ ] Covers happy path and error cases
- [ ] Property tests have ≥100 iterations
- [ ] Property tests reference design document property number
- [ ] Integration tests use @Transactional for rollback
- [ ] No flaky tests (time-dependent, order-dependent)

### Common Test Issues

**Issue 1: Testing Implementation Details**:
```java
// ❌ BAD: Testing private methods
@Test
void testValidateTitle() {
    Method method = TicketService.class.getDeclaredMethod("validateTitle");
    method.setAccessible(true);
    // ...
}
```

**Issue 2: Over-Mocking**:
```java
// ❌ BAD: Mocking value objects
CreateTicketRequest mockRequest = mock(CreateTicketRequest.class);
when(mockRequest.title()).thenReturn("Title");
```

**Issue 3: Poor Test Names**:
```java
// ❌ BAD: Vague names
@Test void test1() { }
@Test void testTicket() { }
```

---

## Documenting AI Mistakes

### When to Document

**Document these mistakes**:
- **Critical**: Security vulnerabilities, data loss risks
- **Major**: Business rule violations, architectural violations
- **Patterns**: Repeated mistakes indicating misunderstanding

**Don't Document**:
- Minor syntax errors (missing semicolons, typos)
- Formatting issues (whitespace, indentation)
- Easily caught issues (compile errors)

### Documentation Format

**Location**: `docs/ai-review.md`

**Template**:
```markdown
## YYYY-MM-DD: [Mistake Title]

**AI Tool**: [Cursor/Copilot/ChatGPT/etc.]  
**Task**: [What was being implemented]  
**Issue**: [What went wrong]  
**Impact**: [Potential consequences if not caught]  
**Resolution**: [How it was fixed]  
**Lesson**: [What to watch for in future]

**Code Example**:
```java
// BAD: AI-generated code
// ...

// GOOD: Corrected code
// ...
```
```

---

## Preserving Important Prompts

### What Qualifies as "Important"

Document prompts that:
- Generate significant code (>100 lines)
- Define architecture or design
- Resolve complex bugs
- Establish patterns or conventions
- Create specs or documentation
- Make technical decisions

### Documentation Locations

**1. Summary** (`docs/prompt-history.md`):
```markdown
## YYYY-MM-DD: [Prompt Title]

**Prompt Number**: NNN  
**File**: `.specstory/history/NNN-description.md`  
**Tool**: [AI Tool Name]  
**Purpose**: [Brief description]  
**Outcome**: [What was created]  
**Files Changed**: [List of files]  
**Notes**: [Any important context]
```

**2. Full Details** (`.specstory/history/NNN-description.md`):
```markdown
# Prompt NNN: [Title]

**Date**: YYYY-MM-DD  
**Session**: N  
**Tool**: [AI Tool]

## User Prompt
```
[Full prompt text]
```

## Context
[Background, rationale, constraints]

## AI Response
[What was generated, decisions made]

## Deliverables
- file1.java (100 lines)
- file2.java (50 lines)

## Impact Assessment
[Immediate and long-term impacts]

## Next Steps
[Follow-up actions]

## Lessons Learned
[Key takeaways]
```

---

## AI Tool Comparison

### Tool Strengths

| Tool | Best For | Limitations |
|------|----------|-------------|
| **GitHub Copilot** | Inline suggestions, auto-completion | Limited context window |
| **Cursor** | Full-file generation, refactoring | May miss project conventions |
| **ChatGPT/Claude** | Complex explanations, architecture | Manual copy-paste needed |
| **Kiro AI** | Spec-driven development, full context | Project-specific |

### Choosing the Right Tool

**Use GitHub Copilot for**:
- Writing tests following existing patterns
- Generating boilerplate DTOs
- Auto-completing method implementations

**Use Cursor for**:
- Creating entire new classes
- Refactoring large code sections
- Multi-file changes

**Use ChatGPT/Claude for**:
- Explaining complex concepts
- Designing architecture
- Code review feedback

**Use Kiro AI for**:
- Executing spec-driven workflows
- Creating requirements and design docs
- Following project constitution

---

## Continuous Improvement

### Weekly Review

Every week, review:
- New entries in `docs/ai-review.md`
- Common patterns in AI mistakes
- Prompts that worked well vs. poorly

### Update Guidelines

When patterns emerge:
1. Add to this document (ai-development.md)
2. Update steering files with new examples
3. Share learnings with team

### Training AI

**Provide Context**:
- Reference PROJECT_CONSTITUTION.md
- Point to existing code examples
- Include relevant steering files
- Reference spec documents

**Example**:
```
"Implement ticket creation following the patterns in TicketController 
and TicketService. Ensure compliance with PROJECT_CONSTITUTION.md 
section 4 (layered architecture) and use DTOs as shown in 
.kiro/steering/java-springboot.md"
```

---

## Red Flags

### Code Review Red Flags

Stop and investigate if AI-generated code:
- ❌ Doesn't compile
- ❌ Has no tests
- ❌ Exposes entities from controllers
- ❌ Uses field injection
- ❌ Has hardcoded values or secrets
- ❌ Doesn't match existing architecture
- ❌ Has TODO or FIXME comments
- ❌ Includes commented-out code
- ❌ Has obvious security issues

### Trust Your Instincts

If something feels wrong:
1. Don't merge it
2. Investigate thoroughly
3. Ask for human review
4. Regenerate with better prompt
5. Write manually if needed

---

## Best Practices Summary

### DO:
- ✅ Review all AI-generated code
- ✅ Test before committing
- ✅ Document significant mistakes
- ✅ Preserve important prompts
- ✅ Provide context in prompts
- ✅ Verify architecture compliance
- ✅ Check for security issues
- ✅ Add tests for AI-generated code

### DON'T:
- ❌ Blindly trust AI output
- ❌ Skip code review
- ❌ Commit without testing
- ❌ Ignore repeated mistakes
- ❌ Use vague prompts
- ❌ Deploy without human verification
- ❌ Hardcode secrets
- ❌ Violate project conventions

---

## Resources

### Internal Documentation
- `PROJECT_CONSTITUTION.md` — Project governance
- `.kiro/steering/*.md` — Coding standards
- `spec/*.md` — Requirements and design
- `docs/ai-review.md` — Mistake log

### External Resources
- [GitHub Copilot Docs](https://docs.github.com/copilot)
- [OpenAI Best Practices](https://platform.openai.com/docs/guides/prompt-engineering)
- [Anthropic Claude Guide](https://docs.anthropic.com/claude/docs)

---

**Version**: 1.0  
**Last Updated**: 2026-09-23
