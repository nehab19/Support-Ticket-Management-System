---
inclusion: manual
---

# Bug Investigation Skill

**Skill Name**: Bug Investigation  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Purpose

This skill provides a systematic approach to investigating and resolving bugs. It ensures thorough root cause analysis, proper documentation, and prevention of similar issues.

**Scope**: Bug investigation, root cause analysis, fix validation  
**Authority**: Follows PROJECT_CONSTITUTION.md testing and documentation requirements

---

## When to Use This Skill

Invoke this skill (`#bug-investigation`) when:

- A bug has been reported or discovered
- Tests are failing unexpectedly
- System behavior doesn't match requirements
- Production issues need investigation
- Need to prevent bug recurrence

---

## Bug Investigation Workflow

### Step 1: Reproduce the Bug

**Goal**: Confirm the bug exists and understand when it occurs

**Actions**:

1. **Gather information**:
   - What is the expected behavior?
   - What is the actual behavior?
   - What steps trigger the bug?
   - What error messages appear?
   - When did it start happening?

2. **Create a minimal reproduction**:
   - Find the simplest steps to trigger the bug
   - Remove unrelated complexity
   - Document exact steps

3. **Verify reproduction**:
   - Can you trigger it consistently?
   - Does it happen in different environments?
   - Does it happen with different data?

**Output**: Clear reproduction steps

**Example**:
```markdown
## Bug Reproduction

**Expected**: Ticket status transitions from OPEN to CANCELLED
**Actual**: Error "Cannot transition from OPEN to CANCELLED"

**Steps**:
1. Create ticket (status = OPEN)
2. Send PATCH /api/tickets/{id}/status with status=CANCELLED
3. Observe 422 error

**Consistency**: 100% reproducible
**Environment**: Local (H2), Production (PostgreSQL)
```

---

### Step 2: Isolate the Issue

**Goal**: Narrow down where the bug occurs

**Investigation areas**:

**Frontend vs Backend**:
- [ ] Check browser console for errors
- [ ] Verify API request is correct
- [ ] Test API directly (curl, Postman)
- [ ] Check backend logs

**Layer identification**:
- [ ] Controller: Check request handling
- [ ] Service: Check business logic
- [ ] Repository: Check database queries
- [ ] Database: Check data state

**Code vs Configuration**:
- [ ] Is it a code bug or config issue?
- [ ] Check environment variables
- [ ] Check application properties
- [ ] Check database connection

**Techniques**:
- Add logging statements
- Use debugger breakpoints
- Write a failing test
- Check database state directly

---

### Step 3: Analyze Root Cause

**Goal**: Understand WHY the bug occurs

**Common root cause categories**:

### 1. Logic Errors
```java
// Example: Off-by-one error
if (count > 10) { }  // Should be >= 10
```

### 2. Missing Validation
```java
// Example: No null check
public void process(String input) {
    input.trim();  // NullPointerException if input is null
}
```

### 3. Incorrect State Management
```java
// Example: Wrong transition check
if (currentStatus == OPEN) {
    // Missing: Check target status validity
    ticket.setStatus(newStatus);
}
```

### 4. Race Conditions
```java
// Example: Concurrent modification
if (!exists(id)) {
    create(id);  // Race: Another thread may create between check and create
}
```

### 5. Configuration Issues
```properties
# Example: Wrong property name
spring.datasource.url=...  # Should be spring.datasource.url
```

### 6. Data Issues
```sql
-- Example: Missing data or constraint
-- Ticket has no status (NULL) due to missing validation
```

**Analysis questions**:
- What assumption was violated?
- What edge case wasn't handled?
- What validation was missing?
- What state was unexpected?
- What condition wasn't checked?

---

### Step 4: Verify Against Specifications

**Check compliance**:

- [ ] Does bug violate spec/requirements.md?
- [ ] Does bug violate spec/state-machine.md?
- [ ] Does bug violate PROJECT_CONSTITUTION.md?
- [ ] Was the requirement ambiguous?
- [ ] Was the implementation incomplete?

**Example**:
```markdown
**Requirement**: REQ-5: Ticket status transitions must follow state machine rules

**Violation**: OPEN → CANCELLED transition is allowed per spec/state-machine.md 
but TicketStateMachine.isValidTransition() returns false

**Root Cause**: Implementation doesn't match specification
```

---

### Step 5: Develop Fix Strategy

**Fix approaches**:

### Fix Type 1: Code Change
- Correct the logic error
- Add missing validation
- Handle edge case
- Fix algorithm

### Fix Type 2: Test Addition
- Add test that would have caught this
- Add regression test
- Add property test for invariant

### Fix Type 3: Specification Update
- Clarify ambiguous requirement
- Document edge case handling
- Update state machine diagram

### Fix Type 4: Architecture Change
- Refactor for better structure
- Add abstraction layer
- Improve error handling

**Fix planning**:
1. **Identify files to change**
2. **Estimate impact** (low/medium/high)
3. **List affected tests**
4. **Consider side effects**
5. **Plan validation approach**

---

### Step 6: Implement Fix

**Implementation checklist**:

- [ ] Write failing test first (TDD)
- [ ] Implement minimal fix
- [ ] Run test (should pass)
- [ ] Run all tests (no regressions)
- [ ] Add defensive code if needed
- [ ] Update error messages
- [ ] Add logging if helpful
- [ ] Update documentation

**Code review before committing**:
- [ ] Fix addresses root cause (not symptom)
- [ ] No new bugs introduced
- [ ] Error handling proper
- [ ] Edge cases covered
- [ ] Code is clean and maintainable

---

### Step 7: Validate Fix

**Validation steps**:

1. **Run reproduction steps**:
   - Bug should no longer occur
   - Original scenario works correctly

2. **Run regression tests**:
   - All existing tests pass
   - No new failures introduced

3. **Test edge cases**:
   - Boundary conditions
   - Null/empty inputs
   - Large datasets
   - Concurrent access (if applicable)

4. **Test in target environment**:
   - Local environment
   - Staging environment (if exists)
   - Production-like configuration

5. **Manual testing**:
   - Test through UI
   - Test through API directly
   - Test with different data

---

### Step 8: Document Fix

**Update docs/ai-review.md** (if AI was involved):
```markdown
## 2026-09-23: State Machine OPEN→CANCELLED Bug

**Tool**: Kiro AI  
**Task**: Implement ticket state machine validation  
**Issue**: AI implementation prevented OPEN→CANCELLED transition despite spec allowing it  
**Impact**: Major - Core business logic broken  
**Resolution**: Updated TicketStateMachine.ALLOWED_TRANSITIONS to include OPEN→CANCELLED  
**Lesson**: Always verify AI implementation against state machine specification. State machines need exhaustive validation.  
**Pattern**: State machine implementation errors (Category: Logic Errors)
```

**Commit message**:
```
fix: allow OPEN to CANCELLED ticket status transition

The state machine was incorrectly rejecting OPEN→CANCELLED transitions
despite this being a valid transition per spec/state-machine.md.

Root cause: ALLOWED_TRANSITIONS map missing this entry

Changes:
- Added OPEN→CANCELLED to ALLOWED_TRANSITIONS
- Added regression test
- Verified all 5 allowed transitions work
- Verified forbidden transitions still rejected

Fixes: #123
```

---

### Step 9: Prevent Recurrence

**Prevention strategies**:

**Add Tests**:
- [ ] Unit test for specific scenario
- [ ] Integration test for end-to-end flow
- [ ] Property test for invariant
- [ ] Regression test suite

**Improve Code**:
- [ ] Add validation where missing
- [ ] Add defensive programming
- [ ] Improve error messages
- [ ] Add logging for debugging

**Update Documentation**:
- [ ] Update spec if ambiguous
- [ ] Add code comments
- [ ] Document edge cases
- [ ] Update ADR if needed

**Update Steering Files**:
- [ ] Add to common AI mistakes
- [ ] Update guidelines
- [ ] Add to anti-patterns
- [ ] Create checklist item

**Review Process**:
- [ ] Add to code review checklist
- [ ] Update PR template
- [ ] Schedule team knowledge sharing

---

## Bug Investigation Template

```markdown
# Bug: [Short Description]

**Reported Date**: YYYY-MM-DD  
**Reporter**: [Name]  
**Severity**: Critical / Major / Minor  
**Status**: Investigating / Fixed / Closed

---

## Symptoms

**Expected Behavior**:
[What should happen]

**Actual Behavior**:
[What actually happens]

**Error Messages**:
```
[Copy exact error messages]
```

---

## Reproduction Steps

1. [Step 1]
2. [Step 2]
3. [Step 3]

**Reproducibility**: Always / Sometimes / Rarely  
**Environment**: Local / Staging / Production  
**Data Required**: [Specific data needed]

---

## Investigation

**Affected Components**:
- [Component 1]
- [Component 2]

**Investigation Log**:
- [Timestamp]: [What was checked and result]
- [Timestamp]: [Next investigation step]

---

## Root Cause

**Category**: Logic Error / Validation / Configuration / Data / Race Condition

**Description**:
[Detailed explanation of why the bug occurs]

**Affected Files**:
- [File 1]: [What's wrong]
- [File 2]: [What's wrong]

---

## Fix

**Approach**: [Code change / Test addition / Spec update]

**Changes**:
- [File 1]: [What changed]
- [File 2]: [What changed]

**Tests Added**:
- [Test 1]: [Purpose]
- [Test 2]: [Purpose]

---

## Validation

- [x] Reproduction steps no longer trigger bug
- [x] All tests pass
- [x] Manual testing complete
- [x] Edge cases tested
- [x] No regressions introduced

---

## Prevention

**Measures Taken**:
- [Prevention 1]
- [Prevention 2]

**Documentation Updated**:
- [Doc 1]
- [Doc 2]

---

## Lessons Learned

[What did we learn? How can we prevent similar bugs?]
```

---

## Common Bug Patterns

### Pattern 1: State Machine Violations

**Symptom**: Invalid state transitions allowed or valid ones blocked

**Investigation**:
- Check TicketStateMachine implementation
- Compare with spec/state-machine.md
- Verify all 5 allowed transitions
- Verify forbidden transitions rejected

**Fix**: Update transition map and add tests

---

### Pattern 2: Null Pointer Exceptions

**Symptom**: NullPointerException in logs

**Investigation**:
- Identify which variable is null
- Trace back to where it's set
- Check for missing validation

**Fix**: Add null checks or use Optional

---

### Pattern 3: Validation Bypass

**Symptom**: Invalid data gets persisted

**Investigation**:
- Check if @Valid is present
- Check if validation rules defined
- Check if validation is in correct layer

**Fix**: Add validation annotations or service-layer checks

---

### Pattern 4: Transaction Issues

**Symptom**: Data inconsistency, race conditions

**Investigation**:
- Check if @Transactional present
- Check transaction boundaries
- Check isolation level

**Fix**: Add @Transactional with appropriate settings

---

### Pattern 5: DTO/Entity Confusion

**Symptom**: LazyInitializationException or data exposure

**Investigation**:
- Check if entities returned from controllers
- Check if DTOs used for all API responses

**Fix**: Convert entities to DTOs in service layer

---

## Debugging Tips

### Use Debugger Effectively
- Set breakpoints at suspected locations
- Step through code line by line
- Inspect variable values
- Watch expressions

### Strategic Logging
```java
log.debug("Processing ticket transition: {} -> {}", currentStatus, newStatus);
log.debug("Allowed transitions: {}", ALLOWED_TRANSITIONS);
log.warn("Invalid transition attempted: {} -> {}", currentStatus, newStatus);
```

### Write Targeted Tests
```java
@Test
void reproducesBug() {
    // Minimal test that reproduces the exact bug
    // Should fail before fix, pass after fix
}
```

### Check Database State
```sql
-- Verify data matches expectations
SELECT * FROM ticket WHERE id = 123;

-- Check for orphaned or invalid data
SELECT * FROM ticket WHERE status IS NULL;
```

---

## Severity Classification

| Severity | Definition | Example | Response Time |
|----------|------------|---------|---------------|
| **Critical** | System down, data loss | Production crash | Immediate |
| **Major** | Core feature broken | Can't create tickets | <4 hours |
| **Minor** | Edge case, workaround exists | Typo in error message | <1 week |
| **Trivial** | Cosmetic issue | UI alignment off | Backlog |

---

## Quick Reference Checklist

### Investigation Phase
- [ ] Reproduce bug consistently
- [ ] Isolate to specific component/layer
- [ ] Identify root cause
- [ ] Verify against specifications

### Fix Phase
- [ ] Write failing test
- [ ] Implement fix
- [ ] All tests pass
- [ ] Manual validation complete

### Documentation Phase
- [ ] Document in ai-review.md (if applicable)
- [ ] Commit with descriptive message
- [ ] Update specs if needed
- [ ] Add prevention measures

---

**Skill Version**: 1.0  
**Last Updated**: 2026-09-23  
**Related Documents**: docs/ai-review.md, PROJECT_CONSTITUTION.md, .kiro/steering/testing.md
