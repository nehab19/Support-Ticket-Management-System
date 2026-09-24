---
inclusion: manual
---

# Prompt History Management Skill

**Skill Name**: Prompt History Management  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Purpose

This skill provides a structured workflow for managing AI prompt history in the Support Ticket Management System. Use this skill when you need to document significant AI interactions for future reference, learning, and project continuity.

---

## When to Use This Skill

Invoke this skill (`#prompt-history`) when:

- You've completed a significant code generation session (>100 lines)
- You've made architectural or design decisions with AI assistance
- You've resolved a complex bug or issue
- You've created specification or documentation artifacts
- You've established new patterns or conventions
- The AI interaction represents important project knowledge

---

## Workflow

### Step 1: Assess Significance

**Determine if the prompt qualifies for documentation:**

**Qualifies**:
- Generated >100 lines of code
- Made architectural/design decisions
- Resolved complex bugs
- Created specs or documentation
- Established patterns/conventions
- Significant time investment (>30 minutes)

**Does Not Qualify**:
- Minor bug fixes (<20 lines)
- Formatting changes
- Simple typo corrections
- Routine code updates

**Action**: If qualifies, proceed to Step 2. Otherwise, stop here.

---

### Step 2: Gather Context

**Collect the following information:**

1. **Date**: Current date (YYYY-MM-DD)
2. **Session Number**: Count of prompt history entries
3. **AI Tool**: Which tool was used (Kiro AI, Cursor, Copilot, ChatGPT, Claude, etc.)
4. **User Prompt**: The exact text of your prompt or request
5. **Context**: Background, rationale, constraints that led to the prompt
6. **AI Response**: Summary of what the AI generated or suggested
7. **Deliverables**: List of files created/modified with approximate line counts
8. **Impact Assessment**: Immediate and long-term impacts of this work
9. **Next Steps**: Follow-up actions or related work
10. **Lessons Learned**: Key takeaways or insights
11. **References**: Links to related documents or resources

---

### Step 3: Choose File Name

**Generate a descriptive file name:**

**Format**: `NNN-short-description.md`

**NNN**: Zero-padded 3-digit number (next in sequence)
- Check `.specstory/history/` for highest number
- Increment by 1

**short-description**: Kebab-case summary (3-5 words)
- Examples: `ticket-state-machine`, `property-based-testing`, `api-error-handling`

**Examples**:
- `010-ticket-crud-implementation.md`
- `011-state-machine-validation.md`
- `012-property-test-suite.md`

---

### Step 4: Create Detailed History File

**Location**: `.specstory/history/NNN-description.md`

**Template**:
```markdown
# Prompt NNN: [Descriptive Title]

**Date**: YYYY-MM-DD  
**Session**: N  
**Tool**: [AI Tool Name]

---

## User Prompt

```
[Full text of user's request/prompt]
[Include any context or constraints provided]
[Preserve formatting and line breaks]
```

---

## Context

[Explain the background and rationale]
- Why was this needed?
- What problem were we solving?
- What constraints or requirements existed?
- What prior work led to this?

---

## AI Response

[Summarize what the AI generated]
- What code/docs were created?
- What decisions were made?
- What approach was taken?
- Were there multiple iterations?

[Include key code snippets if relevant]
```java
// Example of generated code
```

---

## Deliverables

**Files Created**:
- `path/to/file1.java` (100 lines) — Brief description
- `path/to/file2.java` (50 lines) — Brief description

**Files Modified**:
- `path/to/file3.java` (+25 lines) — What changed
- `path/to/file4.md` (+10 lines) — What changed

**Total**: X files, Y lines of code

---

## Impact Assessment

**Immediate Impact**:
- What can we do now that we couldn't before?
- What problems are solved?
- What functionality is available?

**Long-Term Impact**:
- How does this affect architecture?
- What future work does this enable?
- What technical debt (if any) was introduced?

---

## Next Steps

1. [Follow-up action 1]
2. [Follow-up action 2]
3. [Follow-up action 3]

---

## Lessons Learned

**What Went Well**:
- [Positive outcome 1]
- [Positive outcome 2]

**What Could Be Improved**:
- [Improvement opportunity 1]
- [Improvement opportunity 2]

**Key Takeaways**:
- [Insight 1]
- [Insight 2]

---

## References

- [Link to related spec document]
- [Link to related ADR]
- [Link to PROJECT_CONSTITUTION.md section]
- [External resource URL]

---

**Prompt Number**: NNN  
**Status**: Complete  
**Related Prompts**: [Links to related prompts if applicable]
```

---

### Step 5: Update Prompt History Summary

**Location**: `docs/prompt-history.md`

**Add entry in chronological order:**

```markdown
## YYYY-MM-DD: [Prompt Title]

**Prompt Number**: NNN  
**File**: `.specstory/history/NNN-description.md`  
**Tool**: [AI Tool]  
**Purpose**: [One-sentence description]  
**Outcome**: [Brief summary of what was created]  
**Files Changed**: [Count] files, [Count] lines  
**Notes**: [Any important context or caveats]
```

**Example**:
```markdown
## 2026-09-23: Implement Ticket State Machine

**Prompt Number**: 010  
**File**: `.specstory/history/010-ticket-state-machine.md`  
**Tool**: Kiro AI  
**Purpose**: Implement strict state machine validation for ticket status transitions  
**Outcome**: Created TicketStateMachine component with allowlist-based validation  
**Files Changed**: 3 files, 150 lines  
**Notes**: Required refinement to handle terminal state transitions correctly
```

---

### Step 6: Review and Commit

**Review checklist:**
- [ ] File name follows NNN-description.md format
- [ ] All template sections are filled out
- [ ] Code snippets (if any) are syntactically correct
- [ ] File paths are accurate
- [ ] Summary entry added to docs/prompt-history.md
- [ ] No sensitive information (secrets, passwords) included
- [ ] Lessons learned are meaningful and specific

**Commit Message**:
```
docs: add prompt history for [feature/task]

Documented AI interaction for [brief description].

Related files:
- .specstory/history/NNN-description.md
- docs/prompt-history.md
```

---

## Examples

### Example 1: Code Generation Session

**User Prompt**:
```
"Create a TicketService with CRUD operations following the layered 
architecture. Use constructor injection, DTOs for all responses, 
and include JavaDoc."
```

**Deliverables**:
- `TicketService.java` (200 lines)
- `TicketServiceTest.java` (150 lines)
- `CreateTicketRequest.java` (20 lines)
- `TicketDetailResponse.java` (30 lines)

**File**: `.specstory/history/010-ticket-service-crud.md`

---

### Example 2: Architecture Decision

**User Prompt**:
```
"Should we use a separate endpoint for status transitions or combine 
it with the generic update endpoint? Provide pros/cons for each approach."
```

**Deliverables**:
- ADR-001 in `docs/architecture-decisions.md`
- Updated `spec/api-contract.md`

**File**: `.specstory/history/011-status-transition-decision.md`

---

### Example 3: Bug Resolution

**User Prompt**:
```
"The state machine allows transitioning from CLOSED to OPEN, but this 
should be forbidden. Fix the TicketStateMachine and add tests to prevent 
regression."
```

**Deliverables**:
- `TicketStateMachine.java` (fixed)
- `TicketStateMachineTest.java` (+50 lines)

**File**: `.specstory/history/012-state-machine-terminal-fix.md`

---

## Tips

### Writing Good Context

**❌ Vague**:
"We needed to add validation."

**✅ Specific**:
"The state machine was allowing invalid transitions from terminal states 
(CLOSED, CANCELLED) because the transition map didn't explicitly prevent them. 
This violated Requirement 5.3 in spec/requirements.md."

### Capturing Lessons Learned

**❌ Generic**:
"AI was helpful."

**✅ Actionable**:
"AI initially forgot to validate state transitions before persisting. 
This is a pattern we've seen before. Going forward, always verify that 
AI includes business rule validation in service methods."

### Linking Related Work

**Connect prompts that build on each other**:
```markdown
**Related Prompts**:
- Prompt 008: Initial state machine design
- Prompt 010: State machine implementation
- Prompt 012: Terminal state bug fix (this prompt)
```

---

## Maintenance

### Periodic Review

**Monthly**:
- Review recent prompt history entries
- Identify patterns in AI mistakes
- Update steering files with learnings
- Archive old entries (optional)

**Quarterly**:
- Generate statistics (total prompts, by type, by tool)
- Summarize key learnings
- Present findings to team

---

## Statistics Template

**Add to docs/prompt-history.md**:

```markdown
## Statistics

**Total Prompts**: NN  
**Date Range**: YYYY-MM-DD to YYYY-MM-DD

### By Type
- Code Generation: NN (XX%)
- Architecture/Design: NN (XX%)
- Bug Resolution: NN (XX%)
- Documentation: NN (XX%)
- Testing: NN (XX%)

### By Tool
- Kiro AI: NN (XX%)
- Cursor: NN (XX%)
- Copilot: NN (XX%)
- ChatGPT: NN (XX%)

### Top Lessons
1. [Most important lesson]
2. [Second most important lesson]
3. [Third most important lesson]
```

---

## Quick Reference

### File Locations

| Item | Location | Purpose |
|------|----------|---------|
| Detailed history | `.specstory/history/NNN-name.md` | Full prompt documentation |
| Summary | `docs/prompt-history.md` | Quick reference list |
| Current doc | `.kiro/skills/prompt-history/SKILL.md` | This skill |

### Naming Convention

```
NNN-short-description.md

Where:
  NNN = Zero-padded number (001, 002, ..., 010, 011, ...)
  short-description = kebab-case, 3-5 words
  
Examples:
  010-ticket-crud-operations.md
  011-state-machine-validation.md
  012-property-test-suite.md
```

---

## Checklist

Before completing prompt history documentation:

- [ ] Assessed significance (qualifies for documentation)
- [ ] Gathered all context and information
- [ ] Created detailed history file in `.specstory/history/`
- [ ] Updated summary in `docs/prompt-history.md`
- [ ] Reviewed for completeness and accuracy
- [ ] Verified no sensitive information included
- [ ] Committed with descriptive message
- [ ] Linked related prompts (if applicable)

---

**Skill Version**: 1.0  
**Last Updated**: 2026-09-23  
**Maintained By**: Project Team
