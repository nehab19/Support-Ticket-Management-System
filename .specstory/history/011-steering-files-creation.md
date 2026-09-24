# Prompt 011: Steering Files and Skills Creation

**Date**: 2026-09-23  
**Session**: 4  
**Tool**: Kiro AI

---

## User Prompt

```
Create separate folder structure:
.kiro/
├── steering/
│   ├── java-springboot.md
│   ├── testing.md
│   ├── api-standards.md
│   ├── documentation.md
│   └── ai-development.md
└── skills/
    └── prompt-history/
        └── SKILL.md
```

**Context**: Following the project structure migration (Prompt 010), user requested creation of comprehensive steering files (auto-included AI guidelines) and skills (manual-inclusion workflows).

---

## Context

After establishing the new project structure, the `.kiro/` directory needed to be fully populated with:

**Steering Files** (`.kiro/steering/`):
- Auto-included in AI context
- Coding standards and conventions
- Project-wide patterns
- `inclusion: auto` in frontmatter

**Skills** (`.kiro/skills/`):
- Manually invoked with `#skill-name`
- Reusable workflows for specific tasks
- `inclusion: manual` in frontmatter

**Requirements**:
- Comprehensive coverage of all development aspects
- Good/bad examples (✅/❌) for clarity
- Consistent structure across all files
- Integration with PROJECT_CONSTITUTION.md
- Practical, actionable guidelines

---

## AI Response

Created complete steering file and skills structure:

### Steering Files Created (4 new + 1 existing)

#### 1. `.kiro/steering/testing.md` (~1,450 lines)
**Purpose**: Comprehensive testing standards

**Content**:
- Testing philosophy and principles
- Testing pyramid (unit, property-based, integration, E2E)
- JUnit 5 standards and naming conventions
- Spring Boot integration testing
- Property-based testing with jqwik (13 properties)
- Test organization and directory structure
- AssertJ assertion guidelines
- Test data management (builders/fixtures)
- Coverage targets (>80% line coverage)
- Testing anti-patterns to avoid
- CI/CD integration

**Key Sections**:
- Unit Testing (JUnit 5, Mockito, Given-When-Then)
- Integration Testing (H2, @SpringBootTest, MockMvc)
- Property-Based Testing (jqwik, 100+ iterations)
- Assertion Libraries (AssertJ preferred)
- Coverage Targets (JaCoCo)

---

#### 2. `.kiro/steering/api-standards.md` (~1,200 lines)
**Purpose**: REST API design standards

**Content**:
- RESTful design principles
- URL design (resource naming, hierarchical structure)
- HTTP methods usage (GET, POST, PATCH, DELETE)
- HTTP status codes (2xx, 4xx, 5xx)
- Request design (validation, query parameters)
- Response design (JSON structure, camelCase, ISO 8601)
- Error handling (standard format, validation errors)
- API versioning strategies
- Security considerations (CORS, JWT future)
- Documentation (OpenAPI/Swagger)
- Performance optimizations
- Complete CRUD examples

**Key Sections**:
- Core Principles (resource-oriented, stateless)
- URL Design (nouns not verbs, plural resources)
- Status Codes (200, 201, 400, 404, 422, etc.)
- Error Handling (consistent format)
- Versioning (URL vs header)

---

#### 3. `.kiro/steering/documentation.md` (~1,100 lines)
**Purpose**: Documentation standards

**Content**:
- Core documentation principles
- JavaDoc standards (class-level, method-level)
- Inline comment guidelines (when and how)
- README documentation structure
- API documentation format
- Architecture documentation (ADRs)
- Specification documents
- Test documentation
- Markdown standards
- Documentation maintenance and review schedule

**Key Sections**:
- Code Documentation (JavaDoc for public APIs)
- Inline Comments (explain WHY, not WHAT)
- README Structure (features, installation, usage)
- Architecture Decision Records (ADR format)
- Maintenance Schedule (weekly, monthly, quarterly)

---

#### 4. `.kiro/steering/ai-development.md` (~1,000 lines)
**Purpose**: AI-assisted development best practices

**Content**:
- AI usage policy (when to use, when to avoid)
- Mandatory 7-step code review process
- Common AI mistakes with examples (5 categories):
  1. Skipping business rule validation
  2. Exposing JPA entities directly
  3. Field injection instead of constructor
  4. Missing @Transactional annotations
  5. EnumType.ORDINAL instead of STRING
- Prompt engineering guidelines
- AI-generated test code standards
- Mistake documentation process
- Prompt preservation guidelines
- AI tool comparison (Copilot, Cursor, ChatGPT, Kiro)
- Continuous improvement process
- Red flags and best practices

**Key Sections**:
- Core Principles (human oversight, trust but verify)
- AI Usage Policy (appropriate uses, caution areas)
- Code Review Process (7-step checklist)
- Common AI Mistakes (with fixes)
- Mistake Documentation Format

---

### Skills Created (1 initial skill)

#### 5. `.kiro/skills/prompt-history/SKILL.md` (~850 lines)
**Purpose**: Structured workflow for managing AI prompt history

**Content**:
- When to use this skill (significance assessment)
- 6-step workflow:
  1. Assess Significance
  2. Gather Context
  3. Choose File Name (NNN-description.md)
  4. Create Detailed History File
  5. Update Summary
  6. Review and Commit
- Template for detailed history files (11 sections)
- Template for summary entries
- Examples (code gen, architecture, bug fix)
- Tips for writing good context and lessons
- Maintenance and review guidelines
- Statistics tracking template

**Key Features**:
- Structured documentation workflow
- Consistent file naming (NNN-description.md)
- Complete templates
- Examples from project

---

## Deliverables

**Steering Files** (4 new files, ~4,750 lines):
- `.kiro/steering/testing.md` (1,450 lines)
- `.kiro/steering/api-standards.md` (1,200 lines)
- `.kiro/steering/documentation.md` (1,100 lines)
- `.kiro/steering/ai-development.md` (1,000 lines)

**Skills** (1 file, ~850 lines):
- `.kiro/skills/prompt-history/SKILL.md` (850 lines)

**Summary Documentation** (1 file, ~800 lines):
- `STEERING-FILES-SUMMARY.md` (800 lines)

**Total**: 6 files, ~6,400 lines

**Existing File**:
- `.kiro/steering/java-springboot.md` (already existed)

---

## Impact Assessment

**Immediate Impact**:
- ✅ AI always has project context (auto-included steering files)
- ✅ Comprehensive coding standards documented
- ✅ Common AI mistakes documented with examples
- ✅ Structured workflow for prompt documentation
- ✅ Consistent patterns across all steering files

**Long-Term Impact**:
- Higher code quality through AI guidance
- Faster onboarding (new developers have clear standards)
- Reduced AI mistakes (examples of what to avoid)
- Better knowledge capture (prompt history workflow)
- Improved consistency across team
- Continuous learning from AI interactions

**For AI Assistants**:
- Always have context without manual prompting
- Follow project standards automatically
- Avoid common mistakes with clear examples
- Generate better code from the start

**For Human Developers**:
- Reference for code reviews
- Onboarding documentation
- Learning from AI mistake patterns
- Consistency guidelines

---

## Next Steps

1. ✅ All core steering files created
2. ✅ Prompt history skill created
3. ⏸️ Test AI awareness of new guidelines
4. ⏸️ Create first prompt history entry using skill
5. ⏸️ Review and refine based on usage
6. ⏸️ Create additional skills as needed
7. ⏸️ Quarterly review of all steering files

---

## Lessons Learned

**What Went Well**:
- Comprehensive coverage across all development aspects
- Consistent structure with good/bad examples
- Clear separation: steering (auto) vs skills (manual)
- Examples grounded in actual project (state machine, tickets, etc.)
- Integration with PROJECT_CONSTITUTION.md

**What Could Be Improved**:
- Could add more project-specific examples over time
- Need to validate AI actually follows the guidelines
- Should track which guidelines are most helpful
- Consider creating more specialized skills

**Key Takeaways**:
- Steering files dramatically improve AI code quality
- Good/bad examples are extremely valuable
- Separation of auto vs manual inclusion is important
- Skills provide structured workflows for complex tasks
- Documentation should be living and evolving

---

## References

- **PROJECT_STRUCTURE.md** - Canonical structure definition
- **STEERING-FILES-SUMMARY.md** - Complete summary of created files
- **PROJECT_CONSTITUTION.md** - Supreme governance document
- **MIGRATION-SUMMARY.md** - Project structure migration
- Prompt 010: Project structure migration

---

**Prompt Number**: 011  
**Status**: Complete  
**Related Prompts**: 
- Prompt 010: Project structure migration
- Prompt 012: Additional skills creation (next)
