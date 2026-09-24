# Prompt 012: Comprehensive Skills Development

**Date**: 2026-09-23  
**Session**: 4  
**Tool**: Kiro AI

---

## User Prompt

```
Please create relevant skills
```

**Context**: User requested creation of comprehensive skills to support various development workflows beyond the initial prompt-history skill.

---

## Context

After establishing steering files (auto-included guidelines), the project needed manual-invocation skills for specific workflows:

**Requirements**:
- Skills for common development tasks
- Structured, step-by-step workflows
- Integration with steering files and constitution
- Practical examples from the project
- Consistent format across all skills
- `inclusion: manual` (invoked with `#skill-name`)

**Workflows Needed**:
- Code review (systematic validation)
- Architecture decisions (ADR creation)
- Bug investigation (debugging process)
- Code refactoring (safe improvement)
- Performance optimization (systematic tuning)

---

## AI Response

Created 5 comprehensive skills to support development workflows:

### 1. Code Review Skill (`#code-review`)

**File**: `.kiro/skills/code-review/SKILL.md` (~1,100 lines)

**Purpose**: Systematic code review following constitutional requirements

**8-Step Review Workflow**:
1. Architecture Compliance
   - Layered architecture adherence
   - Controllers: HTTP only, no business logic
   - Services: Business logic, no HTTP
   - Repositories: Data access only
   - DTOs for all API contracts

2. Security Review
   - No secrets or credentials
   - Input validation present
   - SQL injection prevention
   - No stack traces exposed
   - Environment variables for config

3. Code Quality
   - Constructor injection (not field)
   - Single responsibility
   - Methods <30 lines
   - No magic numbers
   - EnumType.STRING (not ORDINAL)
   - Proper null handling

4. Error Handling
   - GlobalExceptionHandler catches all
   - Consistent error response format
   - Appropriate HTTP status codes
   - No stack traces in responses

5. Testing Review
   - Unit tests for services
   - Integration tests for endpoints
   - Property tests for business logic
   - AssertJ assertions
   - Given-When-Then structure

6. Documentation Review
   - JavaDoc on public APIs
   - Inline comments explain WHY
   - API documentation updated
   - README updated if needed

7. State Machine Validation (if applicable)
   - Only 5 allowed transitions
   - Terminal states have no outbound transitions
   - Invalid transitions throw HTTP 422
   - Property tests verify correctness

8. Data Persistence Review
   - Proper JPA annotations
   - Flyway migrations for schema changes
   - No schema changes in code

**Features**:
- Common AI mistakes catalog (5 categories with examples)
- Quick reference checklist
- Review report template

---

### 2. ADR Skill (`#adr`)

**File**: `.kiro/skills/adr/SKILL.md` (~850 lines)

**Purpose**: Document architectural and design decisions

**7-Step ADR Workflow**:
1. Identify the Decision
   - What problem are we solving?
   - What choice are we making?

2. Gather Context
   - Technical context
   - Business context
   - Historical context

3. Identify Options
   - List all viable alternatives
   - Pros/cons for each
   - Effort estimates

4. Make the Decision
   - Choose based on criteria
   - Alignment with principles
   - Team consensus

5. Document Consequences
   - Positive impacts
   - Negative impacts
   - Neutral changes

6. Write the ADR
   - Standard format (Context, Decision, Consequences)
   - Alternatives considered
   - Related decisions

7. Update and Communicate
   - Add to docs/architecture-decisions.md
   - Communicate to team
   - Link from related specs

**Features**:
- Standard ADR template
- Examples from the project (10 existing ADRs)
- Status value definitions
- Tips for writing good ADRs

---

### 3. Bug Investigation Skill (`#bug-investigation`)

**File**: `.kiro/skills/bug-investigation/SKILL.md` (~900 lines)

**Purpose**: Systematic bug investigation and resolution

**9-Step Investigation Workflow**:
1. Reproduce the Bug
   - Gather information
   - Create minimal reproduction
   - Verify consistency

2. Isolate the Issue
   - Frontend vs backend
   - Layer identification
   - Code vs configuration

3. Analyze Root Cause
   - Logic errors
   - Missing validation
   - State management issues
   - Race conditions
   - Configuration problems
   - Data issues

4. Verify Against Specifications
   - Check spec/requirements.md
   - Check spec/state-machine.md
   - Check PROJECT_CONSTITUTION.md

5. Develop Fix Strategy
   - Code change
   - Test addition
   - Specification update
   - Architecture change

6. Implement Fix
   - Write failing test first (TDD)
   - Implement minimal fix
   - Run all tests
   - Update documentation

7. Validate Fix
   - Run reproduction steps
   - Regression tests
   - Edge cases
   - Test in target environment

8. Document Fix
   - Update docs/ai-review.md
   - Descriptive commit message
   - Link to issue

9. Prevent Recurrence
   - Add tests
   - Improve code
   - Update documentation
   - Update steering files

**Features**:
- Bug investigation template
- Common bug patterns (6 categories)
- Debugging tips
- Severity classification

---

### 4. Refactoring Skill (`#refactor`)

**File**: `.kiro/skills/refactor/SKILL.md` (~1,200 lines)

**Purpose**: Systematic code refactoring while maintaining correctness

**7-Step Refactoring Workflow**:
1. Ensure Good Test Coverage
   - Verify tests exist
   - All tests pass
   - Write characterization tests if needed

2. Identify Refactoring Opportunity
   - Code smells (duplication, long methods, etc.)
   - 6 common smells documented

3. Choose Refactoring Technique
   - Extract Method
   - Extract Class
   - Rename
   - Introduce Parameter Object
   - Replace Conditional with Polymorphism
   - Simplify Conditional

4. Make Small, Incremental Changes
   - One refactoring at a time
   - Run tests after each change
   - Commit frequently

5. Verify No Behavior Change
   - All tests still pass
   - No new warnings
   - Coverage unchanged
   - API contracts unchanged

6. Improve Code Quality
   - Add documentation
   - Improve naming
   - Add constants

7. Review and Clean Up
   - Final quality check
   - Standards compliance
   - Testing validation

**Features**:
- 6 refactoring techniques with before/after examples
- Code smell catalog
- Anti-patterns to avoid
- Commit message format

---

### 5. Performance Review Skill (`#performance-review`)

**File**: `.kiro/skills/performance-review/SKILL.md` (~1,100 lines)

**Purpose**: Identify, analyze, and optimize performance issues

**5-Step Optimization Workflow**:
1. Establish Baseline
   - Measure response times
   - Database query times
   - Memory usage
   - CPU utilization

2. Identify Performance Issues
   - N+1 query problem
   - Missing database indexes
   - Fetching too much data
   - Inefficient algorithms
   - Excessive object creation

3. Analyze with Profiling Tools
   - Enable SQL logging
   - Enable performance logging
   - Use Spring Boot Actuator

4. Optimize
   - Database optimization (JOIN FETCH, indexes, pagination)
   - Caching optimization
   - Code optimization (loops, data structures)
   - Algorithm optimization

5. Benchmark and Verify
   - Measure after optimization
   - Compare metrics
   - Verify correctness

**Features**:
- Common performance issues catalog
- Optimization techniques by category
- Performance testing guidance
- Target response times
- Before/after examples

---

## Deliverables

**Skills Created** (5 files, ~5,150 lines):
- `.kiro/skills/code-review/SKILL.md` (1,100 lines)
- `.kiro/skills/adr/SKILL.md` (850 lines)
- `.kiro/skills/bug-investigation/SKILL.md` (900 lines)
- `.kiro/skills/refactor/SKILL.md` (1,200 lines)
- `.kiro/skills/performance-review/SKILL.md` (1,100 lines)

**Summary Documentation** (1 file, updated):
- `SKILLS-SUMMARY.md` (updated with new skills)

**Total New Content**: 5 files, ~5,150 lines

**Previous Skill**:
- `.kiro/skills/prompt-history/SKILL.md` (from Prompt 011)

---

## Impact Assessment

**Immediate Impact**:
- ✅ Structured workflows for common tasks
- ✅ Consistent approach to code review
- ✅ Systematic debugging process
- ✅ Safe refactoring guidance
- ✅ Performance optimization framework

**Long-Term Impact**:
- Higher code quality (systematic review)
- Better architectural decisions (ADR documentation)
- Faster bug resolution (structured investigation)
- Safer refactoring (test-driven approach)
- Better performance (systematic optimization)

**For Developers**:
- On-demand expert guidance
- Reduced decision fatigue
- Learn best practices through examples
- Consistent approach across team

**For the Project**:
- Captured expert knowledge
- Repeatable processes
- Quality assurance built-in
- Continuous improvement culture

---

## Next Steps

1. ✅ 5 comprehensive skills created
2. ✅ SKILLS-SUMMARY.md updated
3. ⏸️ Test each skill with sample invocations
4. ⏸️ Collect usage feedback from team
5. ⏸️ Create additional skills as needed:
   - `#security-audit` - Security review
   - `#api-design` - API design workflow
   - `#test-strategy` - Test planning
6. ⏸️ Add more project-specific examples
7. ⏸️ Track skill usage statistics

---

## Lessons Learned

**What Went Well**:
- Comprehensive coverage of development workflows
- Practical, actionable steps in each skill
- Consistent structure across all skills
- Examples grounded in project context
- Integration with steering files and constitution

**What Could Be Improved**:
- Need to validate skills work in practice
- Should gather user feedback early
- Could create video tutorials for skill usage
- Need metrics to track effectiveness

**Key Takeaways**:
- Skills provide structured guidance for complex tasks
- Step-by-step workflows reduce cognitive load
- Examples are critical for understanding
- Skills work best when integrated with project context
- Manual invocation (`#skill-name`) keeps context focused

---

## References

- **SKILLS-SUMMARY.md** - Complete skills overview
- **PROJECT_CONSTITUTION.md** - Supreme governance
- **.kiro/steering/*.md** - Auto-included coding standards
- Prompt 011: Steering files creation
- Prompt 013: SpecKit master workflow (next)

---

**Prompt Number**: 012  
**Status**: Complete  
**Related Prompts**: 
- Prompt 011: Steering files creation
- Prompt 013: SpecKit creation (next)
