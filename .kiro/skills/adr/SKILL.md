---
inclusion: manual
---

# Architecture Decision Record (ADR) Skill

**Skill Name**: ADR Creation  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Purpose

This skill provides a structured workflow for creating Architecture Decision Records (ADRs) to document significant architectural and design decisions. ADRs capture the context, rationale, and consequences of important choices.

**Scope**: All architectural and design decisions  
**Authority**: Follows PROJECT_CONSTITUTION.md documentation requirements

---

## When to Use This Skill

Invoke this skill (`#adr`) when:

- Making a significant architectural decision
- Choosing between technology alternatives
- Establishing new design patterns
- Changing existing architecture
- Resolving architectural debates
- Making trade-offs with long-term implications

---

## What Qualifies as an ADR

### ✅ Requires ADR

- Technology stack choices (frameworks, libraries, databases)
- Architectural patterns (layered, microservices, event-driven)
- Design patterns (singleton, factory, strategy)
- Data modeling approaches (normalization, denormalization)
- API design decisions (REST vs GraphQL, versioning)
- Security approaches (authentication, authorization)
- Performance trade-offs (caching strategies, indexing)
- Testing strategies (unit vs integration vs property)
- Deployment models (monolith vs distributed)

### ❌ Does Not Require ADR

- Minor bug fixes
- Code formatting changes
- Routine feature implementations
- Variable naming
- Simple refactoring

---

## ADR Workflow

### Step 1: Identify the Decision

**Define what decision needs to be made:**

- What problem are we solving?
- What question are we answering?
- What choice are we making?

**Example questions**:
- "Should we use a separate endpoint for status transitions?"
- "Should we use Gradle or Maven?"
- "Should we expose entities or use DTOs?"

---

### Step 2: Gather Context

**Collect relevant information:**

**Technical Context**:
- Current architecture state
- Related systems or components
- Technical constraints
- Performance requirements
- Scalability considerations

**Business Context**:
- Business requirements
- User needs
- Time constraints
- Budget limitations
- Team expertise

**Historical Context**:
- Previous related decisions
- Lessons learned
- Similar patterns in the codebase

---

### Step 3: Identify Options

**List all viable alternatives:**

For each option, document:
- Brief description
- How it works
- Pros (advantages)
- Cons (disadvantages)
- Trade-offs
- Estimated effort

**Example**:
```markdown
### Option 1: Separate Status Transition Endpoint

**Description**: Create a dedicated PATCH /api/tickets/{id}/status endpoint

**Pros**:
- Explicit state machine validation
- Clear API semantics
- Easier to audit status changes

**Cons**:
- More endpoints to maintain
- Slight duplication with update endpoint

**Effort**: Low (1-2 hours)
```

---

### Step 4: Make the Decision

**Choose the best option based on:**

- Alignment with project principles
- Technical merit
- Long-term maintainability
- Team consensus
- Risk mitigation

**Decision criteria**:
1. Does it follow PROJECT_CONSTITUTION.md principles?
2. Is it maintainable long-term?
3. Does it solve the problem effectively?
4. Are the trade-offs acceptable?
5. Does the team agree?

---

### Step 5: Document Consequences

**Identify the impacts:**

**Positive Consequences**:
- What improves?
- What becomes easier?
- What risks are mitigated?

**Negative Consequences**:
- What becomes harder?
- What new risks are introduced?
- What technical debt is created?

**Neutral Consequences**:
- What changes but neither improves nor worsens?

---

### Step 6: Write the ADR

**Location**: `docs/architecture-decisions.md`

**Format**:

```markdown
## ADR-{NNN}: {Decision Title}

**Date**: YYYY-MM-DD  
**Status**: Accepted  
**Deciders**: [Names or "Development Team"]

---

### Context

[Explain the situation and problem]

What forces are at play? (technical, political, social, project-related)
What constraints exist?
What requirements drive this decision?

---

### Decision

[State the decision clearly]

We will [action] by [approach].

Example: "We will validate ticket status transitions through a separate 
endpoint by creating a dedicated PATCH /api/tickets/{id}/status endpoint."

---

### Consequences

**Positive**:
- [Positive consequence 1]
- [Positive consequence 2]

**Negative**:
- [Negative consequence 1]
- [Negative consequence 2]

**Neutral**:
- [Neutral change 1]

---

### Alternatives Considered

**Option 1: [Name]**
- Pros: [List]
- Cons: [List]
- Rejected because: [Reason]

**Option 2: [Name]**
- Pros: [List]
- Cons: [List]
- Rejected because: [Reason]

---

### Related Decisions

- ADR-XXX: [Related decision]
- Links to: [Other documents]

---

### Notes

[Additional context, references, or implementation guidance]
```

---

### Step 7: Update and Communicate

**After writing the ADR:**

1. **Add to docs/architecture-decisions.md** in chronological order
2. **Update the index** (if you have one)
3. **Communicate to team** (PR, Slack, meeting)
4. **Link from related specs** (if applicable)
5. **Commit with clear message**:
   ```
   docs: add ADR-NNN for [decision]
   
   Documented decision to [brief description].
   
   Related to: [issue/feature]
   ```

---

## ADR Examples from Your Project

### Example 1: Separate Status Transition Endpoint

```markdown
## ADR-001: Separate Status Transition Endpoint

**Date**: 2026-09-15  
**Status**: Accepted  
**Deciders**: Development Team

---

### Context

Ticket status transitions must follow strict state machine rules with only 5 
allowed transitions. The question is whether to handle status updates through 
the generic PATCH /api/tickets/{id} endpoint or create a dedicated endpoint.

**Constraints**:
- State machine validation is critical (business rule)
- Need clear audit trail for status changes
- Must return HTTP 422 for invalid transitions

---

### Decision

We will create a separate PATCH /api/tickets/{id}/status endpoint dedicated 
to status transitions.

---

### Consequences

**Positive**:
- Explicit state machine validation in one place
- Clear API semantics (intent to change status is obvious)
- Easier to add status-specific logic (e.g., notifications)
- Better audit trail (status changes distinct from other updates)

**Negative**:
- One more endpoint to maintain
- Slight duplication with update logic

**Neutral**:
- Total endpoints increases from 5 to 6

---

### Alternatives Considered

**Option 1: Use generic update endpoint**
- Pros: Fewer endpoints, standard CRUD pattern
- Cons: State machine validation mixed with other update logic, less clear intent
- Rejected because: Status transitions are special and warrant explicit handling

---

### Related Decisions

- Links to: spec/state-machine.md
- Links to: spec/api-contract.md

---

### Notes

Implementation should use TicketStateMachine service for validation.
Return HTTP 422 for invalid transitions.
```

---

### Example 2: H2 for Tests, PostgreSQL for Production

```markdown
## ADR-003: H2 for Tests, PostgreSQL for Production

**Date**: 2026-09-10  
**Status**: Accepted  
**Deciders**: Development Team

---

### Context

We need to choose database technology for both production and testing. 
Requirements include ACID compliance, JPA compatibility, and fast test execution.

---

### Decision

We will use PostgreSQL 15+ for production and H2 in-memory database for tests.

---

### Consequences

**Positive**:
- PostgreSQL provides robust, ACID-compliant production database
- H2 enables fast, isolated test execution
- Both are JPA-compatible
- No Docker required for tests
- Tests run in-memory (fast, no cleanup needed)

**Negative**:
- Minor dialect differences between H2 and PostgreSQL
- Custom SQL may need adjustment for each
- Risk of tests passing with H2 but failing with PostgreSQL

**Neutral**:
- Need to configure both datasources
- Flyway migrations run against both

---

### Alternatives Considered

**Option 1: PostgreSQL for both**
- Pros: Perfect production parity
- Cons: Slower tests, Docker/Testcontainers required, complex setup
- Rejected because: Test speed is critical for developer productivity

**Option 2: H2 for both**
- Pros: Simplicity, fast
- Cons: H2 not suitable for production, limited features
- Rejected because: Need production-grade database

---

### Related Decisions

- ADR-004: Flyway for database migrations

---

### Notes

Use H2 compatibility mode when possible. Test critical queries against 
PostgreSQL manually before production deployment.
```

---

## ADR Status Values

| Status | Meaning |
|--------|---------|
| **Proposed** | Decision is under consideration |
| **Accepted** | Decision has been made and is active |
| **Deprecated** | Decision is no longer recommended but code still exists |
| **Superseded** | Decision replaced by a newer ADR (link to new ADR) |
| **Rejected** | Decision was considered but not accepted |

**Example of superseded**:
```markdown
## ADR-005: Use Field Injection

**Status**: Superseded by ADR-012  
```

---

## Common ADR Topics

### Technology Choices
- Programming languages
- Frameworks
- Libraries
- Databases
- Build tools

### Architectural Patterns
- Layered architecture
- Microservices
- Event-driven
- CQRS
- Hexagonal

### Design Decisions
- DTO vs Entity exposure
- Constructor vs field injection
- EnumType.STRING vs ORDINAL
- Eager vs lazy loading
- Caching strategies

### API Design
- REST vs GraphQL
- Versioning approach
- Error handling format
- Authentication method
- Rate limiting

### Testing Approaches
- Test pyramid distribution
- Property-based testing
- Integration test strategy
- Test data management
- Coverage targets

---

## ADR Template Quick Reference

```markdown
## ADR-{NNN}: {Title}
**Date**: YYYY-MM-DD
**Status**: Accepted
**Deciders**: [Names]

### Context
[Problem and constraints]

### Decision
[What we decided]

### Consequences
**Positive**: [List]
**Negative**: [List]
**Neutral**: [List]

### Alternatives Considered
[Other options and why rejected]

### Related Decisions
[Links]

### Notes
[Additional info]
```

---

## Tips for Writing Good ADRs

### ✅ DO

- **Be concise** - ADRs should be readable in 5 minutes
- **Focus on WHY** - Explain the reasoning, not just the what
- **Include alternatives** - Show you considered other options
- **Be honest about trade-offs** - Acknowledge negative consequences
- **Use clear language** - Avoid jargon when possible
- **Date decisions** - Track when decisions were made
- **Link related ADRs** - Show decision lineage

### ❌ DON'T

- **Include implementation details** - That belongs in specs
- **Make ADRs too long** - Keep under 1 page if possible
- **Hide trade-offs** - Be transparent about downsides
- **Update old ADRs** - Create new ADRs to supersede them
- **Skip alternatives** - Always show what else was considered
- **Use vague language** - Be specific about the decision

---

## ADR Maintenance

### Quarterly Review

**Check for**:
- ADRs that need updating (status changes)
- Decisions that were never implemented
- Consequences that didn't materialize
- New decisions that supersede old ones

### When to Supersede

**Create a superseding ADR when**:
- Technology has changed significantly
- Requirements have evolved
- Original decision proved problematic
- Better approach discovered

**Format**:
```markdown
## ADR-025: New Approach

**Supersedes**: ADR-010

[New decision details]
```

**Update old ADR**:
```markdown
## ADR-010: Old Approach

**Status**: Superseded by ADR-025
```

---

## Checklist

Before finalizing an ADR:

- [ ] Decision is significant and architectural
- [ ] Context clearly explains the problem
- [ ] Decision is stated unambiguously
- [ ] Consequences (positive, negative, neutral) documented
- [ ] At least 2 alternatives considered
- [ ] Alternatives have pros/cons listed
- [ ] Related decisions linked
- [ ] Status is set (usually "Accepted")
- [ ] Date is current
- [ ] ADR added to docs/architecture-decisions.md
- [ ] Committed with descriptive message

---

**Skill Version**: 1.0  
**Last Updated**: 2026-09-23  
**Related Documents**: docs/architecture-decisions.md, PROJECT_CONSTITUTION.md
