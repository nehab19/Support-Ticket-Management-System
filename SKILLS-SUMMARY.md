# Kiro Skills Summary

**Created**: 2026-09-23  
**Status**: ✅ Complete

---

## Overview

This document summarizes all custom Kiro skills created for the Support Ticket Management System. Skills are reusable AI workflows that provide structured guidance for specific tasks.

**Total Skills**: 7  
**Location**: `.kiro/skills/`  
**Invocation**: Use `#skill-name` in chat

**⭐ Master Skill**: SpecKit - Complete spec-driven development workflow

---

## Skills Directory

```
.kiro/skills/
├── speckit/
│   └── SKILL.md              # ⭐ MASTER: Complete spec-driven workflow
├── prompt-history/
│   └── SKILL.md              # Document significant AI interactions
├── code-review/
│   └── SKILL.md              # Systematic code review workflow
├── adr/
│   └── SKILL.md              # Create Architecture Decision Records
├── bug-investigation/
│   └── SKILL.md              # Investigate and resolve bugs
├── refactor/
│   └── SKILL.md              # Refactor code systematically
└── performance-review/
    └── SKILL.md              # Optimize performance issues
```

---

## Skills Reference

### ⭐ 0. SpecKit (`#speckit`) - MASTER WORKFLOW

**Purpose**: Complete spec-driven development from idea to production

**When to Use**:
- Starting a new feature from scratch
- Need end-to-end guidance
- Want to follow complete methodology
- Ensuring constitutional compliance throughout

**The 12 Phases**:
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

**Key Features**:
- 12-phase structured workflow
- Constitutional compliance at each phase
- Integration with all other skills
- Complete artifact checklist
- Quality gates at each phase

**Outputs**:
- Complete specifications
- Production-ready code
- Comprehensive tests
- Documentation
- Pull request

**Example Invocation**:
```
#speckit

I want to build a user authentication feature. 
Guide me through the complete process.
```

---

### 1. Prompt History (`#prompt-history`)

**Purpose**: Document significant AI interactions for future reference and learning

**When to Use**:
- After generating >100 lines of code
- After architectural decisions
- After complex bug resolutions
- After creating specs or documentation
- When establishing patterns

**Key Features**:
- 6-step structured workflow
- Template for detailed history files
- Summary entry template
- Significance assessment criteria
- Maintenance guidelines

**Outputs**:
- `.specstory/history/NNN-description.md` (detailed)
- `docs/prompt-history.md` (summary entry)

**Example Invocation**:
```
#prompt-history

I just completed a major refactoring of the state machine.
Can you help me document this?
```

---

### 2. Code Review (`#code-review`)

**Purpose**: Systematic code review following constitutional requirements and standards

**When to Use**:
- Reviewing AI-generated code
- Conducting pull request reviews
- Self-reviews before committing
- Validating refactored code

**Key Features**:
- 8-step review workflow:
  1. Architecture compliance
  2. Security review
  3. Code quality
  4. Error handling
  5. Testing review
  6. Documentation review
  7. State machine validation
  8. Data persistence review
- Common AI mistakes catalog
- Quick reference checklist
- Review report template

**Covers**:
- Layered architecture adherence
- Security vulnerabilities
- Java/Spring Boot standards
- Test coverage and quality
- Documentation completeness

**Example Invocation**:
```
#code-review

Please review the TicketService class I just created.
```

---

### 3. ADR (Architecture Decision Record) (`#adr`)

**Purpose**: Document significant architectural and design decisions

**When to Use**:
- Making technology choices
- Establishing design patterns
- Changing existing architecture
- Resolving architectural debates
- Making trade-offs with long-term implications

**Key Features**:
- 7-step ADR creation workflow:
  1. Identify decision
  2. Gather context
  3. Identify options
  4. Make decision
  5. Document consequences
  6. Write ADR
  7. Update and communicate
- Standard ADR template
- Examples from the project
- Status value definitions

**ADR Format**:
```markdown
## ADR-NNN: Title
**Date**: YYYY-MM-DD
**Status**: Accepted

### Context
[Problem and constraints]

### Decision
[What we decided]

### Consequences
[Positive, negative, neutral]

### Alternatives Considered
[Other options and why rejected]
```

**Example Invocation**:
```
#adr

We need to decide whether to use separate endpoints for status 
transitions or combine with the generic update endpoint.
```

---

### 4. Bug Investigation (`#bug-investigation`)

**Purpose**: Systematic bug investigation and resolution

**When to Use**:
- Bug has been reported
- Tests are failing unexpectedly
- System behavior doesn't match requirements
- Production issues need investigation

**Key Features**:
- 9-step investigation workflow:
  1. Reproduce the bug
  2. Isolate the issue
  3. Analyze root cause
  4. Verify against specifications
  5. Develop fix strategy
  6. Implement fix
  7. Validate fix
  8. Document fix
  9. Prevent recurrence
- Bug investigation template
- Common bug patterns catalog
- Debugging tips
- Severity classification

**Root Cause Categories**:
- Logic errors
- Missing validation
- Incorrect state management
- Race conditions
- Configuration issues
- Data issues

**Example Invocation**:
```
#bug-investigation

The state machine is rejecting the OPEN→CANCELLED transition 
but the spec says this should be allowed. Help me investigate.
```

---

### 5. Refactoring (`#refactor`)

**Purpose**: Systematic code refactoring while maintaining correctness

**When to Use**:
- Code has duplication
- Methods or classes too complex
- Code doesn't follow standards
- Improving readability/maintainability
- Reducing technical debt

**Key Features**:
- 7-step refactoring workflow:
  1. Ensure good test coverage
  2. Identify refactoring opportunity
  3. Choose refactoring technique
  4. Make small, incremental changes
  5. Verify no behavior change
  6. Improve code quality
  7. Review and clean up
- 6 refactoring techniques with examples
- Code smell catalog
- Anti-patterns to avoid
- Refactoring checklist

**Refactoring Techniques**:
- Extract Method
- Extract Class
- Rename
- Introduce Parameter Object
- Replace Conditional with Polymorphism
- Simplify Conditional

**Example Invocation**:
```
#refactor

The TicketService has a lot of duplicate code in the update 
methods. Help me refactor this to remove duplication.
```

---

### 6. Performance Review (`#performance-review`)

**Purpose**: Identify, analyze, and optimize performance issues

**When to Use**:
- API responses are slow (>1 second)
- Database queries inefficient
- Memory usage high
- Application startup slow
- Investigating performance regression

**Key Features**:
- 5-step optimization workflow:
  1. Establish baseline
  2. Identify performance issues
  3. Analyze with profiling tools
  4. Optimize
  5. Benchmark and verify
- Common performance issues catalog
- Optimization techniques by category
- Performance testing guidance
- Target response times

**Optimization Categories**:
- Database optimization (indexes, N+1, pagination)
- Caching optimization
- Code optimization (loops, data structures)
- Algorithm optimization
- Memory optimization

**Example Invocation**:
```
#performance-review

The GET /api/tickets endpoint is taking 2 seconds to respond 
with 1000 tickets. Help me optimize this.
```

---

## Usage Examples

### Example 1: After Code Generation

```
User: [Creates TicketService with AI]

User: #code-review
Please review the TicketService I just created with AI assistance.

AI: [Follows code-review skill workflow]
- Checks architecture compliance
- Reviews security
- Validates testing
- etc.
```

---

### Example 2: Making Architectural Decision

```
User: #adr
We need to decide on our caching strategy. Should we use 
Caffeine, Redis, or something else?

AI: [Follows ADR workflow]
- Gathers context
- Lists options with pros/cons
- Helps document decision
- Creates ADR in docs/architecture-decisions.md
```

---

### Example 3: Bug Investigation

```
User: #bug-investigation
Tests are failing with "Cannot transition from OPEN to CANCELLED"
but the spec says this should be valid.

AI: [Follows bug-investigation workflow]
- Helps reproduce consistently
- Analyzes root cause
- Develops fix strategy
- Documents in ai-review.md
```

---

### Example 4: Refactoring Session

```
User: #refactor
I have duplicate code in my service methods. Let's clean this up.

AI: [Follows refactor workflow]
- Identifies code smells
- Suggests refactoring technique (Extract Method)
- Makes incremental changes
- Runs tests after each step
```

---

### Example 5: Performance Optimization

```
User: #performance-review
The /api/tickets endpoint is slow with 1000 records.

AI: [Follows performance-review workflow]
- Establishes baseline metrics
- Identifies N+1 query problem
- Suggests JOIN FETCH optimization
- Verifies improvement
```

---

## How Skills Work

### Invocation
Skills are invoked with the `#` prefix:
```
#skill-name
```

### Context Loading
When invoked, Kiro loads the skill's `SKILL.md` file into context and follows the structured workflow defined within.

### Manual Inclusion
All skills have `inclusion: manual` in their frontmatter, meaning they're only loaded when explicitly requested.

### Workflow Execution
The AI follows the step-by-step workflow in the skill, asking clarifying questions and providing structured guidance.

---

## Skill Development Guidelines

### Creating New Skills

**Structure**:
```markdown
---
inclusion: manual
---

# Skill Name

**Skill Name**: [Name]
**Version**: 1.0
**Last Updated**: YYYY-MM-DD

## Purpose
[What the skill does]

## When to Use This Skill
[Invocation triggers]

## Workflow
[Step-by-step process]

## Examples
[Usage examples]

## Checklist
[Quick reference]
```

**Best Practices**:
- One skill = one workflow
- Clear step-by-step instructions
- Include examples
- Provide checklists
- Keep focused and actionable

---

## Skills vs Steering Files

| Feature | Skills | Steering Files |
|---------|--------|----------------|
| **Inclusion** | Manual (`#skill-name`) | Automatic (always loaded) |
| **Purpose** | Workflows/processes | Coding standards/guidelines |
| **When Used** | On-demand for specific tasks | Always in AI context |
| **Examples** | code-review, refactor, adr | java-springboot.md, testing.md |
| **Size** | 500-1000 lines | 1000-1500 lines |

---

## Skill Invocation Quick Reference

| Task | Skill | Command |
|------|-------|---------|
| **Complete feature workflow** | **SpecKit** | `#speckit` ⭐ |
| Document AI prompt | Prompt History | `#prompt-history` |
| Review code | Code Review | `#code-review` |
| Make architecture decision | ADR | `#adr` |
| Investigate bug | Bug Investigation | `#bug-investigation` |
| Refactor code | Refactoring | `#refactor` |
| Optimize performance | Performance Review | `#performance-review` |

---

## Maintenance

### Updating Skills

When updating skills:
1. Increment version number
2. Update "Last Updated" date
3. Document changes in skill
4. Test with sample invocations
5. Update this summary if needed

### Adding New Skills

When adding new skills:
1. Create directory: `.kiro/skills/new-skill-name/`
2. Create `SKILL.md` with proper frontmatter
3. Follow skill structure guidelines
4. Test invocation
5. Update this summary document

### Quarterly Review

Review all skills quarterly:
- [ ] Verify workflows are still relevant
- [ ] Update examples with real project scenarios
- [ ] Add new patterns/anti-patterns discovered
- [ ] Check for consistency across skills
- [ ] Update version numbers

---

## Integration with Project Governance

**Skills complement the governance structure**:

```
PROJECT_CONSTITUTION.md          # High-level principles
├── GOVERNANCE.md                # Quick reference
├── .kiro/steering/*.md          # Coding standards (auto)
└── .kiro/skills/*.md            # Workflows (manual)
    ├── #code-review             # Enforce standards
    ├── #adr                     # Document decisions
    ├── #bug-investigation       # Systematic debugging
    ├── #refactor                # Improve code quality
    └── #performance-review      # Optimize performance
```

---

## Statistics

**Total Skills**: 7 (including 1 master workflow)  
**Total Lines**: ~10,000+  
**Average Skill Size**: ~1,400 lines

**Breakdown by Size**:
- **speckit: ~4,000 lines** ⭐ (Master workflow)
- prompt-history: ~850 lines
- code-review: ~1,100 lines
- adr: ~850 lines
- bug-investigation: ~900 lines
- refactor: ~1,200 lines
- performance-review: ~1,100 lines

---

## Benefits

### For AI Assistants
- Structured workflows for complex tasks
- Consistent approach to common problems
- Clear guidance on project standards
- Step-by-step processes to follow

### For Developers
- On-demand expert guidance
- Systematic problem-solving
- Learning tool for best practices
- Consistency across team

### For the Project
- Captured expert knowledge
- Repeatable processes
- Quality assurance
- Continuous improvement

---

## Next Steps

### Immediate
1. ✅ All 6 core skills created
2. ⏸️ Test each skill with sample invocations
3. ⏸️ Share with team for feedback

### Short-term
4. ⏸️ Create additional skills as needs arise:
   - `#security-audit` - Security review workflow
   - `#api-design` - API design best practices
   - `#test-strategy` - Test planning workflow
5. ⏸️ Add real project examples to existing skills
6. ⏸️ Create video/documentation for skill usage

### Long-term
7. ⏸️ Quarterly skill reviews and updates
8. ⏸️ Track skill usage statistics
9. ⏸️ Evolve skills based on team feedback

---

## Quick Start Guide

### For New Team Members

1. **Review this summary** to understand available skills
2. **Try a skill**: `#code-review` on existing code
3. **Follow the workflow** provided by the AI
4. **Refer to examples** in skill documentation
5. **Provide feedback** for improvements

### For Experienced Users

1. **Invoke skills proactively** during development
2. **Combine skills** for complex tasks (refactor → code-review)
3. **Customize workflows** based on specific needs
4. **Contribute improvements** based on experience

---

## References

- **PROJECT_STRUCTURE.md** - Skill location and organization
- **PROJECT_CONSTITUTION.md** - Governance principles
- **STEERING-FILES-SUMMARY.md** - Coding standards documentation
- Individual skill files in `.kiro/skills/*/SKILL.md`

---

## Conclusion

Six comprehensive skills have been created to support systematic development workflows:

✅ **SpecKit** ⭐ - Complete spec-driven development (12 phases)  
✅ **Prompt History** - Document significant AI interactions  
✅ **Code Review** - Systematic review following standards  
✅ **ADR** - Document architectural decisions  
✅ **Bug Investigation** - Systematic debugging workflow  
✅ **Refactoring** - Improve code quality safely  
✅ **Performance Review** - Identify and optimize performance issues

These skills provide:
- **Structured guidance** for complex tasks
- **Consistent approaches** across the team
- **Quality assurance** through systematic processes
- **Knowledge capture** of best practices

---

**Creation Completed**: 2026-09-23  
**Created By**: Kiro AI  
**Status**: ✅ Ready for Use  
**Total Content**: ~6,000 lines of workflow guidance

