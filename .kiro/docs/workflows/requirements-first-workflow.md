# Requirements-First Workflow Guide

## Overview

The Requirements-First Workflow is a systematic approach for developing features when user needs and behaviors are clearly understood. This workflow progresses from **Requirements → Design → Tasks → Implementation**, ensuring that every implementation decision traces back to documented user needs.

**When to use this workflow:**
- Building user-facing features with clear user stories
- Stakeholders can articulate specific needs and expected behaviors
- User experience drives technical decisions
- Compliance or regulatory requirements exist

**When NOT to use this workflow:**
- Technical architecture is the primary driver (use Design-First instead)
- Fixing a specific bug (use Bugfix workflow instead)
- Exploring or prototyping (spike first, then create spec)

---

## Workflow Phases

The Requirements-First workflow consists of four sequential phases with user review checkpoints:

```
┌────────────────────────────────────────────────────────────┐
│ Phase 1: Requirements Gathering                            │
│ Input: User stories, stakeholder interviews                │
│ Output: requirements.md                                    │
│ Checkpoint: ✓ User approval required                       │
└──────────────────────┬─────────────────────────────────────┘
                       │
┌──────────────────────▼─────────────────────────────────────┐
│ Phase 2: Design Creation                                   │
│ Input: Approved requirements.md                            │
│ Output: design.md (with correctness properties)            │
│ Checkpoint: ✓ User review required                         │
└──────────────────────┬─────────────────────────────────────┘
                       │
┌──────────────────────▼─────────────────────────────────────┐
│ Phase 3: Task Breakdown                                    │
│ Input: Approved design.md                                  │
│ Output: tasks.md                                           │
│ Checkpoint: ✓ User review required                         │
└──────────────────────┬─────────────────────────────────────┘
                       │
┌──────────────────────▼─────────────────────────────────────┐
│ Phase 4: Implementation                                    │
│ Input: tasks.md                                            │
│ Output: Working, tested code                               │
│ Verification: All tests pass, all tasks completed          │
└────────────────────────────────────────────────────────────┘
```

---

## Phase 1: Requirements Gathering

### Goal

Create a complete, testable specification of user needs using structured patterns (EARS) and quality standards (INCOSE rules).

### Input

- User stories from stakeholders
- Feature requests or product requirements
- Interviews or surveys with end users
- Existing documentation or specifications

### Process

#### Step 1: Identify User Roles and Goals

List all user roles that interact with the feature and their primary goals.

**Example (ticket system):**
- **Support Agent**: Manage and resolve customer issues efficiently
- **Customer**: Track issue resolution status
- **Manager**: Monitor team workload and performance

#### Step 2: Write User Stories

For each capability, write a user story following this format:

```
As a [role], I want [capability], so that [benefit].
```

**Characteristics of good user stories:**
- Focused on a single capability
- Written from user perspective (not technical implementation)
- Includes the "why" (benefit/value)
- Testable through acceptance criteria

**Example:**
```markdown
### Requirement 1: Create Support Ticket

**User Story:** As a customer, I want to create a support ticket with a title and description, so that I can report my issue to the support team.
```

#### Step 3: Define Acceptance Criteria Using EARS Patterns

For each user story, write specific, testable acceptance criteria using EARS (Easy Approach to Requirements Syntax) patterns.

**The Six EARS Patterns:**

##### 1. Ubiquitous (Always-Active Behavior)

**Format:** `THE {system_name} SHALL {capability}`

**When to use:** Behaviors with no triggering event, always active

**Example:**
```
THE Repository SHALL persist all created tickets to the database.
```

##### 2. Event-Driven (Response to Trigger)

**Format:** `WHEN {trigger} [, {optional precondition}] THEN THE {system_name} SHALL {response}`

**When to use:** Specific events that trigger system responses

**Example:**
```
WHEN a user submits a valid ticket creation request, THEN THE API SHALL create a ticket with status OPEN and return HTTP 201.
```

##### 3. State-Driven (Behavior While in State)

**Format:** `WHILE {system state} [, {optional condition}] THE {system_name} SHALL {behavior}`

**When to use:** Behavior depends on system being in a specific state

**Example:**
```
WHILE a ticket is in OPEN status, THE UI SHALL display the "Start Work" button.
```

##### 4. Unwanted Event (Error Conditions)

**Format:** `IF {undesired condition/event} [, WHEN {trigger}] THEN THE {system_name} SHALL {response}`

**When to use:** Validation failures, error conditions, edge cases

**Example:**
```
IF a ticket creation request omits the title field, THEN THE Validator SHALL reject the request with HTTP 400 and error message "Title is required".
```

##### 5. Optional Feature (Conditional Capabilities)

**Format:** `WHERE {feature is included} [, {condition}] THE {system_name} SHALL {capability}`

**When to use:** Optional features, default behaviors, conditional capabilities

**Example:**
```
WHERE no priority is specified in the request, THE API SHALL default to MEDIUM priority.
```

##### 6. Complex (Combination of Patterns)

Combine multiple patterns for complex requirements using nested conditions.

**Example:**
```
WHILE the ticket is in IN_PROGRESS status,
WHEN a user clicks "Resolve",
IF all required fields are complete,
THEN THE System SHALL transition the ticket to RESOLVED status
  and send a notification to the assignee.
```

#### Step 4: Build Domain Glossary

Document all domain terms and system names used in requirements.

**Rules:**
- Every system name in EARS patterns (THE **API**, THE **Validator**) MUST be defined
- Use consistent terminology throughout
- Define acronyms and abbreviations
- Include business domain terms

**Example:**
```markdown
## Glossary

- **Ticket**: A record representing a customer support issue or request.
- **API**: The REST API service providing ticket management endpoints.
- **Validator**: The component responsible for validating incoming request data.
- **Status**: The current state of a ticket (OPEN, IN_PROGRESS, RESOLVED, CLOSED).
- **State_Machine**: The component enforcing valid status transitions.
```

#### Step 5: Review for INCOSE Quality Rules

Verify each requirement against the six INCOSE quality rules:

**Rule 1: Clarity** — Unambiguous, precise language
- ✓ Good: "THE API SHALL return tickets ordered by createdAt descending."
- ✗ Bad: "The system should sort tickets somehow."

**Rule 2: Testability** — Can be verified through testing, inspection, analysis, or demonstration
- ✓ Good: "THE Validator SHALL reject titles exceeding 255 characters with HTTP 400."
- ✗ Bad: "The UI should feel intuitive."

**Rule 3: Completeness** — All necessary information including error cases
- ✓ Good: Include both success path AND error conditions
- ✗ Bad: Only specifying happy path

**Rule 4: Positive Statements** — State what system SHALL do, not what it SHALL NOT do
- ✓ Good: "THE API SHALL validate priority against enum values LOW, MEDIUM, HIGH, CRITICAL."
- ✗ Bad: "THE API SHALL NOT accept invalid priority values."

**Rule 5: Consistency** — No contradictions with other requirements
- Verify: Cross-check all requirements for conflicts

**Rule 6: Atomic** — Each requirement addresses a single concern
- ✓ Good: Split "create and validate" into separate requirements
- ✗ Bad: Combining multiple unrelated behaviors in one requirement

#### Step 6: User Approval Checkpoint

Present `requirements.md` to stakeholders for review:
- Verify all user stories reflect actual needs
- Confirm acceptance criteria capture expected behaviors
- Clarify any ambiguous terms or conditions
- Add missing requirements discovered during review

**Do not proceed to Phase 2 until requirements are approved.**

### Output

A complete `requirements.md` document containing:
- Introduction paragraph
- Glossary with all domain terms
- Requirements section with:
  - Numbered requirements (1, 2, 3...)
  - User story for each requirement
  - Acceptance criteria using EARS patterns
  - All criteria validated against INCOSE rules

---

## Phase 2: Design Creation

### Goal

Create a technical design that satisfies all requirements, including architecture, components, data models, and correctness properties for property-based testing.

### Input

Approved `requirements.md`

### Process

#### Step 1: Identify Research Needs

Before writing design, identify knowledge gaps:
- New technologies or libraries to evaluate
- Architectural patterns to research
- Integration approaches to investigate
- Performance or security considerations

**Conduct research and summarize findings in conversation** (not in design document).

#### Step 2: Write Architecture Section

Define the high-level system structure:
- Layer responsibilities (UI, API, Business Logic, Data)
- Component organization
- Communication patterns
- Technology stack choices

**Include an architecture diagram** using ASCII art or Mermaid:

```
┌──────────┐
│   UI     │  React frontend
└────┬─────┘
     │ HTTP/REST
┌────▼─────┐
│   API    │  Spring Boot REST controllers
└────┬─────┘
     │
┌────▼─────┐
│ Services │  Business logic + State machine
└────┬─────┘
     │
┌────▼─────┐
│   Data   │  JPA repositories + H2 database
└──────────┘
```

#### Step 3: Define Components and Interfaces

Document all major components:
- REST endpoints with request/response formats
- Service classes and their responsibilities
- Component interfaces and contracts

**Example:**
```markdown
### REST Endpoints

**POST /api/tickets**
- Request: `CreateTicketRequest { title, description, priority? }`
- Response: `TicketDetailResponse` (HTTP 201)
- Errors: HTTP 400 (validation), HTTP 500 (server error)

**GET /api/tickets**
- Query params: `status?, keyword?`
- Response: `List<TicketSummaryResponse>` (HTTP 200)
```

#### Step 4: Define Data Models

Specify database schemas, entity definitions, indexes:

```markdown
### Ticket Entity

| Field | Type | Constraints | Description |
|---|---|---|---|
| id | Long | Primary Key, Auto-increment | Unique identifier |
| title | String | NOT NULL, Max 255 | Ticket title |
| description | String | NOT NULL | Full description |
| status | Enum | NOT NULL, Default OPEN | Current status |
| priority | Enum | NOT NULL, Default MEDIUM | Priority level |
| createdAt | Timestamp | NOT NULL | Creation timestamp |
| updatedAt | Timestamp | NOT NULL | Last update timestamp |

**Indexes:**
- Primary: `id`
- Composite: `(status, createdAt DESC)` — Supports filtered, ordered queries
```

#### Step 5: Assess Property-Based Testing Applicability

**Before writing correctness properties**, determine if PBT is appropriate for this feature.

**PBT works best for:**
- Pure functions with clear input/output behavior
- Universal properties holding across all inputs
- Parsers, serializers, data transformations, algorithms
- Business logic with invariants
- Large or infinite input spaces

**PBT is NOT appropriate for:**
- Infrastructure as Code (IaC) — Terraform, CDK, CloudFormation
- UI rendering and layout — React components, HTML templates
- Simple CRUD with no transformation — Direct database reads/writes
- Configuration validation — Schema checks
- Side-effect-only operations — Sending emails, logging
- External service integration with no logic

**Decision:**
- **If PBT applies**: Proceed to steps 6-8
- **If PBT does NOT apply**: Skip to step 9, document alternative testing approach

#### Step 6: Run Prework Tool (PBT Applicable)

**Purpose:** Analyze each acceptance criterion for testability classification.

**Process:**

For each acceptance criterion, determine:
1. **Does this test YOUR code or external services?**
   - External → INTEGRATION
2. **Does behavior vary meaningfully with input?**
   - No → EXAMPLE/SMOKE
3. **Would 100 iterations find more bugs than 2-3 examples?**
   - No → EXAMPLE/INTEGRATION
4. **Cost-effective to run 100+ times?**
   - No (high cost) → INTEGRATION or mock-based PROPERTY

**Output format:**
```
X.Y Criteria Name
  Thoughts: [Step-by-step reasoning about testability]
  Classification: PROPERTY | EXAMPLE | EDGE_CASE | INTEGRATION | SMOKE
  Test Strategy: [How to test, what varies, what's verified]
```

**Example:**
```
1.2 WHEN a valid ticket creation request is submitted, THEN THE API SHALL create a ticket with status OPEN
  Thoughts: This tests core creation logic. Status=OPEN is invariant regardless of input.
    Input varies (title, description, priority), output always has status=OPEN.
    Universal property: ALL created tickets have status OPEN.
  Classification: PROPERTY
  Test Strategy: Generate varied valid requests, verify status=OPEN always.

1.4 IF title exceeds 255 characters, THEN THE Validator SHALL reject with HTTP 400
  Thoughts: Validation logic, boundary condition. Can generate random strings of length 256+.
    Universal property: ALL titles >255 chars rejected.
  Classification: PROPERTY
  Test Strategy: Generate strings of length 256-1000, verify all rejected with 400.

1.1 THE Repository SHALL persist all created tickets
  Thoughts: This tests database integration (external). Would require real/mock DB.
    Not pure logic, more integration test.
  Classification: INTEGRATION
  Test Strategy: Unit test with in-memory database, verify persistence.
```

#### Step 7: Perform Property Reflection (PBT Applicable)

**Purpose:** Eliminate redundant properties by identifying logical implications.

**Process:**

1. List all properties identified as PROPERTY classification in prework
2. Identify redundant properties:
   - **Logically implied:** One property implies another
   - **Combinable:** Multiple properties can merge into comprehensive property
3. Mark redundant properties for removal
4. Ensure each remaining property provides unique validation

**Examples of redundancy:**

- "Adding task increases list length by 1" + "List contains added task"
  - **Decision:** Keep second (verifies presence), remove first (length implied)

- "Parsing preserves structure" + "Round-trip parsing is identity"
  - **Decision:** Keep round-trip (subsumes first)

- "New node has doc field" + "New node has owner field"
  - **Decision:** Combine into "New node has required fields"

#### Step 8: Write Correctness Properties Section (PBT Applicable)

**Format for each property:**

```markdown
### Property N: [Title]

*For any/all* [universal quantification statement]

**Validates: Requirements X.Y, X.Z**
```

**Common Property Patterns:**

**Pattern 1: Invariants** — Properties that remain constant

```markdown
### Property 1: Ticket Creation Invariants

*For any* valid ticket creation request, the created ticket SHALL have status OPEN, a non-null ID, and a non-null createdAt timestamp.

**Validates: Requirements 1.2, 1.3**
```

**Pattern 2: Round-Trip** — Operation followed by inverse returns to original

```markdown
### Property 5: Comment Serialization Round-Trip

*For any* valid comment, serializing to JSON and deserializing back SHALL produce an equivalent comment.

**Validates: Requirements 6.1**
```

**CRITICAL:** Always include round-trip properties for parsers and serializers.

**Pattern 3: Idempotence** — f(x) = f(f(x))

```markdown
### Property 9: Status Transition Idempotence

*For any* valid status transition, applying the same transition twice SHALL have the same effect as applying it once.

**Validates: Requirements 3.4**
```

**Pattern 4: Metamorphic** — Relationship between variants

```markdown
### Property 10: Keyword Search Correctness

*For any* keyword and ticket list, the search SHALL return exactly those tickets whose title or description contains the keyword.

**Validates: Requirements 7.2**
```

**Pattern 5: Error Conditions**

```markdown
### Property 4: Title Length Validation

*For any* string exceeding 255 characters, using it as a ticket title SHALL result in HTTP 400 rejection.

**Validates: Requirements 1.5**
```

#### Step 9: Write Error Handling Section

Map error conditions to HTTP status codes and response structures:

```markdown
## Error Handling

| Condition | HTTP Status | Error Response |
|---|---|---|
| Validation failure (missing/invalid fields) | 400 Bad Request | `ErrorResponse` with field errors |
| Invalid status transition | 422 Unprocessable Entity | `ErrorResponse` with message |
| Ticket not found | 404 Not Found | `ErrorResponse` with message |
| Server error | 500 Internal Server Error | `ErrorResponse` with generic message |

### ErrorResponse Structure

```json
{
  "message": "Human-readable error message",
  "timestamp": "ISO-8601 timestamp",
  "path": "Request path",
  "fieldErrors": [  // Optional, for validation errors
    { "field": "title", "message": "Title is required" }
  ]
}
```
```

#### Step 10: Write Testing Strategy Section

Document the testing approach:

```markdown
## Testing Strategy

### Property-Based Testing

**Library:** jqwik (Java)
**Configuration:** Minimum 100 iterations per property (`@Property(tries = 100)`)

**Property Test Coverage:**
- Property 1: Ticket creation invariants
- Property 4: Title length validation
- Property 8: State machine transitions
- Property 10: Keyword search correctness

**Test Tag Format:**
```java
// Feature: support-ticket-management, Property 1: Ticket creation invariants
@Property(tries = 100)
void ticketCreationInvariants(@ForAll @Size(min=1, max=255) String title) {
    // Test implementation
}
```

### Unit Testing

**Framework:** JUnit 5 + Spring Boot Test
**Database:** H2 in-memory for integration tests

**Unit Test Coverage:**
- Controller layer: Request validation, error handling
- Service layer: Business logic, state machine
- Repository layer: Database queries, indexes
```

#### Step 11: User Review Checkpoint

Present `design.md` to stakeholders and technical reviewers:
- Verify architecture satisfies requirements
- Confirm correctness properties capture critical behaviors
- Review error handling completeness
- Clarify any design decisions

**Do not proceed to Phase 3 until design is approved.**

### Output

A complete `design.md` document containing:
- Overview with goals and scope
- Architecture section with diagrams
- Components and interfaces
- Data models with schemas
- Correctness properties (if PBT applicable)
- Error handling specifications
- Testing strategy

---

## Phase 3: Task Breakdown

### Goal

Decompose implementation into hierarchical, traceable tasks with requirement references.

### Input

Approved `design.md`

### Process

#### Step 1: Identify Top-Level Tasks

Group implementation work into major areas:

```markdown
## Tasks

- [ ] 1. Set up project infrastructure
- [ ] 2. Implement data layer
- [ ] 3. Implement business logic
- [ ] 4. Implement API layer
- [ ] 5. Write property-based tests
- [ ] 6. Write unit tests
```

#### Step 2: Decompose into Subtasks

Break each top-level task into specific, actionable subtasks:

```markdown
- [ ] 2. Implement data layer
  - [ ] 2.1 Create Ticket entity class
    - Define fields matching design schema
    - Add JPA annotations
    - _Requirements: 1.1, 1.3_
  - [ ] 2.2 Create TicketRepository interface
    - Define query methods
    - _Requirements: 1.1, 7.1_
  - [ ] 2.3 Create database migration scripts
    - Flyway migration for ticket table
    - Add indexes per design
    - _Requirements: 1.1_
```

#### Step 3: Annotate with Requirement Traceability

Each task MUST reference the requirements it implements:

```markdown
- [ ] 3.2 Implement TicketService.createTicket()
  - Validate input fields
  - Set initial status to OPEN
  - Persist to repository
  - _Requirements: 1.2, 1.3, 1.5_
```

#### Step 4: Annotate Property Test Tasks

Tasks that implement property tests MUST reference property numbers:

```markdown
- [ ] 5.2 Write TicketCreationPropertyTest
  - Generate valid requests with varied inputs
  - Verify status=OPEN, non-null id/createdAt
  - Run 100 iterations
  - _Requirements: 1.2, 1.3_
  - _Property: 1_
```

#### Step 5: Identify Dependencies and Ordering

Note dependencies between tasks:

```markdown
- [ ] 3. Implement business logic
  - [ ] 3.1 Implement TicketStateMachine
    - Depends on: 2.1 (Ticket entity must exist)
    - _Requirements: 3.1, 3.2, 3.3_
```

#### Step 6: Estimate Effort (Optional)

Optionally add effort estimates to subtasks for planning purposes.

#### Step 7: User Review Checkpoint

Present `tasks.md` to team for review:
- Verify task granularity is appropriate
- Confirm requirement coverage is complete
- Identify any missing tasks
- Adjust ordering based on dependencies

**Do not proceed to Phase 4 until tasks are approved.**

### Output

A complete `tasks.md` document containing:
- Overview of implementation approach
- Hierarchical task list with numbering (1, 1.1, 1.2...)
- Status markers ([ ] not started, [-] in progress, [x] completed)
- Requirement traceability annotations
- Property number annotations for property test tasks
- Optional: Dependencies, effort estimates, notes

---

## Phase 4: Implementation

### Goal

Execute all tasks to produce working, tested code that satisfies requirements.

### Input

`tasks.md` with defined tasks

### Process

#### Step 1: Set Up Development Environment

Ensure all tools and dependencies are ready:
- IDE configured (Cursor/Kiro/VS Code)
- Build tools installed (Maven, npm, etc.)
- Property-based testing library added to project
- Test framework configured

#### Step 2: Execute Tasks Sequentially

For each task:

1. **Update task status to in progress:**
   ```markdown
   - [-] 2.1 Create Ticket entity class
   ```

2. **Implement the task:**
   - Write code according to design specifications
   - Follow project conventions and style
   - Add appropriate comments and documentation

3. **Write tests for the task:**
   - Unit tests for specific examples
   - Property tests if annotated with property number
   - Verify tests pass

4. **Update task status to completed:**
   ```markdown
   - [x] 2.1 Create Ticket entity class
   ```

#### Step 3: Write Property-Based Tests

For each property test task:

1. **Set up test class:**
   ```java
   import net.jqwik.api.*;
   
   class TicketCreationPropertyTest {
       // Test implementation
   }
   ```

2. **Add feature and property tag comment:**
   ```java
   // Feature: support-ticket-management, Property 1: Ticket creation invariants
   @Property(tries = 100)
   void ticketCreationInvariants(/* ... */) {
       // Test implementation
   }
   ```

3. **Configure test iterations (≥100):**
   - jqwik: `@Property(tries = 100)`
   - Hypothesis: `@settings(max_examples=100)`
   - fast-check: `{ numRuns: 100 }`

4. **Write property assertion:**
   ```java
   @Property(tries = 100)
   void ticketCreationInvariants(
       @ForAll @Size(min=1, max=255) String title,
       @ForAll String description
   ) {
       Ticket ticket = service.createTicket(new CreateTicketRequest(title, description, null));
       
       // Property: ALL created tickets have status OPEN
       assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
       assertThat(ticket.getId()).isNotNull();
       assertThat(ticket.getCreatedAt()).isNotNull();
   }
   ```

5. **Run property test and verify:**
   ```bash
   mvn test -Dtest=TicketCreationPropertyTest
   ```

   **Expected output:**
   ```
   TicketCreationPropertyTest:ticketCreationInvariants
     tries = 100                | OK
   ```

#### Step 4: Write Unit Tests

For each component:
- Test specific examples and edge cases
- Test error conditions
- Test integration points

```java
@Test
void createTicket_missingTitle_returns400() {
    CreateTicketRequest request = new CreateTicketRequest(null, "desc", null);
    
    ResponseEntity<?> response = controller.createTicket(request);
    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
}
```

#### Step 5: Verify Requirement Traceability

For each requirement:
1. Find all tasks with `_Requirements: X.Y_`
2. Verify all referenced tasks are completed
3. Verify code implements the requirement
4. Verify tests validate the requirement

#### Step 6: Run Full Test Suite

Before completing implementation:

```bash
# Run all tests
mvn test

# Verify all property tests run ≥100 iterations
# Verify all unit tests pass
# Verify code coverage (optional)
```

#### Step 7: Final Verification Checklist

- [ ] All tasks marked `[x]` completed
- [ ] All property tests pass with ≥100 iterations
- [ ] All unit tests pass
- [ ] All requirements traced to implementation
- [ ] Error handling matches design specification
- [ ] Code follows project conventions
- [ ] Documentation updated (if applicable)

### Output

- Working, tested code
- All tests passing
- Completed `tasks.md` (all tasks marked `[x]`)
- Implementation satisfies all requirements

---

## Checkpoints and Feedback Loops

### User Approval Checkpoints

**After Phase 1 (Requirements):**
- Review user stories with stakeholders
- Verify acceptance criteria capture expected behaviors
- Clarify any ambiguous terms
- **Decision:** Approve and proceed, or iterate on requirements

**After Phase 2 (Design):**
- Review architecture with technical team
- Verify correctness properties capture critical behaviors
- Review error handling completeness
- **Decision:** Approve and proceed, or iterate on design

**After Phase 3 (Tasks):**
- Review task breakdown with team
- Verify completeness and granularity
- Confirm ordering and dependencies
- **Decision:** Approve and proceed, or adjust tasks

### Iterative Refinement

**Returning to Requirements from Design:**

When to iterate:
- Design reveals missing requirements
- Technical constraints conflict with requirements
- Edge cases discovered during design

Process:
1. Add/modify requirements in `requirements.md`
2. Update affected sections in `design.md`
3. Seek user approval again
4. Proceed with updated documents

**Returning to Design from Tasks:**

When to iterate:
- Task breakdown reveals design gaps
- Implementation approach needs refinement
- Dependencies or constraints discovered

Process:
1. Update affected sections in `design.md`
2. Revise tasks in `tasks.md`
3. Seek approval again
4. Proceed with updated documents

**Handling Ambiguities:**

When ambiguity is discovered:
1. Document the ambiguity clearly
2. Present options to stakeholders
3. Get explicit decision
4. Update spec documents accordingly
5. Continue from updated state

---

## Common Patterns and Examples

### Pattern 1: Adding Feature to Existing System

**Scenario:** System exists, adding new capability (e.g., comments on tickets)

**Process:**
1. Write requirements focusing on new capability
2. In design, reference existing architecture
3. Document integration points with existing components
4. In tasks, identify modifications to existing code
5. Add property tests for new behavior only

**Example integration point in design:**
```markdown
### Comment Component

**Integrates with:**
- Existing TicketRepository — Add CommentRepository
- Existing TicketController — Add CommentController
- Existing Ticket entity — Add @OneToMany relationship
```

### Pattern 2: Feature with Complex Validation

**Scenario:** Multiple validation rules (e.g., status transitions)

**Requirements pattern:**
```markdown
#### Acceptance Criteria

1. WHEN transitioning from OPEN to IN_PROGRESS, THE State_Machine SHALL allow the transition.
2. IF transitioning from CLOSED to any status, THEN THE State_Machine SHALL reject with HTTP 422.
3. THE State_Machine SHALL validate all transitions before persisting.
```

**Design pattern:**
Use state machine correctness property:
```markdown
### Property 8: State Machine Transitions

*For any* valid transition (defined in allowedTransitions map), the transition SHALL succeed. *For any* disallowed transition, the State_Machine SHALL reject with HTTP 422.

**Validates: Requirements 3.1, 3.2, 3.3**
```

### Pattern 3: Feature with Search/Filter Logic

**Scenario:** Querying data with filters (e.g., keyword search)

**Requirements pattern:**
```markdown
#### Acceptance Criteria

1. WHEN a keyword query is provided, THE API SHALL return tickets whose title or description contains the keyword (case-insensitive).
2. WHEN no keyword is provided, THE API SHALL return all tickets.
3. THE API SHALL order results by createdAt descending.
```

**Design pattern:**
Use metamorphic property:
```markdown
### Property 10: Keyword Search Correctness

*For any* keyword string, the search results SHALL contain exactly those tickets whose title or description contains the keyword (case-insensitive).

**Validates: Requirements 7.2**
```

### Pattern 4: Feature with Default Values

**Scenario:** Optional fields with defaults (e.g., priority defaults to MEDIUM)

**Requirements pattern:**
```markdown
#### Acceptance Criteria

1. WHERE no priority is specified, THE API SHALL default to MEDIUM priority.
2. WHERE priority is provided, THE API SHALL use the provided value.
```

**Design pattern:**
Include in creation invariants property:
```markdown
### Property 1: Ticket Creation Invariants

*For any* valid ticket creation request, the created ticket SHALL have:
- Non-null priority (MEDIUM if not specified)
- Status OPEN
- Non-null id and createdAt
```

---

## Property-Based Testing Deep Dive

### Prework Tool Detailed Example

**Requirement 1.2:**
```
WHEN a valid ticket creation request is submitted, THEN THE API SHALL create a ticket with status OPEN and return HTTP 201.
```

**Prework analysis:**

```
1.2 Create ticket with status OPEN
  Thoughts:
    - This tests the core creation logic in TicketService
    - Input varies: different titles, descriptions, priorities
    - Output invariant: status is ALWAYS OPEN for new tickets
    - Universal property: "For all valid requests, created ticket has status OPEN"
    - Can run 100+ times with generated inputs (random strings, etc.)
    - Cost: Low (in-memory test, no external dependencies if using H2)
  Classification: PROPERTY
  Test Strategy:
    - Generate varied CreateTicketRequest objects (vary title, description, priority)
    - Call createTicket() for each
    - Assert status = OPEN for ALL results
    - Verify id and createdAt also non-null (creation invariants)
```

### Property Reflection Example

**Properties identified in prework:**
1. Property: "Created ticket has status OPEN"
2. Property: "Created ticket has non-null ID"
3. Property: "Created ticket has non-null createdAt"
4. Property: "Created ticket has non-null priority (default MEDIUM if not provided)"

**Reflection reasoning:**

These properties are all creation invariants that should hold for ANY ticket creation. They test the same operation (createTicket) with the same inputs.

**Decision:** Combine into single comprehensive property:

```markdown
### Property 1: Ticket Creation Invariants

*For any* valid ticket creation request, the created ticket SHALL have:
- Status OPEN
- Non-null ID
- Non-null createdAt timestamp
- Non-null priority (default MEDIUM if not specified)

**Validates: Requirements 1.2, 1.3, 1.5**
```

**Result:** 4 properties reduced to 1, eliminating redundancy while maintaining complete validation.

### When PBT Is NOT Appropriate: Examples

**Example 1: UI Component (NOT appropriate)**

Requirement:
```
WHEN the user views the ticket list, THE UI SHALL display ticket title, status, and priority for each ticket.
```

**Why NOT use PBT:**
- This is UI rendering behavior
- No universal property to test (rendering is deterministic)
- Better tested with snapshot tests or visual regression

**Alternative:** Snapshot test with 2-3 example tickets

---

**Example 2: External API Call (NOT appropriate)**

Requirement:
```
WHEN a ticket is created, THE System SHALL send a notification email to the support team.
```

**Why NOT use PBT:**
- Side-effect-only operation (sending email)
- External service integration (email provider)
- No meaningful input/output relationship to test

**Alternative:** Mock-based unit test verifying email service called with correct parameters

---

**Example 3: Simple CRUD (NOT appropriate)**

Requirement:
```
WHEN a user requests ticket details by ID, THE API SHALL return the ticket from the database.
```

**Why NOT use PBT:**
- Simple read operation, no transformation or logic
- Direct database query, framework-provided behavior
- Better tested with integration test

**Alternative:** Integration test with H2, create ticket → read ticket → verify fields match

---

## IDE Integration Tips

### Invoking Workflow in Cursor/Kiro/VS Code

**Start a requirements-first workflow:**
```
Create a new spec for [feature name] using requirements-first workflow
```

**The subagent will:**
1. Create `.kiro/specs/{feature-name}/` directory
2. Generate `.config.kiro` with UUID and workflow metadata
3. Guide you through Phase 1 (requirements gathering)
4. Pause for user approval checkpoint
5. Continue through phases 2-4 with checkpoints

### Navigating Spec Documents

**File tree navigation:**
```
.kiro/
└── specs/
    └── my-feature/
        ├── .config.kiro
        ├── requirements.md    ← Start here
        ├── design.md          ← Review after requirements
        └── tasks.md           ← Execute tasks
```

**Open all three in tabs** for easy cross-referencing.

**Use search (Cmd/Ctrl+F):**
- Find requirement: Search for "Requirement 1:"
- Find property: Search for "Property 5:"
- Find task: Search for "_Requirements: 1.2_"

### Running Tests from IDE

**Java/Maven:**
```bash
# Run all tests
mvn test

# Run specific property test class
mvn test -Dtest=TicketCreationPropertyTest

# Run all tests with "Property" in name
mvn test -Dtest=*PropertyTest
```

**Python/pytest:**
```bash
# Run all tests
pytest

# Run property tests
pytest tests/test_properties.py

# Run with Hypothesis verbose output
pytest tests/test_properties.py -v --hypothesis-show-statistics
```

**JavaScript/Jest:**
```bash
# Run all tests
npm test

# Run property tests
npm test -- property.test.js

# Run with fast-check verbosity
npm test -- --verbose
```

### Task Tracking

**Manual updates in `tasks.md`:**
```markdown
- [ ] 2.1 Create Ticket entity    ← Not started
- [-] 2.2 Create Repository       ← In progress
- [x] 2.3 Create migration        ← Completed
```

**Use IDE outline view** (if available) to navigate task hierarchy.

**Search for tasks by requirement:**
```
Search: "_Requirements: 1.2_"
Result: All tasks implementing requirement 1.2
```

---

## Quick Start Example

**Scenario:** Add a simple "Add priority labels" feature

### Phase 1: Requirements (5 minutes)

```markdown
## Glossary
- **Priority_Label**: A color-coded visual indicator for ticket priority.

## Requirements

### Requirement 1: Display Priority Labels

**User Story:** As a user, I want to see color-coded priority labels on tickets, so that I can quickly identify high-priority items.

#### Acceptance Criteria

1. WHEN viewing a ticket list, THE UI SHALL display a colored label for each ticket's priority.
2. THE UI SHALL use red for CRITICAL, orange for HIGH, yellow for MEDIUM, and green for LOW.
3. THE Label SHALL include the priority text ("CRITICAL", "HIGH", etc.).
```

**✓ User approves**

### Phase 2: Design (10 minutes)

```markdown
## Architecture

Frontend-only change: Add PriorityLabel component to TicketList.

## Components

### PriorityLabel Component

Props: `priority: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL"`

**Color mapping:**
| Priority | Color | Hex |
|---|---|---|
| LOW | Green | #10b981 |
| MEDIUM | Yellow | #fbbf24 |
| HIGH | Orange | #f97316 |
| CRITICAL | Red | #ef4444 |

## Correctness Properties

**PBT NOT applicable:**
- This is UI rendering (deterministic)
- No universal property to test
- Use snapshot test instead

## Testing Strategy

Snapshot test with 4 examples (one per priority level).
```

**✓ User approves**

### Phase 3: Tasks (5 minutes)

```markdown
## Tasks

- [ ] 1. Create PriorityLabel component
  - [ ] 1.1 Create PriorityLabel.tsx with color mapping
    - _Requirements: 1.2, 1.3_
  - [ ] 1.2 Write snapshot test with 4 priority levels
    - _Requirements: 1.1, 1.2, 1.3_
- [ ] 2. Integrate into TicketList
  - [ ] 2.1 Import PriorityLabel in TicketList.tsx
    - _Requirements: 1.1_
  - [ ] 2.2 Render PriorityLabel for each ticket
    - _Requirements: 1.1_
```

**✓ User approves**

### Phase 4: Implementation (20 minutes)

**Task 1.1:** Create component
**Task 1.2:** Write test
**Task 2.1:** Import
**Task 2.2:** Render

**Run tests:**
```bash
npm test -- PriorityLabel.test.tsx
```

**✓ All tests pass, all tasks complete**

---

## Completion Checklist

Use this checklist to verify your spec is complete:

### Requirements Document
- [ ] Introduction paragraph present
- [ ] Glossary includes all domain terms
- [ ] All system names in EARS patterns defined in glossary
- [ ] Each requirement has user story
- [ ] All acceptance criteria use EARS patterns
- [ ] All criteria pass INCOSE rules (clarity, testability, completeness, positive, consistency, atomic)
- [ ] User has approved requirements

### Design Document
- [ ] Overview with goals and scope
- [ ] Architecture section with diagram
- [ ] Components and interfaces documented
- [ ] Data models with schemas and indexes
- [ ] PBT applicability assessed
- [ ] If PBT applicable: Prework completed, reflection performed, correctness properties written
- [ ] If PBT not applicable: Alternative testing documented
- [ ] Each property references requirements
- [ ] Error handling section complete
- [ ] Testing strategy documented
- [ ] User has approved design

### Tasks Document
- [ ] Overview of implementation approach
- [ ] Hierarchical task numbering (1, 1.1, 1.2...)
- [ ] Each task references requirements (`_Requirements: X.Y_`)
- [ ] Property test tasks reference property numbers (`_Property: N_`)
- [ ] Dependencies identified
- [ ] User has approved tasks

### Implementation
- [ ] All tasks marked `[x]` completed
- [ ] All property tests pass with ≥100 iterations
- [ ] All unit tests pass
- [ ] Code matches design architecture
- [ ] Error responses match design specification
- [ ] All requirements traced to implementation

---

## Next Steps

1. **Read the full design document** for detailed guidance on EARS patterns, INCOSE rules, and property patterns
2. **Review the support-ticket-management spec** in `.kiro/specs/support-ticket-management/` as a complete example
3. **Start your first spec:**
   - Choose a feature idea
   - Invoke the requirements-first workflow subagent
   - Follow the phases with checkpoints
4. **Iterate and refine** based on user feedback at each phase

For questions or guidance on specific workflow phases, refer to the detailed sections in this document.
