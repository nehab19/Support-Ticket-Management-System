# Project Structure

**Support Ticket Management System**  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Overview

This document defines the canonical structure for organizing specifications, documentation, AI steering files, and prompt history.

---

## Directory Structure

```
support-ticket-system/
│
├── spec/                                    # SPECIFICATIONS (What to build)
│   ├── requirements.md                      # User stories, acceptance criteria (EARS)
│   ├── architecture.md                      # System architecture, component design
│   ├── data-model.md                        # Database schemas, entities, relationships
│   ├── api-contract.md                      # REST endpoints, request/response formats
│   ├── state-machine.md                     # Ticket status state machine definition
│   ├── ui-flow.md                           # User interface flows and wireframes
│   └── test-strategy.md                     # Testing approach, property-based testing
│
├── docs/                                    # DOCUMENTATION (Project artifacts)
│   ├── prompt-history.md                    # Summary of all AI prompts
│   ├── ai-review.md                         # AI-generated code review log
│   ├── architecture-decisions.md            # ADRs (Architecture Decision Records)
│   └── testing-report.md                    # Test results, coverage reports
│
├── .specstory/                              # PROMPT HISTORY (Detailed AI interactions)
│   └── history/
│       ├── 001-project-start.md
│       ├── 002-run-project.md
│       ├── 003-start-fe-be.md
│       ├── 004-restart-applications.md
│       ├── 005-github-setup.md
│       ├── 006-github-push-resolution.md
│       ├── 007-prompt-history-creation.md
│       ├── 008-project-constitution.md
│       └── 009-java-springboot-guidelines.md
│
├── .kiro/                                   # KIRO AI CONFIGURATION
│   ├── steering/                            # AI coding guidelines (auto-included)
│   │   ├── java-springboot.md              # Java/Spring Boot standards
│   │   ├── testing.md                       # Testing guidelines
│   │   ├── api-standards.md                 # API design standards
│   │   ├── documentation.md                 # Documentation standards
│   │   └── ai-development.md                # AI-assisted development guidelines
│   │
│   ├── skills/                              # Custom skills (manual inclusion)
│   │   ├── prompt-history/
│   │   │   └── SKILL.md                     # Document AI interactions
│   │   ├── code-review/
│   │   │   └── SKILL.md                     # Systematic code review
│   │   ├── adr/
│   │   │   └── SKILL.md                     # Architecture Decision Records
│   │   ├── bug-investigation/
│   │   │   └── SKILL.md                     # Bug investigation workflow
│   │   ├── refactor/
│   │   │   └── SKILL.md                     # Code refactoring workflow
│   │   └── performance-review/
│   │       └── SKILL.md                     # Performance optimization
│   │
│   └── specs/                               # LEGACY: Old spec structure (to be migrated)
│       ├── support-ticket-management/
│       ├── migrate-to-h2-database/
│       └── spec-driven-development-workflow/
│
├── PROJECT_CONSTITUTION.md                  # Supreme governance document
├── GOVERNANCE.md                            # Governance overview
├── README.md                                # Project overview
├── DEVELOPMENT.md                           # Developer guide (to be created)
│
├── backend/                                 # Java/Spring Boot application
├── frontend/                                # Next.js/React application
└── .gitignore                               # Git ignore rules
```

---

## Directory Purposes

### 1. `spec/` - Specification Documents

**Purpose**: Define WHAT to build (requirements, architecture, contracts)

**Contents**:
- **requirements.md**: User stories with EARS acceptance criteria
- **architecture.md**: System architecture, layers, components
- **data-model.md**: Database schema, entities, relationships, indexes
- **api-contract.md**: REST endpoints, DTOs, error responses
- **state-machine.md**: Ticket status transitions (5 allowed, others rejected)
- **ui-flow.md**: User flows, wireframes, component hierarchy
- **test-strategy.md**: Testing approach, property-based testing plan

**When to Update**:
- Starting new feature development
- Changing architecture or design
- Adding new API endpoints
- Modifying data models
- Updating business rules

**Audience**: Developers, stakeholders, architects

---

### 2. `docs/` - Project Documentation

**Purpose**: Document artifacts, decisions, and reports

**Contents**:
- **prompt-history.md**: Summary of all important AI prompts and outcomes
- **ai-review.md**: Log of meaningful AI mistakes and patterns
- **architecture-decisions.md**: ADRs documenting key architectural choices
- **testing-report.md**: Test results, coverage, property test outcomes

**When to Update**:
- After important AI interactions
- After finding significant AI mistakes
- After making architectural decisions
- After running test suites

**Audience**: Team members, future developers, auditors

---

### 3. `.specstory/` - Detailed Prompt History

**Purpose**: Preserve complete AI interaction history

**Contents**:
- **history/{number}-{description}.md**: Full prompt text, context, AI response, decisions made

**Naming Convention**: `{number}-{description}.md`
- Example: `008-project-constitution.md`
- Example: `009-java-springboot-guidelines.md`

**When to Create**:
- After significant code generation (>100 lines)
- After architecture/design decisions
- After bug resolutions
- After creating specs or documentation
- After establishing patterns/conventions

**Audience**: AI reviewers, team historians, future AI training

---

### 4. `.kiro/steering/` - AI Coding Guidelines

**Purpose**: Automatic AI context (coding standards, conventions, patterns)

**Contents**:
- **java-springboot.md**: Java 21, Spring Boot, Gradle, JPA standards
- **testing.md**: Unit, integration, property-based testing guidelines
- **api-standards.md**: REST API design, error handling, versioning
- **documentation.md**: JavaDoc, inline comments, README standards
- **ai-development.md**: AI-assisted development best practices

**File Format**:
```markdown
---
inclusion: auto
---

# Title

Content...
```

**Auto-Inclusion**: Files with `inclusion: auto` are automatically loaded into AI context

**When to Create**:
- Establishing coding standards
- Defining project-wide patterns
- Setting up conventions for consistency

**Audience**: AI assistants (primary), human developers (reference)

---

### 5. `.kiro/skills/` - Custom Kiro Skills

**Purpose**: Reusable AI skills for specific tasks (manual inclusion)

**Contents**:
- **prompt-history/SKILL.md**: Document significant AI interactions
- **code-review/SKILL.md**: Systematic code review workflow
- **adr/SKILL.md**: Create Architecture Decision Records
- **bug-investigation/SKILL.md**: Investigate and resolve bugs
- **refactor/SKILL.md**: Refactor code systematically
- **performance-review/SKILL.md**: Optimize performance issues

**File Format**:
```markdown
---
inclusion: manual
---

# Skill Name

Description and instructions...
```

**Manual Inclusion**: Use `#` in chat to include specific skill

**When to Create**:
- Creating reusable workflows
- Defining complex, multi-step processes
- Building domain-specific capabilities

**Audience**: AI assistants (when invoked)

---

### 6. `.kiro/specs/` - Legacy Spec Structure

**Purpose**: Old spec structure (to be migrated to top-level `spec/`)

**Status**: **DEPRECATED** - Will be restructured

**Current Contents**:
- support-ticket-management/
- migrate-to-h2-database/
- spec-driven-development-workflow/

**Migration Plan**:
- Extract content to top-level `spec/` directory
- Split monolithic design.md into separate files
- Archive or remove after migration

---

## File Naming Conventions

### Specification Files
- Lowercase with hyphens: `requirements.md`, `api-contract.md`
- Descriptive, specific: `state-machine.md` not `states.md`

### Documentation Files
- Lowercase with hyphens: `prompt-history.md`, `ai-review.md`
- Present tense: `testing-report.md` not `test-results.md`

### Prompt History Files
- Format: `{number}-{description}.md`
- Number: Zero-padded 3 digits (001, 002, 003...)
- Description: Kebab-case, concise (project-start, github-setup)

### Steering Files
- Lowercase with hyphens: `java-springboot.md`, `api-standards.md`
- Domain-specific: `testing.md`, `documentation.md`

### Skills
- Directory per skill: `prompt-history/`
- Main file: `SKILL.md` (uppercase)

---

## Content Guidelines

### Specification Files

**requirements.md**:
- User stories: "As a [role], I want [capability], so that [benefit]"
- Acceptance criteria using EARS patterns (Ubiquitous, Event-driven, State-driven, Unwanted event, Optional, Complex)
- Glossary of domain terms
- Requirement traceability

**architecture.md**:
- System context diagram
- Component architecture
- Layer responsibilities
- Technology stack decisions
- Design patterns used

**data-model.md**:
- Entity definitions with fields and types
- Relationships (one-to-many, many-to-many)
- Database indexes
- Migration strategy

**api-contract.md**:
- Endpoint table (Method, Path, Description, Status codes)
- Request/Response DTOs with JSON examples
- Error response formats
- Authentication/Authorization requirements

**state-machine.md**:
- State definitions
- Allowed transitions (exhaustive list)
- Forbidden transitions (explicit)
- Transition validation rules
- Error handling

**ui-flow.md**:
- User journey maps
- Wireframes or mockups
- Component hierarchy
- State management approach

**test-strategy.md**:
- Test types (unit, integration, property-based)
- Coverage expectations
- Property test specifications
- Test data generation strategy

---

### Documentation Files

**prompt-history.md**:
- Date, tool, purpose, outcome for each prompt
- Link to detailed history in `.specstory/history/`
- Summary statistics

**ai-review.md**:
- Date, tool, task, issue, impact, resolution, lesson
- Pattern identification
- Statistics (total reviews, issues by severity)

**architecture-decisions.md**:
- ADR format: Title, Status, Context, Decision, Consequences
- Chronological order
- Immutable (new ADRs supersede old ones)

**testing-report.md**:
- Test execution results
- Coverage metrics
- Property test outcomes (iterations, failures)
- Performance benchmarks

---

### Prompt History Files (.specstory/history/)

**Structure**:
```markdown
# Prompt {number}: {Title}

**Date**: YYYY-MM-DD
**Session**: N
**Tool**: Kiro AI

---

## User Prompt

```
Full user request text
```

---

## Context

Background, rationale, constraints

---

## AI Response

What was created, decisions made, code examples

---

## Deliverables

Files created/updated with line counts

---

## Impact Assessment

Immediate and long-term impacts

---

## Next Steps

Follow-up actions

---

## Lessons Learned

Key takeaways

---

## References

Links to related documents
```

---

### Steering Files (.kiro/steering/)

**Structure**:
```markdown
---
inclusion: auto
---

# {Topic} Guidelines

**Purpose**: Brief description

---

## Section 1

Guidelines with ✅ GOOD and ❌ BAD examples

---

## Section 2

More guidelines...

---

## Summary

Key principles
```

**Content Requirements**:
- Clear, actionable rules
- Good and bad examples for each guideline
- Rationale for each rule
- Code snippets demonstrating patterns

---

## Migration Plan

### Phase 1: Create New Structure (Immediate)
1. Create top-level `spec/` directory
2. Create individual spec files (requirements.md, architecture.md, etc.)
3. Create missing steering files (testing.md, api-standards.md, etc.)
4. Create `docs/architecture-decisions.md`
5. Create `docs/testing-report.md`

### Phase 2: Extract Content (Week 1)
1. Extract from `.kiro/specs/support-ticket-management/design.md`:
   - Architecture → `spec/architecture.md`
   - Data Models → `spec/data-model.md`
   - API Endpoints → `spec/api-contract.md`
   - State Machine → `spec/state-machine.md`
   - Testing → `spec/test-strategy.md`
2. Copy requirements to `spec/requirements.md`
3. Document architectural decisions in `docs/architecture-decisions.md`

### Phase 3: Deprecate Old Structure (Week 2)
1. Mark `.kiro/specs/` as deprecated
2. Update all documentation references
3. Archive old structure (optional)

---

## Usage Examples

### Starting New Feature

1. **Create spec files**:
   ```bash
   # Add to spec/requirements.md
   # Update spec/architecture.md (if needed)
   # Update spec/data-model.md (if new entities)
   # Update spec/api-contract.md (if new endpoints)
   # Update spec/test-strategy.md
   ```

2. **Reference steering files**:
   - AI automatically includes `.kiro/steering/*.md` files
   - Follow patterns in java-springboot.md, testing.md, etc.

3. **Document decisions**:
   - Add ADR to `docs/architecture-decisions.md`

4. **Preserve prompts**:
   - Add summary to `docs/prompt-history.md`
   - Create detailed file in `.specstory/history/`

### Code Review

1. **Check compliance**:
   - Verify against `spec/requirements.md`
   - Verify against `.kiro/steering/java-springboot.md`
   - Verify against `PROJECT_CONSTITUTION.md`

2. **Document AI mistakes**:
   - Add to `docs/ai-review.md` if significant

3. **Update tests**:
   - Record results in `docs/testing-report.md`

---

## Quick Reference

| Need | File | Location |
|------|------|----------|
| What to build | requirements.md | `spec/` |
| How system works | architecture.md | `spec/` |
| Database schema | data-model.md | `spec/` |
| API endpoints | api-contract.md | `spec/` |
| State transitions | state-machine.md | `spec/` |
| UI flows | ui-flow.md | `spec/` |
| Test plan | test-strategy.md | `spec/` |
| Coding standards | java-springboot.md | `.kiro/steering/` |
| Testing standards | testing.md | `.kiro/steering/` |
| API standards | api-standards.md | `.kiro/steering/` |
| Prompt summary | prompt-history.md | `docs/` |
| Prompt details | {number}-{name}.md | `.specstory/history/` |
| AI mistakes | ai-review.md | `docs/` |
| Design decisions | architecture-decisions.md | `docs/` |
| Test results | testing-report.md | `docs/` |

---

## Maintenance

**Weekly**:
- Review `docs/ai-review.md` for patterns
- Update `docs/testing-report.md` after test runs
- Check `spec/` files are up to date

**Monthly**:
- Review `docs/architecture-decisions.md` relevance
- Archive old `.specstory/history/` files (optional)
- Audit steering files for updates

**Quarterly**:
- Review entire `spec/` directory for accuracy
- Update `PROJECT_CONSTITUTION.md` if needed
- Consolidate lessons from `docs/ai-review.md`

---

**Version**: 1.0  
**Status**: Active  
**Next Review**: 2026-10-23
