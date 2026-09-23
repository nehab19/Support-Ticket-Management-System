# Spec-Driven Development Workflow

This document describes the specification-driven development methodology used to build the Support Ticket Management System with Kiro AI.

## Overview

Spec-driven development is a systematic approach to software development that emphasizes:
1. **Requirements First**: Document what needs to be built before building it
2. **Design Before Implementation**: Plan the architecture and approach
3. **Task Breakdown**: Break implementation into manageable, trackable tasks
4. **Property-Based Testing**: Define correctness properties that must hold
5. **Iterative Refinement**: Collaborate with AI to refine each phase

## Workflow Phases

### Phase 1: Requirements Definition

**Purpose**: Capture the "what" and "why" of the feature

**Location**: `.kiro/specs/{feature-name}/requirements.md`

**Contents**:
- Feature overview and goals
- User stories with acceptance criteria
- Functional requirements (numbered for traceability)
- Non-functional requirements (performance, security, etc.)
- Correctness properties (for property-based testing)
- Constraints and assumptions

**Example from this project**:
```markdown
## User Story 1: Create Support Ticket
As a support agent, I want to create a new support ticket so that I can track customer issues.

### Acceptance Criteria
- Agent can enter title (required, max 255 chars)
- Agent can enter description (required, multiline)
- Agent can select priority level
- Ticket is created with status OPEN
- Ticket receives unique ID and timestamp
```

**Process**:
1. Start with rough idea or user request
2. AI helps formalize into structured requirements
3. Iterate with user for clarity and completeness
4. Define measurable acceptance criteria
5. Identify correctness properties to test

---

### Phase 2: Design Documentation

**Purpose**: Capture the "how" - technical approach and architecture

**Location**: `.kiro/specs/{feature-name}/design.md`

**Contents**:
- High-level architecture
- Component breakdown
- Data models and schemas
- API contracts
- State machines and workflows
- Technology choices and rationale
- Integration points
- Security considerations

**Example from this project**:
```markdown
## State Machine Design

Ticket lifecycle is governed by a strict state machine:

OPEN → IN_PROGRESS (agent starts work)
OPEN → CANCELLED (request withdrawn)
IN_PROGRESS → RESOLVED (issue fixed)
IN_PROGRESS → CANCELLED (cannot fix)
RESOLVED → CLOSED (customer confirms)
RESOLVED → IN_PROGRESS (issue recurs)

Implementation: TicketStateMachine component validates all transitions
```

**Process**:
1. Review requirements with AI
2. Propose technical approach
3. AI suggests architecture patterns
4. Discuss technology choices
5. Document data models
6. Define API contracts
7. Plan testing strategy

---

### Phase 3: Task Breakdown

**Purpose**: Create executable implementation plan with traceability

**Location**: `.kiro/specs/{feature-name}/tasks.md`

**Contents**:
- Numbered, hierarchical task list
- Each task references requirements
- Subtasks for complex work
- Acceptance criteria per task
- Checkbox format for tracking
- Property-based test tasks explicitly listed

**Example from this project**:
```markdown
- [ ] 3. Domain enums and JPA entities
  - [ ] 3.1 Create `TicketStatus` and `Priority` enums
    - `TicketStatus`: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`
    - `Priority`: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
    - Place in `com.example.supportticket.model`
    - _Requirements: 1.6, 5.1_

  - [ ] 3.2 Implement `Ticket` JPA entity
    - Fields: `id`, `title`, `description`, `priority`, `status`, ...
    - `@PrePersist` sets both timestamps
    - _Requirements: 1.1, 1.2, 2.2, 3.1, 9.1_
```

**Task Format**:
- `[ ]` = Not started
- `[x]` = Completed
- `[-]` = In progress
- `[~]` = Queued

**Process**:
1. AI breaks design into implementation tasks
2. Tasks ordered by dependency
3. Each task sized for ~30-60 min work
4. Requirements traceability maintained
5. User reviews and adjusts scope

---

## Property-Based Testing Integration

A key aspect of this workflow is defining correctness properties upfront.

### What are Properties?

Properties are formal specifications of behavior that should hold for all valid inputs:

**Example**:
```
Property 1: Ticket creation invariants
For any valid CreateTicketRequest, the created ticket must have:
- status == OPEN
- non-null id
- non-null createdAt
```

### Property Definition Process

1. **During Requirements**: Identify what must always be true
2. **During Design**: Map properties to test strategy
3. **During Tasks**: Create explicit property test tasks
4. **During Implementation**: Write executable property tests

### Property Tests in This Project

This project includes 12 property-based tests:
- Ticket creation invariants
- Unique identifiers
- Title length validation
- Priority enum validation
- List ordering
- Field completeness
- Partial update preservation
- State machine transitions
- Comment creation
- Keyword search correctness
- Blank keyword rejection
- Status filter correctness
- Error response completeness

---

## Kiro AI Workflow

### Creating a New Spec

**Option 1: Requirements First (Traditional)**
```
User: "I want to build a support ticket system"
AI: "Let's start with requirements. What features do you need?"
→ Creates requirements.md
→ User reviews and approves
→ AI creates design.md based on requirements
→ User reviews and approves
→ AI creates tasks.md with implementation plan
```

**Option 2: Design First (Technical)**
```
User: "I need to migrate from PostgreSQL to H2"
AI: "Let's start with the technical design"
→ Creates design.md with approach
→ User reviews and approves
→ AI derives requirements.md from design
→ AI creates tasks.md with implementation plan
```

### Executing Tasks

**Manual Execution**:
```bash
# User reads task file
# User asks AI to implement specific task
User: "Execute task 3.1"
AI: Implements the task, updates checkbox to [x]
```

**Automated Execution**:
```bash
User: "Run all tasks"
AI: 
- Marks tasks as [~] queued
- Executes each task sequentially
- Marks as [-] in progress, then [x] complete
- Reports progress after each task
```

### Task Execution with Subagents

Kiro uses specialized subagents for task execution:
- **spec-task-execution**: Implements individual tasks
- **context-gatherer**: Analyzes codebase for context
- **general-task-execution**: Handles arbitrary tasks

---

## File Structure

```
project-root/
├── .kiro/
│   └── specs/
│       ├── {feature-1}/
│       │   ├── .config.kiro          # Spec metadata
│       │   ├── requirements.md       # What to build
│       │   ├── design.md            # How to build it
│       │   └── tasks.md             # Implementation plan
│       └── {feature-2}/
│           ├── requirements.md
│           ├── design.md
│           └── tasks.md
├── .specstory/
│   └── history/                     # Prompt history
│       ├── 001-*.md
│       └── ...
├── docs/
│   ├── prompt-history.md            # Consolidated history
│   └── spec-driven-development.md   # This file
└── [source code]
```

---

## Benefits of This Approach

### 1. **Clarity Before Code**
- Understand requirements fully before implementation
- Avoid building wrong thing
- Reduce rework and technical debt

### 2. **Traceability**
- Every line of code traces to a requirement
- Easy to verify completeness
- Clear audit trail for decisions

### 3. **Collaborative AI Development**
- AI understands full context
- Better code generation
- Consistent with architecture

### 4. **Incremental Progress**
- Work can pause and resume
- Tasks completed in any order (respecting dependencies)
- Progress visible via checkboxes

### 5. **Quality Assurance**
- Property-based testing from start
- Test cases derived from requirements
- Coverage is planned, not accidental

### 6. **Documentation as Artifact**
- Specs become living documentation
- New team members understand "why"
- Design decisions preserved

---

## Real-World Example: This Project

### Feature: Support Ticket Management

**Step 1: Requirements** (User Story → Acceptance Criteria)
```
User Story 1: Create tickets
- Must have title, description, priority
- Status defaults to OPEN
- ID and timestamps auto-generated
```

**Step 2: Design** (Technical Decisions)
```
- Spring Boot REST API
- JPA entities with validation
- State machine for transitions
- Property-based tests with jqwik
```

**Step 3: Tasks** (39 tasks total)
```
1. Backend scaffolding
2. Database migrations
3. Domain models
...
10. Backend tests (20 test classes)
11. Frontend scaffolding
...
15. Final verification
```

**Step 4: Execution**
- AI executed all 39 tasks
- All checkboxes marked [x]
- Property tests passing
- Applications deployed

---

## Bugfix Workflow

For fixing bugs, use the bugfix spec workflow:

**Step 1: Bug Condition Exploration**
```markdown
## Bug Condition C(X)
The bug occurs when: quantity = 0 in cart checkout

Test: For all carts where quantity = 0, checkout should fail gracefully
```

**Step 2: Root Cause Design**
```markdown
Issue: Division by zero in discount calculation
Fix: Add validation before calculation
```

**Step 3: Fix Implementation Tasks**
```markdown
1. Add validation for quantity > 0
2. Update error handling
3. Add test for edge case
```

---

## Tips for Effective Spec-Driven Development

### 1. **Start Small**
- Begin with clear, focused features
- Don't try to spec entire system at once
- Iterate and expand

### 2. **Be Specific in Requirements**
- Use "must", "should", "may" clearly
- Quantify requirements (e.g., "max 255 chars")
- Include negative cases ("must not allow...")

### 3. **Review Each Phase**
- Don't rush to implementation
- User approval at each phase
- Refine based on feedback

### 4. **Maintain Traceability**
- Reference requirements in tasks
- Link tests to properties
- Keep audit trail

### 5. **Use Property-Based Testing**
- Think in terms of invariants
- "What should always be true?"
- Test with generated data, not fixed examples

### 6. **Leverage AI Strengths**
- Let AI suggest architecture patterns
- Use AI for boilerplate generation
- AI can spot inconsistencies

### 7. **Keep Specs Updated**
- Update specs when requirements change
- Treat specs as living documents
- Version control everything

---

## Integration with Other Tools

### Cursor / VS Code
- Install SpecStory extension for prompt tracking
- Use AI chat for requirement refinement
- Leverage inline AI for implementation

### Kiro
- Built-in spec workflow support
- Automatic task tracking
- Subagent delegation for execution

### Git
- Commit specs before implementation
- Reference spec files in commit messages
- Branch per feature/spec

---

## Comparison: Traditional vs Spec-Driven

| Aspect | Traditional | Spec-Driven |
|--------|------------|-------------|
| **Planning** | Minimal or ad-hoc | Comprehensive upfront |
| **Documentation** | After code (if at all) | Before and during code |
| **Testing** | Added later | Planned from requirements |
| **AI Collaboration** | Tactical (fix this bug) | Strategic (build this system) |
| **Traceability** | Poor | Excellent |
| **Onboarding** | Difficult | Clear from specs |
| **Quality** | Varies | Consistent |

---

## Conclusion

Spec-driven development with AI assistance provides:
- ✅ Clear requirements and design before coding
- ✅ Traceable implementation plan
- ✅ Property-based testing from the start
- ✅ Living documentation
- ✅ Efficient AI collaboration
- ✅ High-quality, maintainable code

This project demonstrates the effectiveness of this approach, resulting in a complete, tested, documented system built through human-AI collaboration.

---

## References

- [.kiro/specs/support-ticket-management/](../.kiro/specs/support-ticket-management/) - Full spec example
- [.kiro/specs/migrate-to-h2-database/](../.kiro/specs/migrate-to-h2-database/) - Bugfix spec example
- [docs/prompt-history.md](prompt-history.md) - How this project was built
- [.specstory/history/](../.specstory/history/) - Individual prompt records

---

**Created**: 2026-09-23  
**Last Updated**: 2026-09-23  
**Project**: Support Ticket Management System  
**Methodology**: Spec-Driven Development with Kiro AI
