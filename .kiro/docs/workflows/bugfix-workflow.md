# Bugfix Workflow Guide

## Overview

The Bugfix Workflow is a systematic approach for documenting and fixing bugs through falsifiable bug conditions and property-based reproduction tests. This workflow progresses from **Bug Condition → Reproduction → Design → Tasks → Implementation**, ensuring that bugs are clearly documented, reproducible, and verifiably fixed.

**When to use this workflow:**
- Fixing reported bugs with incorrect system behavior
- Addressing edge cases discovered in production
- Correcting logic errors in existing code
- Fixing validation or business rule violations

**When NOT to use this workflow:**
- Adding new features (use Requirements-First or Design-First instead)
- Refactoring without behavior change (use Requirements-First to document current behavior)
- Performance optimization (use Design-First for architectural changes)
- Exploratory prototyping (spike first, then create spec if needed)

---

## Workflow Phases

The Bugfix workflow consists of five sequential phases with user review checkpoints:

```
┌────────────────────────────────────────────────────────────┐
│ Phase 1: Bug Condition Definition                          │
│ Input: Bug report, reproduction steps                      │
│ Output: requirements.md with bug condition                 │
│ Checkpoint: ✓ User review required                         │
└──────────────────────┬─────────────────────────────────────┘
                       │
┌──────────────────────▼─────────────────────────────────────┐
│ Phase 2: Reproduction Test                                 │
│ Input: Bug condition from requirements.md                  │
│ Output: Failing property-based test                        │
│ Verification: Test fails on current code                   │
└──────────────────────┬─────────────────────────────────────┘
                       │
┌──────────────────────▼─────────────────────────────────────┐
│ Phase 3: Design Fix Approach                               │
│ Input: Bug condition and failing test                      │
│ Output: design.md with fix approach                        │
│ Checkpoint: ✓ User review required                         │
└──────────────────────┬─────────────────────────────────────┘
                       │
┌──────────────────────▼─────────────────────────────────────┐
│ Phase 4: Task Breakdown                                    │
│ Input: Approved design.md                                  │
│ Output: tasks.md                                           │
│ Checkpoint: ✓ User review required                         │
└──────────────────────┬─────────────────────────────────────┘
                       │
┌──────────────────────▼─────────────────────────────────────┐
│ Phase 5: Implementation + Verification                     │
│ Input: tasks.md                                            │
│ Output: Fixed code, passing tests                          │
│ Verification: Reproduction test now passes                 │
└────────────────────────────────────────────────────────────┘
```

---

## Phase 1: Bug Condition Definition

### Goal

Write a falsifiable statement describing the incorrect behavior, expected correct behavior, and root cause (if known).

### Input

- Bug report or issue description
- Reproduction steps from user or QA
- System logs or error messages
- Stack traces or diagnostic information

### Process

#### Step 1: Understand the Bug Report

Gather all available information about the bug:
- What is the user experiencing?
- What steps trigger the bug?
- What is the actual behavior?
- What was the expected behavior?
- What environment or conditions reproduce it?

**Example bug report:**
```
When a support ticket is in CLOSED status, the API allows 
transitioning it to IN_PROGRESS without returning an error. 
This violates the rule that closed tickets cannot be reopened 
through normal status transitions.
```

#### Step 2: Write the Bug Condition as a Falsifiable Statement

A bug condition MUST be a clear, testable statement with three components:

1. **Current (Incorrect) Behavior**: What the system does now
2. **Expected (Correct) Behavior**: What the system should do
3. **Root Cause** (if known): Why the bug exists

**Format:**
```markdown
## Bug Condition

**Current Behavior**: [Specific description of incorrect behavior]

**Expected Behavior**: [Specific description of correct behavior using EARS patterns]

**Root Cause**: [Technical explanation of why bug exists, if known]
```

**Characteristics of good bug conditions:**
- **Specific**: References exact components, states, or inputs
- **Falsifiable**: Can be proven true or false through testing
- **Behavioral**: Describes observable system behavior, not implementation
- **Complete**: Includes both actual and expected behavior

#### Step 3: Write Bug Condition Examples

**Example 1: State Machine Bug**

```markdown
## Bug Condition

**Current Behavior**: When a ticket status is CLOSED, the API allows transitioning to IN_PROGRESS without error. The status change is accepted and persisted to the database.

**Expected Behavior**: Transitions from CLOSED to any other status SHALL be rejected with HTTP 422 (Unprocessable Entity) and an error message indicating the transition is not allowed.

**Root Cause**: The `TicketStateMachine.isValidTransition()` method does not check if the source status is a terminal state (CLOSED). The method only validates transitions defined in the `allowedTransitions` map, but CLOSED is not present as a key in the map, causing the validation to be skipped.
```

**Example 2: Validation Bug**

```markdown
## Bug Condition

**Current Behavior**: When a ticket is created with a title containing only whitespace characters (spaces, tabs, newlines), the API accepts the request and creates a ticket with a blank title.

**Expected Behavior**: IF a ticket creation request contains a title with only whitespace, THEN THE Validator SHALL reject the request with HTTP 400 and error message "Title cannot be blank".

**Root Cause**: The `@NotNull` annotation on the `title` field only checks for null values, not for blank/empty strings. Additional validation using `@NotBlank` or custom validator is required.
```

**Example 3: Edge Case Bug**

```markdown
## Bug Condition

**Current Behavior**: When keyword search is performed with an empty string (""), the API returns zero results even though tickets exist in the database.

**Expected Behavior**: WHEN keyword search is performed with an empty string, THE API SHALL return all tickets (equivalent to no filter applied), ordered by createdAt descending.

**Root Cause**: The `TicketRepository.findByKeyword()` method uses a LIKE query that treats empty strings as a literal match instead of a wildcard. The query should check for empty/null keyword before applying the LIKE filter.
```

**Example 4: Data Integrity Bug**

```markdown
## Bug Condition

**Current Behavior**: When a comment is added to a ticket, the ticket's `updatedAt` timestamp is not automatically updated, causing the ticket to appear unchanged in listings ordered by last update.

**Expected Behavior**: WHEN a comment is added to a ticket, THE System SHALL automatically update the parent ticket's `updatedAt` timestamp to the current time.

**Root Cause**: The Comment entity has no cascade or lifecycle callback configured to touch the parent Ticket entity. JPA does not automatically update parent timestamps when child entities are added.
```

#### Step 4: Create Requirements Document

Organize the bug condition in a requirements document following the standard template:

```markdown
# Requirements Document — [Bug Name]

## Introduction

This document describes a bug discovered in [system/component] where [brief summary of incorrect behavior].

---

## Glossary

- **[Term]**: [Definition of technical or domain terms used in bug condition]
- **[System_Name]**: [Definition of system components referenced]

---

## Bug Condition

**Current Behavior**: [Detailed description of incorrect behavior]

**Expected Behavior**: [EARS pattern statement of correct behavior]

**Root Cause**: [Technical explanation if known, or "Under investigation" if not]

---

## Requirements

### Requirement 1: [Fix Description]

**User Story:** As a [role], I want [correct behavior], so that [benefit of fix].

#### Acceptance Criteria

1. [EARS pattern statement describing correct behavior]
2. [EARS pattern for error condition if applicable]
3. [EARS pattern for edge cases related to bug]
```

**Complete Example:**

```markdown
# Requirements Document — Closed Ticket Transition Bug

## Introduction

This document describes a bug in the support ticket state machine where transitions from CLOSED status are incorrectly allowed, violating the business rule that closed tickets cannot be reopened through normal status transitions.

---

## Glossary

- **State_Machine**: The component enforcing valid status transitions for tickets.
- **Terminal_State**: A status from which no further transitions are allowed (e.g., CLOSED).
- **Status**: The current state of a ticket (OPEN, IN_PROGRESS, RESOLVED, CLOSED).
- **Transition**: A change from one status to another status.

---

## Bug Condition

**Current Behavior**: When a ticket status is CLOSED, the API allows transitioning to IN_PROGRESS without error. The status change is accepted and persisted to the database.

**Expected Behavior**: Transitions from CLOSED to any other status SHALL be rejected with HTTP 422 (Unprocessable Entity) and an error message indicating the transition is not allowed.

**Root Cause**: The `TicketStateMachine.isValidTransition()` method does not check if the source status is a terminal state. The method only validates transitions defined in the `allowedTransitions` map, but CLOSED is not present as a key in the map, causing validation to be skipped.

---

## Requirements

### Requirement 1: Enforce Terminal State Transitions

**User Story:** As a support manager, I want closed tickets to remain closed unless explicitly reopened through an administrative action, so that the ticket workflow integrity is maintained and historical data is not corrupted.

#### Acceptance Criteria

1. WHEN a status transition is requested from CLOSED to any other status, THEN THE State_Machine SHALL reject the transition with HTTP 422.
2. THE State_Machine SHALL include an error message indicating "Cannot transition from CLOSED status".
3. THE Repository SHALL NOT persist any changes when a transition from CLOSED is attempted.
4. IF the source status is not CLOSED, THEN THE State_Machine SHALL apply normal transition validation rules.
```

#### Step 5: User Review Checkpoint

Present the `requirements.md` to stakeholders:
- Confirm the bug condition accurately describes the issue
- Verify expected behavior aligns with business rules
- Review root cause analysis (if known)
- Clarify any ambiguous terms

**Do not proceed to Phase 2 until bug condition is approved.**

### Output

A complete `requirements.md` document containing:
- Introduction describing the bug
- Glossary with relevant technical terms
- Bug Condition section with current behavior, expected behavior, and root cause
- Requirements section with user story and EARS acceptance criteria

---

## Phase 2: Reproduction Test

### Goal

Write a property-based test that currently FAILS, demonstrating the bug, and will PASS once the bug is fixed.

### Input

Bug condition from `requirements.md`

### Process

#### Step 1: Identify the Testable Property

From the bug condition's "Expected Behavior", extract a universal property that should hold:

**Bug condition:**
```
Expected Behavior: Transitions from CLOSED to any other status 
SHALL be rejected with HTTP 422.
```

**Property:**
```
For any target status (OPEN, IN_PROGRESS, RESOLVED), attempting 
to transition from CLOSED SHALL result in HTTP 422 rejection.
```

#### Step 2: Choose Test Strategy

Determine the appropriate test type:

**Property-Based Test** (preferred for bugfixes):
- Use when the bug affects a range of inputs
- Use when you can generate varied test cases
- Use when the bug is about universal behavior violation

**Example-Based Test** (fallback):
- Use when the bug is specific to exact input values
- Use when property test is too complex
- Use when bug involves external integration

**For most bugfixes, prefer property-based tests** to verify the fix works across all relevant inputs.

#### Step 3: Write the Reproduction Test

**Test Structure:**

```java
// Feature: {feature-name}, Bug: {bug-name}
@Property(tries = 100)
void bugReproductionTest(@ForAll InputGenerator inputs) {
    // 1. Arrange: Set up conditions that trigger the bug
    // 2. Act: Perform the operation that exhibits the bug
    // 3. Assert: Verify expected (correct) behavior
}
```

**Example 1: State Machine Bug**

```java
// Feature: support-ticket-management, Bug: closed-ticket-transition
@Property(tries = 100)
void closedTicketsCannotTransition(
    @ForAll("nonClosedStatuses") TicketStatus targetStatus
) {
    // Arrange: Create a ticket and transition to CLOSED
    Ticket ticket = createAndSaveTicket();
    transitionTo(ticket, TicketStatus.CLOSED);
    
    // Act: Attempt to transition from CLOSED to any other status
    ResponseEntity<?> response = ticketController.transitionStatus(
        ticket.getId(),
        new StatusTransitionRequest(targetStatus)
    );
    
    // Assert: Should be rejected with HTTP 422
    assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody())
        .asInstanceOf(InstanceOfAssertFactories.type(ErrorResponse.class))
        .extracting(ErrorResponse::getMessage)
        .asString()
        .contains("Cannot transition from CLOSED");
}

@Provide
Arbitrary<TicketStatus> nonClosedStatuses() {
    return Arbitraries.of(
        TicketStatus.OPEN,
        TicketStatus.IN_PROGRESS,
        TicketStatus.RESOLVED
    );
}
```

**Example 2: Validation Bug**

```java
// Feature: support-ticket-management, Bug: whitespace-title-validation
@Property(tries = 100)
void blankTitlesAreRejected(@ForAll("whitespaceStrings") String blankTitle) {
    // Arrange: Create request with whitespace-only title
    CreateTicketRequest request = new CreateTicketRequest(
        blankTitle,
        "Valid description",
        Priority.MEDIUM
    );
    
    // Act: Attempt to create ticket
    ResponseEntity<?> response = ticketController.createTicket(request);
    
    // Assert: Should be rejected with HTTP 400
    assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody())
        .asInstanceOf(InstanceOfAssertFactories.type(ErrorResponse.class))
        .extracting(ErrorResponse::getFieldErrors)
        .asList()
        .anySatisfy(error -> {
            assertThat(error.getField()).isEqualTo("title");
            assertThat(error.getMessage()).contains("cannot be blank");
        });
}

@Provide
Arbitrary<String> whitespaceStrings() {
    return Arbitraries.of(" ", "  ", "\t", "\n", "   \t\n  ");
}
```

**Example 3: Edge Case Bug**

```java
// Feature: support-ticket-management, Bug: empty-keyword-search
@Property(tries = 100)
void emptyKeywordReturnsAllTickets() {
    // Arrange: Create multiple tickets
    List<Ticket> allTickets = createMultipleTickets(10);
    
    // Act: Search with empty string
    ResponseEntity<List<TicketSummaryResponse>> response = 
        ticketController.listTickets("", null);
    
    // Assert: Should return all tickets
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(allTickets.size());
    assertThat(response.getBody())
        .extracting(TicketSummaryResponse::getId)
        .containsExactlyInAnyOrderElementsOf(
            allTickets.stream().map(Ticket::getId).toList()
        );
}
```

#### Step 4: Run the Reproduction Test

Execute the test and **verify it FAILS on the current (buggy) code**:

```bash
mvn test -Dtest=ClosedTicketTransitionBugTest
```

**Expected output (test SHOULD fail):**

```
ClosedTicketTransitionBugTest:closedTicketsCannotTransition
  tries = 100                | FAILED

Assertion failed for status transition from CLOSED to IN_PROGRESS:
Expected: HttpStatus.UNPROCESSABLE_ENTITY (422)
Actual: HttpStatus.OK (200)
```

**CRITICAL:** The reproduction test MUST fail on unfixed code. If it passes, the test does not reproduce the bug.

#### Step 5: Document the Failure

Capture the test failure output to confirm the bug is reproduced:

**In commit message or PR description:**
```
Reproduction test written for closed ticket transition bug.

Test currently FAILS as expected:
- Attempted transitions from CLOSED to IN_PROGRESS return HTTP 200
- Expected HTTP 422 with error message
- Bug confirmed: State machine does not validate terminal states

Test will PASS once fix is implemented.
```

#### Step 6: Document in Design (Optional Early Design)

If design approach is already clear, you may create an initial `design.md` documenting the test:

```markdown
# Design Document — Closed Ticket Transition Bug Fix

## Overview

Fix for bug where transitions from CLOSED status are incorrectly allowed.

## Reproduction Test

**Test:** `ClosedTicketTransitionBugTest.closedTicketsCannotTransition()`

**Property:** For any target status (OPEN, IN_PROGRESS, RESOLVED), attempting to transition from CLOSED SHALL result in HTTP 422.

**Current Result:** FAILS (returns HTTP 200)

**Expected Result After Fix:** PASSES (returns HTTP 422)
```

### Output

- Failing reproduction test added to codebase
- Test failure documented and confirmed
- Test code reviewed for correctness
- Optional: Initial design document started

---

## Phase 3: Design Fix Approach

### Goal

Document the technical approach for fixing the bug, including code changes, updated correctness properties, and verification strategy.

### Input

- Bug condition from `requirements.md`
- Failing reproduction test from Phase 2

### Process

#### Step 1: Analyze Root Cause

If root cause was not identified in Phase 1, investigate now:

1. Review the failing test and actual vs. expected behavior
2. Examine the relevant code components
3. Trace the execution path that leads to the bug
4. Identify the specific code location or logic error

**Document findings:**
```markdown
## Root Cause Analysis

**Component:** TicketStateMachine.isValidTransition()

**Issue:** The method checks if a transition exists in the `allowedTransitions` map:

```java
public boolean isValidTransition(TicketStatus from, TicketStatus to) {
    return allowedTransitions.getOrDefault(from, Set.of()).contains(to);
}
```

When `from` is CLOSED, the map returns an empty set (since CLOSED 
is not a key), and the contains() check returns false. However, the 
method returns false (not valid), but the calling code treats 
"not found in map" as "not explicitly forbidden", allowing the transition.

**Actual Root Cause:** The calling code in TicketService does not 
properly handle the validation result. It checks `isValidTransition()` 
but only throws an exception if explicitly false, treating "not in map" 
as allowed.
```

#### Step 2: Design the Fix

Specify the exact code changes needed:

```markdown
## Fix Approach

### Option 1: Add Terminal State Check (Recommended)

Modify `TicketStateMachine.isValidTransition()` to explicitly reject transitions from terminal states:

```java
public boolean isValidTransition(TicketStatus from, TicketStatus to) {
    // Terminal states (CLOSED) cannot transition to any other status
    if (from == TicketStatus.CLOSED) {
        return false;
    }
    
    // Check allowed transitions for non-terminal states
    return allowedTransitions.getOrDefault(from, Set.of()).contains(to);
}
```

**Pros:**
- Clear, explicit logic
- Self-documenting intent
- Easy to extend with more terminal states
- Minimal change surface

**Cons:**
- Hardcodes CLOSED as terminal state

### Option 2: Define Terminal States Set

Create a set of terminal states and check membership:

```java
private static final Set<TicketStatus> TERMINAL_STATES = 
    Set.of(TicketStatus.CLOSED);

public boolean isValidTransition(TicketStatus from, TicketStatus to) {
    if (TERMINAL_STATES.contains(from)) {
        return false;
    }
    return allowedTransitions.getOrDefault(from, Set.of()).contains(to);
}
```

**Pros:**
- More flexible (easy to add terminal states)
- Clear declaration of business rule
- Testable independently

**Cons:**
- Slightly more complex

**Decision:** Use Option 2 for better maintainability.
```

#### Step 3: Update Error Handling (if needed)

Specify error response changes:

```markdown
## Error Response Update

Current error response when validation fails:
```json
{
  "message": "Invalid status transition"
}
```

Updated error response to include more context:
```json
{
  "message": "Cannot transition from CLOSED status. Closed tickets must be reopened administratively.",
  "currentStatus": "CLOSED",
  "attemptedStatus": "IN_PROGRESS"
}
```

**Implementation:** Update `InvalidStatusTransitionException` to include source and target status fields.
```

#### Step 4: Document Correctness Properties (if applicable)

If the fix introduces new properties or updates existing ones:

```markdown
## Correctness Properties

### Property 1: Terminal State Invariant

*For any* status classified as terminal (CLOSED), attempting to transition to any other status SHALL be rejected.

**Validates: Requirements 1.1, 1.2**

### Property 2: Non-Terminal State Transitions

*For any* non-terminal status, transitions SHALL follow the allowedTransitions map. Valid transitions SHALL succeed; invalid transitions SHALL be rejected with HTTP 422.

**Validates: Requirements 1.3, 1.4**
```

#### Step 5: Specify Testing Strategy

Document how the fix will be verified:

```markdown
## Testing Strategy

### Reproduction Test Verification

**Primary verification:** `ClosedTicketTransitionBugTest.closedTicketsCannotTransition()` MUST pass after fix.

**Process:**
1. Implement fix per design
2. Run reproduction test
3. Verify test passes with 100 iterations
4. Verify error message matches expected format

### Regression Testing

**Additional tests to verify fix does not break existing behavior:**

1. **Valid transition tests**: Ensure OPEN → IN_PROGRESS, IN_PROGRESS → RESOLVED, etc. still work
2. **Invalid transition tests**: Ensure invalid non-terminal transitions still rejected
3. **Integration tests**: Full ticket lifecycle still functions correctly

**Property Test:** Existing `StateMachinePropertyTest` should still pass, validating that all previously valid transitions remain valid.

### Manual Verification (if needed)

1. Start application
2. Create ticket (status OPEN)
3. Transition to CLOSED
4. Attempt to transition to IN_PROGRESS via API
5. Verify HTTP 422 response with correct error message
```

#### Step 6: Identify Side Effects and Risks

Document potential impacts of the fix:

```markdown
## Risks and Side Effects

### Potential Side Effects

1. **Breaking change:** If any code path currently relies on transitioning from CLOSED, it will now fail. 
   - **Mitigation:** Search codebase for transitions from CLOSED; verify none exist except reproduction test.

2. **API behavior change:** Clients expecting HTTP 200 for closed transitions will now receive HTTP 422.
   - **Mitigation:** This is intended; bug fix corrects incorrect behavior.

### Regression Risks

- Low risk: Fix is localized to state machine validation logic
- No database schema changes required
- No API contract changes (only error response)

### Performance Impact

- Negligible: Adds one Set.contains() check per validation
```

#### Step 7: User Review Checkpoint

Present `design.md` to technical reviewers:
- Verify fix approach correctly addresses root cause
- Review error handling changes
- Confirm testing strategy is comprehensive
- Assess risks and side effects

**Do not proceed to Phase 4 until design is approved.**

### Output

A complete `design.md` document containing:
- Root cause analysis
- Fix approach with code examples
- Error handling updates (if applicable)
- Correctness properties (if applicable)
- Testing strategy including reproduction test verification
- Risks and side effects assessment

---

## Phase 4: Task Breakdown

### Goal

Create a detailed implementation plan with tasks for fixing the bug and verifying the fix.

### Input

Approved `design.md`

### Process

#### Step 1: Identify Top-Level Tasks

Group work into major categories:

```markdown
## Tasks

- [ ] 1. Implement state machine fix
- [ ] 2. Update error handling
- [ ] 3. Verify reproduction test passes
- [ ] 4. Add regression tests
- [ ] 5. Update documentation
```

#### Step 2: Decompose into Subtasks

Break each task into specific, actionable steps:

```markdown
- [ ] 1. Implement state machine fix
  - [ ] 1.1 Add TERMINAL_STATES constant to TicketStateMachine
    - Define Set containing TicketStatus.CLOSED
    - _Requirements: 1.1_
  - [ ] 1.2 Update isValidTransition() method
    - Add terminal state check before allowed transitions check
    - Return false if source status is terminal
    - _Requirements: 1.1, 1.2_
  - [ ] 1.3 Add unit test for terminal state validation
    - Test CLOSED → OPEN, CLOSED → IN_PROGRESS, CLOSED → RESOLVED
    - Verify all return false
    - _Requirements: 1.1_

- [ ] 2. Update error handling
  - [ ] 2.1 Update InvalidStatusTransitionException
    - Add fields: currentStatus, attemptedStatus
    - Update constructor and message format
    - _Requirements: 1.2_
  - [ ] 2.2 Update GlobalExceptionHandler
    - Include currentStatus and attemptedStatus in ErrorResponse
    - Update error message to mention terminal state
    - _Requirements: 1.2_

- [ ] 3. Verify reproduction test passes
  - [ ] 3.1 Run ClosedTicketTransitionBugTest
    - Verify test now passes with 100 iterations
    - Verify error message format matches expected
    - _Requirements: 1.1, 1.2_

- [ ] 4. Add regression tests
  - [ ] 4.1 Verify existing StateMachinePropertyTest still passes
    - Run full property test suite
    - Verify no previously valid transitions broken
    - _Requirements: 1.4_
  - [ ] 4.2 Add unit test for valid non-terminal transitions
    - Test OPEN → IN_PROGRESS → RESOLVED
    - Verify these still succeed
    - _Requirements: 1.4_

- [ ] 5. Update documentation
  - [ ] 5.1 Update TicketStateMachine class documentation
    - Document terminal state concept
    - Explain transition validation logic
  - [ ] 5.2 Update API documentation (if applicable)
    - Document HTTP 422 for terminal state transitions
    - Include example error response
```

#### Step 3: Annotate with Requirement Traceability

Each task references requirements from the bug condition:

```markdown
- [ ] 1.2 Update isValidTransition() method
  - Add terminal state check before allowed transitions check
  - Return false if source status is terminal
  - _Requirements: 1.1, 1.2_
```

#### Step 4: Mark Reproduction Test Task

Clearly identify the task that verifies the bug is fixed:

```markdown
- [ ] 3. Verify reproduction test passes ⭐ BUG FIX VERIFICATION
  - [ ] 3.1 Run ClosedTicketTransitionBugTest
    - This test currently FAILS and MUST PASS after fix
    - Verify test passes with 100 iterations
    - Verify error message format matches expected
    - _Requirements: 1.1, 1.2_
    - _Bug: closed-ticket-transition_
```

#### Step 5: Identify Dependencies

Note task dependencies:

```markdown
- [ ] 3. Verify reproduction test passes ⭐
  - Depends on: 1.1, 1.2, 2.1, 2.2 (fix must be implemented first)
```

#### Step 6: User Review Checkpoint

Present `tasks.md` to team:
- Verify task breakdown is complete
- Confirm fix implementation steps are correct
- Review verification and regression test plans

**Do not proceed to Phase 5 until tasks are approved.**

### Output

A complete `tasks.md` document containing:
- Overview of bug fix approach
- Hierarchical task list with clear fix and verification tasks
- Reproduction test verification task clearly marked
- Regression test tasks to prevent side effects
- Requirement traceability for all tasks

---

## Phase 5: Implementation + Verification

### Goal

Execute tasks to fix the bug and verify the reproduction test passes, confirming the bug is resolved.

### Input

`tasks.md` with defined tasks

### Process

#### Step 1: Implement the Fix

Execute fix implementation tasks:

```java
// Task 1.1: Add TERMINAL_STATES constant
public class TicketStateMachine {
    private static final Set<TicketStatus> TERMINAL_STATES = 
        Set.of(TicketStatus.CLOSED);
    
    // ... rest of class
}
```

```java
// Task 1.2: Update isValidTransition() method
public boolean isValidTransition(TicketStatus from, TicketStatus to) {
    // Terminal states cannot transition to any other status
    if (TERMINAL_STATES.contains(from)) {
        return false;
    }
    
    // Check allowed transitions for non-terminal states
    return allowedTransitions.getOrDefault(from, Set.of()).contains(to);
}
```

```java
// Task 2.1: Update exception
public class InvalidStatusTransitionException extends RuntimeException {
    private final TicketStatus currentStatus;
    private final TicketStatus attemptedStatus;
    
    public InvalidStatusTransitionException(
        TicketStatus currentStatus, 
        TicketStatus attemptedStatus
    ) {
        super(String.format(
            "Cannot transition from %s status. Current: %s, Attempted: %s",
            currentStatus == TicketStatus.CLOSED ? "CLOSED" : "invalid",
            currentStatus,
            attemptedStatus
        ));
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }
    
    // Getters
}
```

#### Step 2: Run Reproduction Test ⭐

**This is the critical verification step.**

Execute the reproduction test that was failing in Phase 2:

```bash
mvn test -Dtest=ClosedTicketTransitionBugTest
```

**Expected output (test SHOULD now PASS):**

```
ClosedTicketTransitionBugTest:closedTicketsCannotTransition
  tries = 100                | OK
  
All property checks passed.
```

**If test still fails:**
1. Review test failure output
2. Verify fix was implemented correctly
3. Check if root cause analysis was accurate
4. Debug and iterate on fix
5. Rerun test until it passes

**If test passes:**
✅ Bug is confirmed fixed. The reproduction test verifies the expected behavior now holds.

#### Step 3: Run Regression Tests

Verify the fix does not break existing functionality:

```bash
# Run existing property tests
mvn test -Dtest=StateMachinePropertyTest

# Run unit test suite
mvn test

# Verify all tests pass
```

**Expected:**
- All previously passing tests still pass
- No new test failures introduced
- Existing valid transitions still work correctly

#### Step 4: Manual Verification (if applicable)

Perform manual testing to verify the fix in a running system:

1. Start the application
2. Create a ticket and transition to CLOSED
3. Attempt to transition to IN_PROGRESS via API or UI
4. Verify HTTP 422 response with correct error message
5. Verify valid transitions still work (OPEN → IN_PROGRESS, etc.)

#### Step 5: Update Task Status

Mark all tasks as completed:

```markdown
- [x] 1. Implement state machine fix
  - [x] 1.1 Add TERMINAL_STATES constant
  - [x] 1.2 Update isValidTransition() method
  - [x] 1.3 Add unit test for terminal state validation

- [x] 2. Update error handling
  - [x] 2.1 Update InvalidStatusTransitionException
  - [x] 2.2 Update GlobalExceptionHandler

- [x] 3. Verify reproduction test passes ⭐
  - [x] 3.1 Run ClosedTicketTransitionBugTest — PASSES ✅

- [x] 4. Add regression tests
  - [x] 4.1 Verify StateMachinePropertyTest still passes
  - [x] 4.2 Add unit test for valid non-terminal transitions

- [x] 5. Update documentation
  - [x] 5.1 Update TicketStateMachine class documentation
  - [x] 5.2 Update API documentation
```

#### Step 6: Final Verification Checklist

- [x] Reproduction test passes (100 iterations)
- [x] All regression tests pass
- [x] Manual verification confirms fix (if applicable)
- [x] Error messages match design specification
- [x] No new bugs introduced
- [x] Code follows project conventions
- [x] Documentation updated

### Output

- Fixed code implemented and tested
- Reproduction test passes, confirming bug is fixed
- All regression tests pass
- Completed `tasks.md` with all tasks marked done
- Bug verified as resolved

---

## Writing Falsifiable Bug Conditions

### What Makes a Bug Condition Falsifiable?

A falsifiable bug condition can be proven true or false through testing. It has:

1. **Observable behavior**: Describes what the system does, not internal implementation
2. **Specific inputs/conditions**: Clear about when the bug occurs
3. **Expected vs. actual**: States both incorrect and correct behavior
4. **Testable**: Can write a test that passes when bug is fixed, fails when bug exists

### Good vs. Bad Bug Conditions

**Bad (Not Falsifiable):**
```
The state machine is broken.
```
❌ Too vague, no observable behavior, can't test

**Good (Falsifiable):**
```
Current Behavior: Transitions from CLOSED to IN_PROGRESS return HTTP 200
Expected Behavior: Transitions from CLOSED SHALL return HTTP 422
```
✅ Specific, observable, testable

---

**Bad (Not Falsifiable):**
```
Title validation doesn't work properly.
```
❌ No specific inputs, unclear what "properly" means

**Good (Falsifiable):**
```
Current Behavior: Titles with only whitespace (e.g., "   ") are accepted
Expected Behavior: Titles with only whitespace SHALL be rejected with HTTP 400
```
✅ Specific input, clear expected behavior

---

**Bad (Not Falsifiable):**
```
The system is slow when searching tickets.
```
❌ Subjective ("slow"), no measurable criteria

**Good (Falsifiable):**
```
Current Behavior: Keyword search with 1000 tickets takes 5+ seconds
Expected Behavior: Keyword search SHALL complete within 200ms for up to 10,000 tickets
```
✅ Measurable, specific performance requirement

---

### Template for Bug Conditions

Use this template for all bug conditions:

```markdown
## Bug Condition

**Current Behavior**: [Specific description of what happens now]
- Include: Inputs that trigger bug, actual output/behavior
- Be precise: Use exact values, error codes, response formats

**Expected Behavior**: [EARS pattern statement of correct behavior]
- Use EARS patterns (WHEN/THEN, IF/THEN, etc.)
- Specify: Expected output, error codes, validation messages

**Root Cause**: [Technical explanation OR "Under investigation"]
- If known: Component, method, line of code, logic error
- If unknown: State "Root cause under investigation"
- Include: Relevant code snippets or stack traces
```

---

## Reproduction Tests: Best Practices

### Test Requirements

Every reproduction test MUST:

1. **Fail on unfixed code**: Test must demonstrate the bug exists
2. **Pass on fixed code**: Test must verify the fix works
3. **Be automated**: No manual steps required
4. **Be repeatable**: Same result every run
5. **Be fast**: Complete in seconds, not minutes

### Property-Based vs. Example-Based

**Use Property-Based Test when:**
- Bug affects a range of inputs (e.g., all whitespace strings)
- Bug is about universal behavior (e.g., all transitions from CLOSED)
- You can generate varied test cases
- Bug might occur with unexpected input variations

**Use Example-Based Test when:**
- Bug occurs with specific exact input only
- Property test would be overly complex
- Bug involves external integration (API, database)
- Bug is about specific edge case value

### Example Reproduction Test Patterns

**Pattern 1: State-Based Bug**

```java
@Property(tries = 100)
void bugReproductionTest(@ForAll("triggeringStates") State state) {
    // Arrange: Put system in state that triggers bug
    system.transitionTo(state);
    
    // Act: Perform operation that should behave correctly
    Result result = system.performOperation();
    
    // Assert: Verify expected (correct) behavior
    assertThat(result).satisfiesExpectedBehavior();
}
```

**Pattern 2: Input Validation Bug**

```java
@Property(tries = 100)
void bugReproductionTest(@ForAll("invalidInputs") Input input) {
    // Act: Submit invalid input
    ResponseEntity<?> response = controller.submit(input);
    
    // Assert: Should be rejected (but currently isn't)
    assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
}
```

**Pattern 3: Edge Case Bug**

```java
@Property(tries = 100)
void bugReproductionTest(@ForAll("edgeCaseInputs") Input input) {
    // Act: Process edge case
    Result result = service.process(input);
    
    // Assert: Verify correct handling
    assertThat(result).isNotNull();
    assertThat(result.isValid()).isTrue();
}
```

### Generator Strategies

**Strategy 1: Focus on Bug Trigger**

Generate inputs that specifically trigger the bug:

```java
@Provide
Arbitrary<String> whitespaceStrings() {
    // Generate only whitespace strings (the bug trigger)
    return Arbitraries.of(" ", "  ", "\t", "\n", "   \t\n");
}
```

**Strategy 2: Include Bug and Non-Bug Cases**

Generate both triggering and non-triggering inputs:

```java
@Provide
Arbitrary<TicketStatus> allStatuses() {
    // Generate all statuses (bug affects only CLOSED)
    return Arbitraries.of(TicketStatus.values());
}
```

**Strategy 3: Boundary Values**

Generate values around boundaries where bug occurs:

```java
@Provide
Arbitrary<Integer> lengthsAroundLimit() {
    // Generate lengths near 255 (validation boundary)
    return Arbitraries.integers().between(253, 258);
}
```

---

## Common Bugfix Patterns

### Pattern 1: Validation Bug

**Scenario:** Input validation is missing or incorrect

**Bug Condition Template:**
```markdown
Current Behavior: Invalid input [X] is accepted and processed
Expected Behavior: Invalid input [X] SHALL be rejected with HTTP 400 and message "[error]"
Root Cause: Validator lacks @[Annotation] or custom validation logic
```

**Fix Approach:**
- Add validation annotation (@NotBlank, @Size, @Pattern)
- Or implement custom validator
- Update error messages

### Pattern 2: State Machine Bug

**Scenario:** Invalid state transitions are allowed

**Bug Condition Template:**
```markdown
Current Behavior: Transition from [State A] to [State B] is allowed
Expected Behavior: Transition from [State A] to [State B] SHALL be rejected with HTTP 422
Root Cause: State machine does not validate [specific condition]
```

**Fix Approach:**
- Add validation logic to state machine
- Update allowed transitions map
- Add terminal state checks

### Pattern 3: Edge Case Bug

**Scenario:** Specific input values cause unexpected behavior

**Bug Condition Template:**
```markdown
Current Behavior: When input is [edge case value], system produces [wrong output]
Expected Behavior: When input is [edge case value], system SHALL produce [correct output]
Root Cause: Code does not handle [edge case] (e.g., empty string, zero, null)
```

**Fix Approach:**
- Add edge case handling
- Update conditionals to include edge cases
- Add guard clauses

### Pattern 4: Data Integrity Bug

**Scenario:** Related data is not updated consistently

**Bug Condition Template:**
```markdown
Current Behavior: When [child entity] changes, [parent entity] is not updated
Expected Behavior: When [child entity] changes, [parent entity] SHALL update [field]
Root Cause: No cascade or lifecycle callback configured
```

**Fix Approach:**
- Add JPA cascade configuration
- Add lifecycle callbacks (@PreUpdate, @PrePersist)
- Or implement explicit update logic

---

## Verification Process

### Phase 2 Verification: Test Fails

After writing reproduction test:

1. **Run test on unfixed code**
2. **Verify test FAILS**
3. **Capture failure output**
4. **Confirm failure matches bug condition**

If test passes on unfixed code:
❌ Test does not reproduce bug — rewrite test or reconsider bug condition

### Phase 5 Verification: Test Passes

After implementing fix:

1. **Run reproduction test**
2. **Verify test PASSES**
3. **Run full test suite**
4. **Verify no regressions**

If test still fails after fix:
❌ Fix is incorrect — debug and iterate

If test passes but other tests fail:
❌ Fix introduced regression — revise fix approach

### Success Criteria

Bug fix is complete when:

- ✅ Reproduction test passes (100 iterations for property tests)
- ✅ All regression tests pass
- ✅ Manual verification confirms fix (if applicable)
- ✅ Error messages match specification
- ✅ No new bugs introduced
- ✅ Code reviewed and approved

---

## Summary

The Bugfix Workflow provides a systematic approach for fixing bugs:

1. **Phase 1**: Write falsifiable bug condition with current behavior, expected behavior, and root cause
2. **Phase 2**: Write reproduction test that FAILS on unfixed code
3. **Phase 3**: Design fix approach and verification strategy
4. **Phase 4**: Break fix into implementable tasks
5. **Phase 5**: Implement fix and verify reproduction test PASSES

**Key Principles:**

- Bug conditions must be falsifiable and testable
- Reproduction tests must fail before fix, pass after fix
- Prefer property-based tests to verify fix across input range
- Always run regression tests to prevent side effects
- Verify fix through both automated tests and manual testing

**Next Steps:**

1. When you encounter a bug, start with Phase 1: Bug Condition Definition
2. Create `.kiro/specs/{bug-name}/` directory
3. Write `requirements.md` with bug condition
4. Follow phases 2-5 to fix and verify
5. Keep reproduction test in codebase to prevent regression
