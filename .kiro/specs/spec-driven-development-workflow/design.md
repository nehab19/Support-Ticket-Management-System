# Design Document — Spec-Driven Development Workflow

## Overview

The Spec-Driven Development Workflow is a structured methodology documentation system that transforms rough ideas into detailed, testable implementation plans. The system consists of markdown documentation templates, workflow phase definitions, quality standards (EARS patterns and INCOSE rules), and property-based testing guidance. The documentation enables developers working in Cursor/Kiro/VS Code to follow a systematic process from requirements gathering through design creation to task execution and implementation.

### Goals

- Provide clear, repeatable workflows for feature development and bugfix scenarios
- Enforce quality standards for requirements (EARS patterns, INCOSE rules)
- Integrate property-based testing principles from specification to implementation
- Support three workflow paths: requirements-first, design-first, and bugfix
- Enable traceability from requirements through design to tasks
- Maintain consistency across specs through templates and conventions

### Scope

This design covers:
- Documentation structure and markdown templates
- Three workflow variants (requirements-first, design-first, bugfix)
- File structure and naming conventions
- Property-based testing integration methodology
- EARS patterns and INCOSE quality rules reference
- Workflow decision guide and common patterns
- IDE integration guidance for Cursor/Kiro/VS Code

This design does NOT cover:
- Automated code generation from specs (specs are human-readable blueprints)
- Project management or issue tracking integration
- Automated requirements validation tooling
- Version control workflows (git branching strategies)

---

## Architecture

The workflow system is structured as layered documentation with increasing specificity:

```
┌─────────────────────────────────────────────────────────┐
│         Quick Start Guide + Decision Tree               │ Entry
└────────────────────┬────────────────────────────────────┘
                     │
     ┌───────────────┼───────────────┐
     │               │               │
┌────▼──────┐  ┌────▼──────┐  ┌────▼──────┐
│Requirements│  │  Design   │  │  Bugfix   │  Workflow
│   First    │  │   First   │  │  Workflow │  Variants
└────┬───────┘  └─────┬─────┘  └─────┬─────┘
     │                │              │
     └────────────────┼──────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
   ┌────▼────┐   ┌───▼────┐   ┌───▼────┐
   │Require- │   │Design  │   │Tasks   │  Document
   │ments.md │   │.md     │   │.md     │  Templates
   └─────────┘   └────────┘   └────────┘
        │             │             │
        └─────────────┼─────────────┘
                      │
        ┌─────────────┼─────────────────────┐
        │             │                     │
   ┌────▼────┐   ┌───▼────────┐   ┌───────▼────────┐
   │  EARS   │   │  Property  │   │    Common      │  Reference
   │Patterns │   │    Based   │   │   Patterns     │  Materials
   │& INCOSE │   │   Testing  │   │  & Examples    │
   └─────────┘   └────────────┘   └────────────────┘
```

### Architectural principles

1. **Human-first**: Markdown documentation optimized for reading and writing by humans
2. **Traceability**: Explicit links from tasks → design → requirements
3. **Incrementality**: Each workflow phase produces a complete, reviewable document
4. **Flexibility**: Support for iterative refinement and feedback loops
5. **Tool-agnostic core**: Templates work in any markdown-aware environment

---

## Components and Interfaces

### File Structure

Every spec lives in `.kiro/specs/{feature-name}/` where `{feature-name}` uses kebab-case.

```
.kiro/
└── specs/
    └── {feature-name}/
        ├── .config.kiro           # Metadata (specId, workflowType, specType)
        ├── requirements.md        # User stories + acceptance criteria
        ├── design.md              # Architecture + correctness properties
        └── tasks.md               # Hierarchical task breakdown
```

### Config File Schema

**Path**: `.kiro/specs/{feature-name}/.config.kiro`

**Content** (JSON):
```json
{
  "specId": "uuid-v4",
  "workflowType": "requirements-first | design-first | bugfix",
  "specType": "feature | bugfix"
}
```

| Field | Type | Purpose |
|---|---|---|
| `specId` | UUID v4 | Unique identifier for the spec |
| `workflowType` | enum | Which workflow path was followed |
| `specType` | enum | Feature development or bugfix |

### Document Templates

Each template is a markdown file with a defined structure.

#### Requirements Template Structure

```markdown
# Requirements Document

## Introduction

[Overview paragraph describing the feature/system]

---

## Glossary

- **Term**: Definition
- **System_Name**: Definition (used in EARS patterns)

---

## Requirements

### Requirement N: [Title]

**User Story:** As a [role], I want [capability], so that [benefit].

#### Acceptance Criteria

1. [EARS pattern statement]
2. [EARS pattern statement]
...
```

#### Design Template Structure

```markdown
# Design Document — [Feature Name]

## Overview

[High-level description, goals, scope]

---

## Architecture

[System structure, component diagram, layer responsibilities]

---

## Components and Interfaces

[REST endpoints, APIs, component map]

---

## Data Models

[Database schemas, entity definitions, indexes]

---

## Correctness Properties

*[Explanation of what properties are]*

### Property N: [Title]

*For any/all* [universal quantification statement]

**Validates: Requirements X.Y, X.Z**

---

## Error Handling

[Exception mapping, error response structures]

---

## Testing Strategy

[PBT library selection, test configuration, unit vs property tests]
```

#### Tasks Template Structure

```markdown
# Implementation Plan: [Feature Name]

## Overview

[Summary of implementation approach]

---

## Tasks

- [ ] 1. [Top-level task]
  - [ ] 1.1 [Subtask]
    - Detail
    - _Requirements: X.Y, X.Z_
  - [x] 1.2 [Completed subtask]

- [-] 2. [In-progress task]
  - ...

---

## Notes

[Optional section for cross-cutting concerns, dependencies]
```

**Task Status Markers**:
- `[ ]` — Not started
- `[-]` — In progress
- `[x]` — Completed

---

## Workflows

### Workflow 1: Requirements-First

**When to use**: New feature with clear user needs

**Phases**:
```
1. Requirements Gathering
   ↓
2. Design Creation
   ↓
3. Task Breakdown
   ↓
4. Implementation
```

**Phase 1: Requirements Gathering**

**Input**: User stories, stakeholder interviews, feature requests

**Process**:
1. Identify user roles and goals
2. Write user stories (As a [role], I want [capability], so that [benefit])
3. Define acceptance criteria using EARS patterns
4. Build glossary of domain terms
5. Review for INCOSE quality rules
6. User approval checkpoint

**Output**: `requirements.md`

**Phase 2: Design Creation**

**Input**: Approved `requirements.md`

**Process**:
1. Identify research needs (technologies, libraries, patterns)
2. Conduct research and summarize findings in conversation
3. Write architecture and component sections
4. Define data models and interfaces
5. **Assess PBT applicability** (see Testing Strategy section below)
6. **If PBT applies**: Run prework tool to analyze acceptance criteria
7. **If PBT applies**: Perform property reflection to eliminate redundancy
8. **If PBT applies**: Write correctness properties section
9. **If PBT does NOT apply**: Skip correctness properties, document alternative testing approach
10. Write error handling and testing strategy sections
11. User review checkpoint

**Output**: `design.md`

**Phase 3: Task Breakdown**

**Input**: Approved `design.md`

**Process**:
1. Decompose implementation into hierarchical tasks
2. Annotate tasks with requirement references (_Requirements: X.Y_)
3. For property tests, annotate with property numbers
4. Estimate effort and identify dependencies
5. User review checkpoint

**Output**: `tasks.md`

**Phase 4: Implementation**

**Input**: `tasks.md`

**Process**:
1. Execute tasks sequentially or in dependency order
2. Mark tasks in progress `[-]` and completed `[x]`
3. Write code, tests, documentation
4. Run property tests with ≥100 iterations
5. Verify requirement traceability

---

### Workflow 2: Design-First

**When to use**: New feature with clear technical architecture

**Phases**:
```
1. Design Creation
   ↓
2. Requirements Derivation
   ↓
3. Task Breakdown
   ↓
4. Implementation
```

**Phase 1: Design Creation**

**Input**: Technical architecture ideas, component sketches

**Process**:
1. Write architecture and component sections
2. Define data models and interfaces
3. Document design decisions and rationales
4. Create diagrams (ASCII art or Mermaid)
5. User review checkpoint

**Output**: `design.md` (initial version, no correctness properties yet)

**Phase 2: Requirements Derivation**

**Input**: `design.md`

**Process**:
1. Extract user-facing behaviors from design
2. Write user stories for each behavior
3. Convert behaviors into EARS acceptance criteria
4. Build glossary from design terms
5. User review checkpoint

**Output**: `requirements.md`

**Phase 3: Complete Design with Properties**

**Input**: `requirements.md`

**Process**:
1. **Assess PBT applicability**
2. **If PBT applies**: Run prework tool, perform reflection, write properties
3. **If PBT does NOT apply**: Document alternative testing
4. Complete error handling and testing strategy
5. User review checkpoint

**Output**: `design.md` (complete)

**Phase 4: Task Breakdown and Implementation**

Same as requirements-first workflow

---

### Workflow 3: Bugfix

**When to use**: Fixing existing bugs

**Phases**:
```
1. Bug Condition Definition
   ↓
2. Reproduction Test
   ↓
3. Design (Fix Approach)
   ↓
4. Task Breakdown
   ↓
5. Implementation + Verification
```

**Phase 1: Bug Condition**

**Input**: Bug report, reproduction steps

**Process**:
1. Write bug condition as falsifiable statement
2. Identify root cause (if known)
3. Define expected correct behavior

**Output**: `requirements.md` with bug condition section

**Example bug condition**:
```markdown
## Bug Condition

**Current Behavior**: When a ticket status is CLOSED, the API allows transitioning to IN_PROGRESS without error.

**Expected Behavior**: Transitions from CLOSED to any other status SHALL be rejected with HTTP 422.

**Root Cause**: State machine does not validate transitions from terminal states.
```

**Phase 2: Reproduction Test**

**Process**:
1. Write property-based test that currently FAILS
2. Test verifies the expected behavior
3. Run test to confirm failure

**Phase 3: Design Fix**

**Process**:
1. Document fix approach in `design.md`
2. Update state machine / component design
3. Update correctness properties if needed

**Phase 4: Task Breakdown and Implementation**

**Process**:
1. Create tasks for fix implementation
2. Implement fix
3. Verify reproduction test now PASSES
4. Add additional tests if needed

---

## EARS Patterns Reference

### Six EARS Patterns

EARS (Easy Approach to Requirements Syntax) provides six structured templates for writing requirements.

#### 1. Ubiquitous

**Format**: `THE {system_name} SHALL {capability}`

**When**: Always-active behaviors, no triggering event

**Examples**:
- THE API SHALL persist all created tickets to the database.
- THE System SHALL encrypt all passwords before storage.

#### 2. Event-Driven

**Format**: `WHEN {trigger} [, {optional precondition}] THEN THE {system_name} SHALL {response}`

**When**: Specific events trigger system responses

**Examples**:
- WHEN a user submits a valid login form THEN THE System SHALL authenticate the user and redirect to the dashboard.
- WHEN a ticket creation request is submitted, THEN THE API SHALL create a ticket with status OPEN.

#### 3. State-Driven

**Format**: `WHILE {system state} [, {optional condition}] THE {system_name} SHALL {behavior}`

**When**: Behavior depends on system being in a specific state

**Examples**:
- WHILE a ticket is in OPEN status, THE UI SHALL display the "Start Work" button.
- WHILE the user is authenticated, THE System SHALL show the logout link.

#### 4. Unwanted Event

**Format**: `IF {undesired condition/event} [, WHEN {trigger}] THEN THE {system_name} SHALL {response}`

**When**: Error conditions, validation failures, edge cases

**Examples**:
- IF a ticket creation request omits the title field, THEN THE Validator SHALL reject the request with HTTP 400.
- IF a status transition is invalid, THEN THE State_Machine SHALL return HTTP 422.

#### 5. Optional Feature

**Format**: `WHERE {feature is included} [, {condition}] THE {system_name} SHALL {capability}`

**When**: Conditional features, optional capabilities

**Examples**:
- WHERE the premium tier is active, THE System SHALL enable advanced analytics.
- WHERE no priority is specified, THE API SHALL default to MEDIUM priority.

#### 6. Complex (Combination)

Combine multiple patterns for complex requirements using nested triggers.

**Example**:
```
WHILE the ticket is in IN_PROGRESS status,
WHEN a user clicks "Resolve",
IF all required fields are complete,
THEN THE System SHALL transition the ticket to RESOLVED status
  and send a notification to the assignee.
```

### EARS System Names

All system names (THE **System**, THE **API**, THE **Validator**) MUST be defined in the Glossary section.

---

## INCOSE Quality Rules

The International Council on Systems Engineering defines quality characteristics for requirements:

### Rule 1: Clarity

**Definition**: Requirement is unambiguous and uses precise language

**Good**: `WHEN a user submits a create-ticket request with a title exceeding 255 characters, THEN THE Validator SHALL reject the request with HTTP 400.`

**Bad**: `The system should probably validate the title length somehow.`

### Rule 2: Testability

**Definition**: Requirement can be verified through testing, inspection, analysis, or demonstration

**Good**: `THE API SHALL return all tickets ordered by createdAt descending.` (testable: query API, verify order)

**Bad**: `The UI should feel intuitive.` (not testable)

### Rule 3: Completeness

**Definition**: Requirement specifies all necessary information, including error cases

**Good**: Include both success and failure acceptance criteria for each requirement

**Bad**: Only specifying the happy path

### Rule 4: Positive Statements

**Definition**: Requirement states what the system SHALL do, not what it SHALL NOT do

**Good**: `THE API SHALL validate priority against the enum values LOW, MEDIUM, HIGH, CRITICAL.`

**Bad**: `THE API SHALL NOT accept invalid priority values.`

### Rule 5: Consistency

**Definition**: No contradictions with other requirements

**Verification**: Cross-check all requirements for conflicts

### Rule 6: Atomic

**Definition**: Each requirement addresses a single concern

**Good**: Split "create and validate ticket" into separate requirements for creation and validation

**Bad**: Combining multiple behaviors in a single requirement

---

## Property-Based Testing Integration

### What Are Correctness Properties?

A correctness property is a universal characteristic that must hold across all valid executions of a system component. Properties are expressed as "for all X, condition P(X) holds" statements.

### When PBT Is Appropriate

Property-based testing works best when:
- The code under test has **clear input/output behavior** (pure functions, deterministic transformations)
- There are **universal properties** that hold across a wide input space
- The input space is large or infinite (strings, numbers, collections, structured data)
- Testing **parsers, serializers, data transformations, algorithms, business logic**

### When PBT Is NOT Appropriate

Do NOT use property-based testing for:

1. **Infrastructure as Code (IaC)** — Terraform, CDK, CloudFormation
   - Use snapshot tests and policy/compliance checks

2. **UI rendering and layout** — React components, HTML templates
   - Use snapshot tests and visual regression tests

3. **Simple CRUD operations** — Direct database reads/writes with no transformation
   - Use example-based unit tests

4. **Configuration validation** — Checking config files have required fields
   - Use schema validation and example tests

5. **Side-effect-only operations** — Sending emails, logging, webhooks
   - Use mock-based unit tests

6. **External service integration** — API calls, file I/O without logic
   - Use integration tests with 1-3 examples

**Rule of thumb**: If you cannot write "for all inputs X, property P(X) holds", use example-based tests instead.

### Common Property Patterns

#### 1. Invariants

Properties that remain constant despite transformations

**Examples**:
- Collection size preserved after map: `len(map(f, xs)) == len(xs)`
- Tree balance preserved after insertion
- `object.startDate <= object.endDate`

**Spec pattern**:
```markdown
### Property N: [Invariant name]

*For any* [data structure], after [operation], [invariant] SHALL hold.

**Validates: Requirements X.Y**
```

#### 2. Round-Trip Properties

Operation followed by its inverse returns to original value

**Examples**:
- Serialization: `decode(encode(x)) == x`
- Parsing: `parse(format(x)) == x`
- Database: `read(write(x)) == x`

**Spec pattern**:
```markdown
### Property N: [Operation] round-trip

*For any* valid [input type], [operation] followed by [inverse operation] SHALL produce an equivalent value.

**Validates: Requirements X.Y**
```

**Critical**: Always include a round-trip property for parsers and serializers.

#### 3. Idempotence

Operation applied twice equals operation applied once: `f(x) = f(f(x))`

**Examples**:
- `distinct(distinct(list)) == distinct(list)`
- `normalize(normalize(string)) == normalize(string)`
- `SET status = X` applied twice has same effect as once

**Spec pattern**:
```markdown
### Property N: [Operation] idempotence

*For any* [input], applying [operation] twice SHALL produce the same result as applying it once.

**Validates: Requirements X.Y**
```

#### 4. Metamorphic Properties

Relationship between two variants without knowing exact output

**Examples**:
- `len(filter(list, predicate)) <= len(list)`
- `sort(list1) == sort(list2)` if `list1` and `list2` contain same elements

**Spec pattern**:
```markdown
### Property N: [Relationship name]

*For any* [inputs], [relationship between operations] SHALL hold.

**Validates: Requirements X.Y**
```

#### 5. Error Conditions

Invalid inputs produce appropriate errors

**Examples**:
- Title > 255 chars → HTTP 400
- Invalid enum value → HTTP 400
- Non-existent ID → HTTP 404

**Spec pattern**:
```markdown
### Property N: [Error condition] validation

*For any* [invalid input description], THE System SHALL [error response].

**Validates: Requirements X.Y**
```

### Property Creation Process (Requirements-First)

1. **Complete acceptance criteria in requirements.md**
2. **Write design sections through Data Models**
3. **Assess PBT applicability** (see "When PBT Is Appropriate" above)
4. **If PBT applies**:
   a. **Stop before Correctness Properties section**
   b. **Run prework tool** to analyze each acceptance criterion
   c. **Classify each criterion**: PROPERTY, EXAMPLE, EDGE_CASE, INTEGRATION, SMOKE
   d. **Perform property reflection** to eliminate redundancy
   e. **Write Correctness Properties section** based on prework
5. **If PBT does NOT apply**:
   - Skip Correctness Properties section
   - Document alternative testing strategy
6. **Complete remaining sections** (Error Handling, Testing Strategy)

### Prework Tool Usage

**Purpose**: Analyze acceptance criteria for testability before writing properties

**Process**:
For each acceptance criterion:
1. Does this test YOUR code or external services? (External → INTEGRATION)
2. Does behavior vary meaningfully with input? (No → EXAMPLE/SMOKE)
3. Would 100 iterations find more bugs than 2-3? (No → EXAMPLE/INTEGRATION)
4. Cost-effective to run 100+ times? (High cost → INTEGRATION or mock-based PROPERTY)

**Output format**:
```
X.Y Criteria Name
  Thoughts: step by step reasoning
  Classification: PROPERTY | EXAMPLE | EDGE_CASE | INTEGRATION | SMOKE
  Test Strategy: how to test, what varies, what's verified
```

### Property Reflection

After prework, eliminate redundant properties:

1. Review ALL properties identified in prework
2. Identify logically redundant properties (one implies another)
3. Identify properties that can be combined into comprehensive property
4. Mark redundant properties for removal
5. Ensure each remaining property provides unique validation

**Examples of redundancy**:
- "Adding task increases list length by 1" + "List contains added task" → Keep second, remove first
- "Parsing preserves structure" + "Round-trip parsing is identity" → Keep round-trip (subsumes first)
- "New node has doc field" + "New node has owner field" → Combine into "New node has required fields"

### Property Annotation Format

```markdown
### Property N: [Title]

*For any/all* [universal quantification starting with "for any" or "for all"]

**Validates: Requirements X.Y, X.Z**
```

### Property-Based Testing Libraries

| Language | Library | Configuration |
|---|---|---|
| Java | jqwik | `@Property(tries = 100)` |
| Python | Hypothesis | `@given(...)` with `settings(max_examples=100)` |
| JavaScript/TypeScript | fast-check | `fc.assert(fc.property(...), { numRuns: 100 })` |
| Haskell | QuickCheck | `quickCheck prop` (default 100 tests) |

### Property Test Tag Format

Each property test MUST include a comment tag:

```java
// Feature: {feature-name}, Property {N}: {property-title}
@Property(tries = 100)
void propertyTest(...) { ... }
```

**Example**:
```java
// Feature: support-ticket-management, Property 1: Ticket creation invariants
@Property(tries = 100)
void ticketCreationInvariants(@ForAll @Size(min=1, max=255) String title) {
    Ticket ticket = service.createTicket(new CreateTicketRequest(title, "desc", null));
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
    assertThat(ticket.getId()).isNotNull();
    assertThat(ticket.getCreatedAt()).isNotNull();
}
```

---

## Workflow Decision Guide

### Decision Tree

```
Start: Need to implement something?
  │
  ├─ Is it a bug fix?
  │   └─ YES → Bugfix Workflow
  │        (Bug condition → Reproduction test → Fix → Verify)
  │
  └─ NO → New feature
      │
      ├─ Do you have clear user stories and acceptance criteria?
      │   └─ YES → Requirements-First Workflow
      │        (Requirements → Design → Tasks → Implement)
      │
      └─ NO → Do you have a clear technical architecture?
          ├─ YES → Design-First Workflow
          │    (Design → Requirements → Tasks → Implement)
          │
          └─ NO → Start with requirements gathering
               (Clarify with stakeholders, then choose workflow)
```

### Scenario Guidance

| Scenario | Recommended Workflow | Rationale |
|---|---|---|
| Building user-facing feature with clear user needs | Requirements-First | User stories drive design |
| Building internal library/API with technical architecture | Design-First | Technical structure is primary concern |
| Fixing reported bug | Bugfix | Focus on reproduction and verification |
| Refactoring existing code | Design-First or Requirements-First | Depends on whether behavior or structure drives change |
| Adding property tests to existing code | Use PBT guide section | Extract properties from existing behavior |
| Spike/prototype | No spec needed | Explore first, spec after direction is clear |
| Documentation of technical debt | Requirements or Design | Capture current state + desired state |

---

## Common Workflow Patterns

### Pattern 1: Adding Feature to Existing System

**Situation**: System exists, need to add a new capability

**Process**:
1. Choose Requirements-First workflow
2. Write requirements focusing on new capability
3. In design, reference existing system architecture
4. In tasks, identify integration points with existing code
5. Add property tests for new behavior

**Example**: Adding comment feature to ticket system

### Pattern 2: Creating New Microservice

**Situation**: Building a new standalone service

**Process**:
1. Choose Design-First workflow
2. Start with architecture diagram showing service boundaries
3. Define API contracts and data models
4. Derive requirements from API behaviors
5. Complete design with properties for core logic

**Example**: Building notification service

### Pattern 3: Refactoring with Behavior Preservation

**Situation**: Improving code structure without changing behavior

**Process**:
1. If no spec exists, create one documenting current behavior (Requirements-First)
2. Write property tests capturing current behavior
3. Perform refactoring
4. Verify property tests still pass
5. Update design document to reflect new structure

**Example**: Extracting state machine into separate component

### Pattern 4: Adding PBT to Existing Code

**Situation**: Existing codebase lacks property tests

**Process**:
1. Identify core logic functions/methods
2. Create design.md with correctness properties section
3. For each property:
   - Write universal statement
   - Implement as property test
   - Run with ≥100 iterations
4. Document in testing strategy

**Example**: Adding properties to existing parser

### Pattern 5: Documenting Technical Debt

**Situation**: Known issues or limitations need tracking

**Process**:
1. Create spec with requirements describing desired state
2. In design, document current state vs. desired state
3. Create tasks for incremental improvements
4. Mark tasks as not started
5. Prioritize in backlog

**Example**: Database performance optimization spec

---

## IDE Integration Guidance

### Cursor/Kiro/VS Code Usage

#### Invoking Workflow Subagents

From chat interface:
```
Create a new spec for [feature name] using requirements-first workflow
```

Subagents:
- `feature-requirements-first-workflow` — Full requirements → design → tasks flow
- `feature-design-first-workflow` — Full design → requirements → tasks flow
- `bugfix-workflow` — Bug condition → reproduction → fix flow
- `spec-task-execution` — Execute tasks from tasks.md

#### Navigating Spec Documents

1. Use file tree: `.kiro/specs/{feature-name}/`
2. Open all three docs in tabs: requirements.md, design.md, tasks.md
3. Use markdown preview for formatted view
4. Use search (Cmd/Ctrl+F) to find requirement references

#### Running Tests

From integrated terminal:
```bash
# Java/Maven
mvn test

# Python/pytest
pytest tests/

# JavaScript/Jest
npm test

# With specific property test
mvn test -Dtest=PropertyClassName
```

#### Task Tracking

1. Manually update task markers in tasks.md:
   - `[ ]` → `[-]` when starting
   - `[-]` → `[x]` when complete
2. Use IDE search to find tasks by requirement reference: `_Requirements: 1.1_`
3. Use outline view (if available) to navigate task hierarchy

#### Markdown Tips

- Use `#` headers for sections (auto-generates outline)
- Use code fences with language tags for syntax highlighting
- Use tables for structured data (endpoints, schemas)
- Use Mermaid for diagrams (if IDE supports)

---

## Testing Strategy

### Dual Testing Approach

The workflow integrates two complementary testing strategies:

1. **Example-Based Unit Tests**
   - Specific scenarios with concrete inputs/outputs
   - Edge cases and error conditions
   - Integration points between components
   - Fast, deterministic, easy to debug

2. **Property-Based Tests**
   - Universal properties across all inputs
   - Comprehensive input coverage through randomization
   - Catches unexpected edge cases
   - Minimum 100 iterations per property

### When to Use Each

| Use Example Tests | Use Property Tests |
|---|---|
| Specific example demonstrates correct behavior | Universal property holds for all inputs |
| Integration between components | Pure function or clear input/output |
| Error handling for specific conditions | Invariant, round-trip, or idempotence |
| Setup/teardown complex | Input space is large or infinite |
| External service calls | Testing parser, serializer, algorithm |

### Avoid Over-Testing

- Don't write 20 example tests for the same input variation → Use property test
- Don't write property tests for deterministic lookups → Use example test
- Don't write tests for framework behavior (Spring Boot, React) → Trust framework

### Property Test Configuration

**Minimum requirements**:
- Run ≥100 iterations per property test
- Tag each test with feature name and property number
- Reference design document property in test comment

**Configuration examples**:
```java
// jqwik (Java)
@Property(tries = 100)

// Hypothesis (Python)
@given(text())
@settings(max_examples=100)

// fast-check (JavaScript)
fc.assert(fc.property(fc.string(), (s) => { ... }), { numRuns: 100 })
```

### Test Organization

```
src/test/java/com/example/feature/
  PropertyTest1.java              // Feature: feature-name, Property 1
  PropertyTest2.java              // Feature: feature-name, Property 2
  ServiceUnitTest.java            // Example-based unit tests
  IntegrationTest.java            // Full-stack integration tests
```

---

## Example: Support Ticket Management

The support-ticket-management spec (`.kiro/specs/support-ticket-management/`) demonstrates:

### Requirements Document

- **Glossary**: 12 domain terms (Ticket, Status, Priority, State_Machine, etc.)
- **11 requirements** with user stories
- **EARS patterns used**:
  - Event-driven: "WHEN a valid ticket creation request is submitted, THE API SHALL create a ticket with status OPEN"
  - Unwanted event: "IF a ticket creation request omits the title field, THEN THE Validator SHALL reject the request with HTTP 400"
  - Ubiquitous: "THE Repository SHALL persist all created tickets and comments"
  - Optional: "WHERE no priority is specified, THE API SHALL default to MEDIUM"

### Design Document

- **Architecture**: 3-tier diagram (UI → API → Database)
- **Components**: REST endpoints, DTOs, state machine
- **Data models**: Ticket and Comment entities with schemas
- **13 Correctness Properties**:
  - Property 1 (Invariant): Ticket creation invariants (status=OPEN, non-null id/createdAt)
  - Property 8 (State machine): Allowed transitions succeed, disallowed rejected
  - Property 10 (Metamorphic): Keyword search returns exactly matching tickets
  - Property 7 (Partial update): Untouched fields preserved
- **Testing Strategy**: jqwik for properties, H2 for unit tests, example-based for integration

### Tasks Document

- **59 subtasks** organized into 15 top-level tasks
- **Requirement traceability**: Each task annotated with `_Requirements: X.Y_`
- **Property test tasks**: Annotated with property numbers
  - Task 10.2: "Write property test StateMachinePropertyTest (Property 8)"
  - Task 10.5: "Write property test TicketCreationPropertyTest (Property 1)"
- **Status tracking**: All tasks marked `[x]` (completed)

### Key Patterns Demonstrated

1. **State machine enforcement** as correctness property
2. **Round-trip properties** for comment creation
3. **Metamorphic properties** for search correctness
4. **Error response completeness** as property
5. **Dual testing** (13 properties + 8 unit/integration test classes)
6. **Requirements → Properties traceability** (each property validates specific requirements)

---

## Appendix: Quick Start Checklist

### Creating Your First Spec

1. **Choose workflow** (requirements-first, design-first, or bugfix)
2. **Create directory**: `.kiro/specs/{feature-name}/`
3. **Create config**: `.config.kiro` with UUID, workflowType, specType
4. **Write first document**:
   - Requirements-first: Start with `requirements.md`
   - Design-first: Start with `design.md`
   - Bugfix: Start with bug condition in `requirements.md`
5. **Follow workflow phases** (see Workflows section)
6. **Get user approval** at each phase checkpoint
7. **Complete all three documents** before implementation
8. **Execute tasks** from `tasks.md`
9. **Verify**:
   - All properties pass with ≥100 iterations
   - All unit tests pass
   - All tasks marked `[x]`
   - All requirements traced to design and tasks

### Quality Checklist

**Requirements**:
- [ ] All terms in Glossary
- [ ] All acceptance criteria use EARS patterns
- [ ] All criteria pass INCOSE rules (clarity, testability, completeness, positive)
- [ ] Each requirement has user story

**Design**:
- [ ] Architecture diagram present
- [ ] All components documented
- [ ] Data models include schemas and indexes
- [ ] PBT applicability assessed
- [ ] If PBT applicable: Prework completed, reflection performed, properties written
- [ ] If PBT not applicable: Alternative testing documented
- [ ] Each property references requirements
- [ ] Error handling mapped to HTTP statuses

**Tasks**:
- [ ] Hierarchical numbering used
- [ ] Each task references requirements
- [ ] Property test tasks reference property numbers
- [ ] Dependencies identified
- [ ] Effort estimated (optional)

**Implementation**:
- [ ] All property tests tagged with feature name + property number
- [ ] Property tests run ≥100 iterations
- [ ] All tests pass
- [ ] Code matches design architecture
- [ ] Error responses match design specification

---

## Conclusion

The Spec-Driven Development Workflow provides a systematic, repeatable process for transforming ideas into tested implementations. By following the workflow phases, using structured templates, applying EARS patterns and INCOSE rules, and integrating property-based testing, developers can create high-quality specifications that serve as living documentation throughout the development lifecycle.

**Next Steps**:
1. Read through the support-ticket-management spec as a reference example
2. Choose a workflow (requirements-first, design-first, or bugfix) for your next feature
3. Create the spec directory and config file
4. Follow the workflow phases with user checkpoints
5. Implement and verify using the testing strategy

For detailed guidance on any workflow phase, refer to the relevant section in this document.
