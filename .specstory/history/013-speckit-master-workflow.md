# Prompt 013: SpecKit Master Workflow Creation

**Date**: 2026-09-23  
**Session**: 4  
**Tool**: Kiro AI

---

## User Prompt

```
The speckit should be:
Constitution ↓ Specify ↓ Clarify ↓ Plan ↓ Checklist ↓ Tasks ↓ 
Analyze ↓ Implement ↓ Testing ↓ Converge ↓ Human Review ↓ Fix
```

**Context**: User defined the complete 12-phase spec-driven development workflow that should guide feature development from initial idea to production-ready implementation.

---

## Context

After creating steering files (auto-included guidelines) and individual skills (manual workflows), the project needed a master workflow that:

**Requirements**:
- Guide complete feature development lifecycle
- Integrate all other skills at appropriate phases
- Ensure constitutional compliance at every phase
- Provide quality gates before proceeding
- Support both simple and complex features
- Be the supreme orchestrating workflow

**The 12 Phases Defined**:
1. **Constitution** - Align with project principles
2. **Specify** - Define requirements (EARS format)
3. **Clarify** - Resolve ambiguities
4. **Plan** - Design technical solution
5. **Checklist** - Create implementation checklist
6. **Tasks** - Break down into detailed tasks
7. **Analyze** - Validate design before coding
8. **Implement** - Write production code
9. **Testing** - Comprehensive test coverage
10. **Converge** - Integrate and prepare PR
11. **Human Review** - Get feedback
12. **Fix** - Address feedback and iterate

**Constraints**:
- Must integrate with PROJECT_CONSTITUTION.md
- Must use all steering files
- Must orchestrate other skills
- Must produce complete artifacts
- Must ensure quality at each phase

---

## AI Response

Created SpecKit - the master workflow for spec-driven development:

### SpecKit Skill Created

**File**: `.kiro/skills/speckit/SKILL.md` (~4,000 lines)

**Purpose**: Complete spec-driven development from idea to production

**Invocation**: `#speckit`

---

## The 12 Phases in Detail

### Phase 1: Constitution (15-30 minutes)
**Goal**: Ensure constitutional alignment

**Activities**:
- Review PROJECT_CONSTITUTION.md relevant sections
- Identify applicable principles
- Check for conflicts with existing decisions
- Review related ADRs
- Constitutional compliance check
- Governance setup

**Outputs**:
- Constitutional compliance statement
- List of applicable principles
- Governance plan

---

### Phase 2: Specify (1-2 hours)
**Goal**: Define requirements with acceptance criteria

**Activities**:
- Gather requirements from stakeholders
- Write user stories (As a [role], I want [capability], so that [benefit])
- Define acceptance criteria using EARS patterns:
  - Ubiquitous: "The system shall..."
  - Event-driven: "When [event], the system shall..."
  - State-driven: "While [state], the system shall..."
  - Unwanted event: "If [error], the system shall..."
  - Optional: "Where [condition], the system shall..."
- Identify correctness properties for property-based testing

**Outputs**:
- User stories (3-10 stories)
- Acceptance criteria (EARS format)
- Correctness properties
- Requirements document: `spec/requirements-{feature}.md`

---

### Phase 3: Clarify (30-60 minutes)
**Goal**: Resolve ambiguities and document assumptions

**Activities**:
- Identify ambiguities (unclear requirements, missing edge cases)
- Ask clarifying questions
- Document decisions for edge cases
- Define error scenarios
- Specify boundary conditions
- Update requirements with clarifications

**Outputs**:
- Clarifications document
- Updated requirements
- Documented assumptions
- Resolved ambiguities

---

### Phase 4: Plan (2-4 hours)
**Goal**: Design the technical solution

**Activities**:
- Architectural design (layers, components, patterns)
- Data model design (entities, relationships, indexes)
- API contract design (endpoints, DTOs, status codes)
- State machine design (if applicable)
- Test strategy (unit, integration, property-based)

**Outputs**:
- `spec/architecture-{feature}.md`
- `spec/data-model-{feature}.md`
- `spec/api-contract-{feature}.md`
- `spec/state-machine-{feature}.md` (if applicable)
- `spec/test-strategy-{feature}.md`

**Integration**: Use `#adr` skill for architectural decisions

---

### Phase 5: Checklist (30 minutes)
**Goal**: Create implementation checklist

**Activities**:
- Break down implementation into steps
- List all files to create/modify
- Identify dependencies
- Order tasks logically
- Create implementation checklist

**Outputs**:
- Implementation checklist (25+ items)
- Risk assessment
- Dependency list

---

### Phase 6: Tasks (30-60 minutes)
**Goal**: Create detailed task breakdown

**Activities**:
- Create task file: `.kiro/specs/{feature-name}/tasks.md`
- Define tasks with sub-tasks
- Estimate effort for each task
- Identify critical path
- Plan iteration milestones

**Outputs**:
- Detailed tasks file with sub-tasks
- Effort estimates
- Implementation order

---

### Phase 7: Analyze (30 minutes)
**Goal**: Review and validate before implementation

**Activities**:
- Design review against constitution
- Completeness check (all requirements have design)
- Dependency analysis
- Risk assessment
- **Go/No-Go decision**

**Outputs**:
- Design review report
- Completeness checklist (verified)
- Risk mitigation plan
- Decision to proceed or return to earlier phase

---

### Phase 8: Implement (Varies)
**Goal**: Write production code following specifications

**Activities**:
- Follow task order sequentially
- Mark tasks as complete in tasks.md
- Commit frequently (after each major task)
- Follow `.kiro/steering/java-springboot.md` standards
- Use constructor injection
- DTOs for all API contracts
- JavaDoc for public methods

**Outputs**:
- Implemented code (all layers)
- Migration scripts
- DTOs
- Frequent commits

**Standards**:
- Follow layered architecture
- Business logic in service layer only
- Proper exception handling
- @Transactional where needed
- EnumType.STRING for enums

---

### Phase 9: Testing (Equal to implementation time)
**Goal**: Comprehensive test coverage

**Activities**:
- Write unit tests (JUnit 5, Mockito, AssertJ)
- Write integration tests (Spring Boot Test, MockMvc)
- Write property-based tests (jqwik, 100+ iterations)
- Achieve >80% code coverage
- Test all edge cases and error scenarios

**Outputs**:
- Comprehensive test suite
- Test coverage report (>80%)
- Property tests (100+ iterations each)
- All tests passing

**Standards**:
- Test names: `methodName_condition_expectedResult`
- Given-When-Then structure
- AssertJ assertions
- No flaky tests

---

### Phase 10: Converge (1-2 hours)
**Goal**: Integrate, validate, and prepare for review

**Activities**:
- Run full test suite
- Code quality check (linter, no TODOs)
- Documentation review (JavaDoc, README, API docs)
- Constitutional compliance verification
- Create pull request with complete description

**Outputs**:
- Pull request
- Complete, tested, documented code
- Updated specifications
- Compliance verification

**Integration**: Use `#code-review` skill before submitting

---

### Phase 11: Human Review (Varies)
**Goal**: Get human validation and feedback

**Activities**:
- Self-review first using `#code-review`
- Submit PR with complete description
- Provide context and link to specifications
- Address reviewer questions
- Make requested changes

**PR Description Template**:
- Summary of changes
- Links to specifications
- Testing details
- Constitutional compliance checklist
- ADRs created
- Screenshots (if applicable)

**Outcomes**:
- ✅ Approved → Merge
- 🔄 Changes Requested → Phase 12

---

### Phase 12: Fix (Varies)
**Goal**: Address review feedback and iterate

**Activities**:
- Categorize feedback (critical, important, nice-to-have)
- Address each item
- Implement fixes with focused commits
- Re-validate (run tests, self-review)
- Respond to reviewers
- Re-submit for review

**Iteration**:
- If more feedback → Repeat Phase 12
- If approved → Merge and complete!

**Integration**: Use relevant skills as needed:
- `#bug-investigation` for issues
- `#refactor` for code quality
- `#performance-review` for performance issues

---

## SpecKit Features

### Quality Gates

Each phase has exit criteria that must be met before proceeding:
- Phase 1: Constitutional alignment verified
- Phase 2: Requirements documented
- Phase 3: Ambiguities resolved
- Phase 4: Design complete and reviewed
- Phase 7: **Go/No-Go decision** (critical gate)
- Phase 9: All tests pass, coverage >80%
- Phase 10: PR ready, compliance verified

### Integration Points

SpecKit orchestrates all other skills:
```
Phase 4  → #adr (architectural decisions)
Phase 8  → #refactor (code quality)
Phase 9  → Follow .kiro/steering/testing.md
Phase 10 → #code-review (before PR)
Phase 11 → #bug-investigation (if issues)
Phase 12 → #performance-review (if needed)
```

### Constitutional Compliance

Verified at multiple phases:
- Phase 1: Initial alignment
- Phase 7: Design validation
- Phase 10: Implementation verification

### Flexibility

- **Complex features**: Use all 12 phases
- **Simple features**: Use abbreviated workflow (2, 6, 8, 9, 10, 11)
- **Bug fixes**: Use `#bug-investigation` instead

---

## Deliverables

**SpecKit Skill** (1 file, ~4,000 lines):
- `.kiro/skills/speckit/SKILL.md` (4,000 lines)

**Supporting Documentation** (2 files):
- `SPECKIT-README.md` (~500 lines) - User guide
- `SKILLS-SUMMARY.md` (updated) - Added SpecKit

**Total New Content**: ~4,500 lines

**Updated Files**:
- `SKILLS-SUMMARY.md` - Added SpecKit as master workflow
- `PROJECT_STRUCTURE.md` - Updated skills directory

---

## Impact Assessment

**Immediate Impact**:
- ✅ Complete end-to-end workflow documented
- ✅ Quality gates at every phase
- ✅ Constitutional compliance built-in
- ✅ Integration with all other skills
- ✅ Systematic approach to feature development

**Long-Term Impact**:
- Consistent high-quality implementations
- Reduced rework (validation in Phase 7)
- Better planning (Phases 1-7 before coding)
- Comprehensive testing (Phase 9 mandatory)
- Successful reviews (Phase 10 preparation)
- Faster iterations (structured Phase 12)

**For Developers**:
- Clear roadmap from idea to production
- Guidance at each phase
- Integration with other workflows
- Quality assurance built-in

**For the Project**:
- Formalized development methodology
- Repeatable, consistent process
- High-quality deliverables
- Constitutional compliance assured
- Knowledge captured in artifacts

---

## Example Journey

**Feature**: User Authentication

**Timeline**: 2 weeks

**Phases**:
- Phase 1: 20 min → Constitutional review ✅
- Phase 2: 1.5 hrs → 8 requirements with EARS ✅
- Phase 3: 45 min → Edge cases documented ✅
- Phase 4: 3 hrs → Complete design specs ✅
- Phase 5: 30 min → 25-item checklist ✅
- Phase 6: 1 hr → 5 tasks, 23 sub-tasks ✅
- Phase 7: 30 min → Design validated ✅
- Phase 8: 3 days → 18 files, 18 commits ✅
- Phase 9: 2 days → 32 tests, 87% coverage ✅
- Phase 10: 2 hrs → PR created ✅
- Phase 11: 1 day → Feedback received ✅
- Phase 12: 4 hrs → 5 issues fixed ✅

**Result**: Feature complete and merged! 🎉

---

## Next Steps

1. ✅ SpecKit master workflow created
2. ✅ SPECKIT-README.md guide created
3. ✅ All documentation updated
4. ⏸️ Test SpecKit with real feature
5. ⏸️ Gather user feedback
6. ⏸️ Refine phases based on usage
7. ⏸️ Create abbreviated workflow variants
8. ⏸️ Track metrics (time per phase, quality outcomes)

---

## Lessons Learned

**What Went Well**:
- 12-phase workflow provides complete coverage
- Quality gates prevent proceeding with issues
- Integration with other skills is seamless
- Constitutional compliance is systematic
- Flexibility for simple vs complex features

**What Could Be Improved**:
- Need real-world validation
- Should add time estimate guidelines by complexity
- Could create automated phase checkers
- Need metrics dashboard for tracking

**Key Takeaways**:
- Master workflow orchestrates all other skills
- Quality gates are critical for success
- Constitutional compliance must be systematic
- Validation before implementation (Phase 7) prevents rework
- Human review (Phase 11) is mandatory for quality

---

## References

- **SpecKit Skill**: `.kiro/skills/speckit/SKILL.md`
- **SpecKit Guide**: `SPECKIT-README.md`
- **PROJECT_CONSTITUTION.md** - Supreme governance
- **All Steering Files**: `.kiro/steering/*.md`
- **All Skills**: `.kiro/skills/*/SKILL.md`
- Prompt 010: Project structure migration
- Prompt 011: Steering files creation
- Prompt 012: Skills development

---

**Prompt Number**: 013  
**Status**: Complete  
**Related Prompts**: 
- Prompt 010: Project structure migration
- Prompt 011: Steering files creation
- Prompt 012: Skills development
- This completes the foundation of spec-driven development system

---

## System Complete

With SpecKit created, the project now has:

✅ **Project Structure** (Prompt 010) - Organized specifications  
✅ **Steering Files** (Prompt 011) - Auto-included guidelines  
✅ **Individual Skills** (Prompt 012) - Specialized workflows  
✅ **SpecKit Master Workflow** (Prompt 013) - Complete lifecycle

**Total System**:
- 7 skills (~10,000 lines)
- 5 steering files (~6,000 lines)
- Complete documentation
- Production-ready methodology

🎉 **Spec-Driven Development System: COMPLETE!**
