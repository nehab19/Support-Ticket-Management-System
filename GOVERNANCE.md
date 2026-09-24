# Project Governance

**Support Ticket Management System**  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Overview

This document provides a quick reference to the project's governance structure and links to detailed governance artifacts.

---

## Governance Documents

### 1. Project Constitution
**Location**: `PROJECT_CONSTITUTION.md`  
**Purpose**: Supreme technical authority establishing engineering principles, architectural decisions, and mandatory rules  
**Sections**: 15 comprehensive sections covering methodology, technology, architecture, business rules, testing, AI governance, and enforcement

**Key Highlights**:
- Spec-Driven Development methodology (mandatory)
- Strict layered architecture (Controller → Service → Repository → Database)
- Ticket state machine with 5 allowed transitions (all others rejected)
- Backend ownership of business rules
- No secrets in Git (critical requirement)
- Mandatory human review of AI-generated code
- Testing before feature completion

**When to Reference**: 
- Starting new feature development
- Reviewing pull requests
- Making architectural decisions
- Resolving technical disputes
- Onboarding new contributors

### 2. AI Review Log
**Location**: `docs/ai-review.md`  
**Purpose**: Document meaningful mistakes made by AI assistants during development  
**Update Frequency**: As issues are discovered  

**What to Document**:
- Logic errors and security vulnerabilities
- Architecture violations
- Missing validation or error handling
- Test gaps or incorrect tests
- Repeated patterns indicating AI misunderstanding

**What NOT to Document**:
- Minor syntax errors or typos
- Formatting issues
- Easily caught compiler errors

### 3. Prompt History
**Location**: `docs/prompt-history.md`  
**Purpose**: Summary of all important AI prompts and their outcomes  
**Update Frequency**: After each important prompt  

**Detailed History Files**: `.specstory/history/{number}-{description}.md`

**What to Preserve**:
- Prompts generating significant code (>100 lines)
- Architecture or design prompts
- Bug resolution prompts
- Pattern-establishing prompts
- Spec or documentation creation prompts

---

## State Machine Specification

### Ticket Status States

| State | Type | Description |
|-------|------|-------------|
| **OPEN** | Initial | Ticket created, awaiting work |
| **IN_PROGRESS** | Active | Ticket being actively worked on |
| **RESOLVED** | Completion | Work complete, awaiting verification |
| **CLOSED** | Terminal | Ticket finalized and archived |
| **CANCELLED** | Terminal | Ticket abandoned without completion |

### Allowed Transitions

```
OPEN → IN_PROGRESS   (Agent starts work)
OPEN → CANCELLED     (Cancel before work starts)
IN_PROGRESS → RESOLVED    (Complete work)
IN_PROGRESS → CANCELLED   (Cancel during work)
RESOLVED → CLOSED    (Verify and finalize)
```

### Rejected Transitions

**All other transitions are forbidden**, including:
- Any transition FROM terminal states (CLOSED, CANCELLED)
- Any transition TO OPEN (cannot return to initial state)
- Skipping states (e.g., OPEN → RESOLVED without IN_PROGRESS)
- Reverting (e.g., RESOLVED → IN_PROGRESS)

**Error Response**: HTTP 422 (Unprocessable Entity) with descriptive message

**Enforcement**: `TicketStateMachine` component in service layer validates before persistence

---

## Constitutional Requirements Summary

### Absolute Rules (Zero Tolerance)

#### 1. Security
- ❌ **NEVER** commit secrets to Git
- ✅ Use environment variables for all secrets
- ✅ Keep `.env`, `.env.local`, `application-local.properties` in `.gitignore`

#### 2. Architecture
- ❌ **NEVER** implement business rules only in frontend
- ✅ Backend MUST enforce all business rules
- ✅ Frontend is presentation layer only
- ✅ Follow layered architecture strictly

#### 3. State Machine
- ❌ **NEVER** allow invalid state transitions
- ✅ Validate all transitions through `TicketStateMachine`
- ✅ Reject forbidden transitions with HTTP 422
- ✅ Test state machine with property-based tests (≥100 iterations)

#### 4. Testing
- ❌ **NEVER** ship untested code
- ✅ Write tests before or during feature implementation
- ✅ All acceptance criteria must have corresponding tests
- ✅ Property tests must run ≥100 iterations

#### 5. API Contracts
- ❌ **NEVER** expose domain entities directly
- ✅ Use DTOs for all request/response bodies
- ✅ Document all endpoints (method, path, request, response, errors)
- ✅ Validate all inputs on backend

#### 6. AI-Assisted Development
- ❌ **NEVER** merge unreviewed AI-generated code
- ✅ Human MUST review all AI code line-by-line
- ✅ Test AI code thoroughly (automated + manual)
- ✅ Document meaningful AI mistakes in `docs/ai-review.md`

#### 7. Documentation
- ❌ **NEVER** skip documentation for important changes
- ✅ Preserve important AI prompts in `.specstory/history/`
- ✅ Update `docs/prompt-history.md` after each important prompt
- ✅ Create specs in `.kiro/specs/{feature-name}/` for all features

---

## Development Workflow

### 1. Feature Development

**Process**:
1. Create spec in `.kiro/specs/{feature-name}/`
   - Choose workflow: Requirements-First, Design-First, or Bugfix
   - Create `requirements.md` (user stories, EARS acceptance criteria)
   - Create `design.md` (architecture, components, data models, properties)
   - Create `tasks.md` (hierarchical task breakdown with traceability)
2. Implement following Test-Driven Development
   - Write tests first (or during implementation)
   - Implement to pass tests
   - Refactor while keeping tests green
3. Verify constitutional compliance
   - Backend validates all inputs
   - State machine enforces transitions
   - DTOs for API contracts
   - No secrets committed
4. Human review (if AI-assisted)
   - Review code line-by-line
   - Test thoroughly
   - Document issues in `docs/ai-review.md`
5. Submit pull request
   - Reference spec and requirements
   - Include test results
   - Complete PR checklist

### 2. Bug Fixing

**Process**:
1. Create bugfix spec in `.kiro/specs/{bug-name}/`
   - Write bug condition (current vs. expected behavior)
   - Identify root cause
2. Write reproduction test that FAILS
   - Demonstrates bug exists
   - Will PASS once bug is fixed
3. Implement fix following design approach
4. Verify reproduction test now PASSES
5. Run regression tests (ensure no new issues)
6. Submit pull request with spec reference

### 3. Code Review

**Checklist**:
- [ ] Code follows constitutional principles
- [ ] Layered architecture respected (no business logic in controller)
- [ ] Backend validates all inputs
- [ ] State machine validates all transitions
- [ ] DTOs used for API contracts (entities not exposed)
- [ ] Tests written and passing (unit, integration, property)
- [ ] No secrets committed (check carefully)
- [ ] Error responses follow standard format
- [ ] API endpoints documented
- [ ] JavaDoc for public APIs
- [ ] AI-generated code reviewed by human
- [ ] Meaningful AI mistakes documented
- [ ] Important prompts preserved

---

## Enforcement

### Violation Severity

| Severity | Examples | Consequence |
|----------|----------|-------------|
| **Critical** | Secrets in Git, Security vulnerabilities | Immediate rollback, Incident review |
| **Major** | Business logic only in frontend, Untested features, Invalid transitions allowed | PR rejection, Refactoring required |
| **Minor** | Inconsistent error format, Missing JavaDoc, Formatting issues | Request for correction in review |

### Resolution Process

1. **Violation Identified**: During PR review or post-merge audit
2. **Severity Assessment**: Classify as Critical, Major, or Minor
3. **Action Taken**:
   - **Critical**: Immediate revert, emergency fix, team notification
   - **Major**: PR rejection or follow-up PR required before next release
   - **Minor**: Comment in PR, request fix in current or follow-up PR
4. **Documentation**: Record in incident log (if critical) or PR comments
5. **Prevention**: Update checklist, add automated checks, team discussion

---

## Amendment Process

### Proposing Changes

To amend the constitution or governance structure:

1. **Create Pull Request**: Propose changes to `PROJECT_CONSTITUTION.md` or governance docs
2. **Document Rationale**: Explain why change is needed, what problem it solves
3. **Impact Assessment**: Analyze effect on existing code and practices
4. **Team Review**: Require approval from project lead or team consensus
5. **Update Version**: Increment version number and update date
6. **Communicate**: Notify all contributors of constitutional changes

### Recent Amendments

| Version | Date | Changes | Rationale |
|---------|------|---------|-----------|
| 1.0 | 2026-09-23 | Initial constitution | Establish governance framework |

---

## Quick Reference

### Essential Commands

**Backend**:
```bash
# Build
./gradlew build  # or: mvn clean package

# Run tests
./gradlew test   # or: mvn test

# Start application
./gradlew bootRun  # or: mvn spring-boot:run
```

**Frontend**:
```bash
# Install dependencies
npm install

# Run development server
npm run dev

# Build production
npm run build

# Run tests
npm test
```

### Important Paths

| Path | Purpose |
|------|---------|
| `PROJECT_CONSTITUTION.md` | Technical governance document |
| `GOVERNANCE.md` | This document (governance overview) |
| `docs/ai-review.md` | AI mistake log |
| `docs/prompt-history.md` | Prompt summary |
| `.specstory/history/` | Detailed prompt history |
| `.kiro/specs/` | Feature specifications |
| `README.md` | Project overview |
| `DEVELOPMENT.md` | Developer guide (to be created) |

### Key Contacts

| Role | Responsibility | Contact |
|------|----------------|---------|
| **Project Lead** | Constitutional amendments, Critical violations | TBD |
| **Tech Lead** | Architecture decisions, Design reviews | TBD |
| **QA Lead** | Testing standards, Test coverage | TBD |

---

## Resources

### Internal Documentation
- **Constitution**: `PROJECT_CONSTITUTION.md` - Complete governance rules
- **README**: Project overview, setup, features
- **Specs**: `.kiro/specs/` - Feature specifications
- **Prompt History**: `docs/prompt-history.md` - AI assistance log

### External References
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Jakarta Bean Validation**: https://beanvalidation.org/
- **jqwik (Property Testing)**: https://jqwik.net/
- **Next.js**: https://nextjs.org/
- **Flyway**: https://flywaydb.org/

### Methodology
- **Spec-Driven Development**: `.kiro/docs/` - Workflow guides (to be created)
- **EARS Patterns**: Easy Approach to Requirements Syntax
- **INCOSE Rules**: Requirements quality standards

---

## Getting Help

### Questions About...

**Governance & Process**: Read this document and `PROJECT_CONSTITUTION.md`

**Technical Architecture**: See constitution Section 4 (Architectural Principles)

**State Machine**: See constitution Section 5.1 (Ticket State Machine)

**Testing**: See constitution Section 9 (Testing Standards)

**AI Assistance**: See constitution Section 10 (AI-Assisted Development)

**Security**: See constitution Section 8 (Security and Secret Management)

### Escalation Path

1. **Check Documentation**: Constitution, specs, README
2. **Review Examples**: Existing code, completed specs
3. **Ask Team**: Slack, email, stand-up
4. **Escalate**: Project lead for constitutional interpretation

---

## Continuous Improvement

This governance framework is living documentation. As the project evolves:

- **Review Quarterly**: Assess if rules still serve project needs
- **Update Based on Experience**: Learn from mistakes and successes
- **Simplify Where Possible**: Remove unnecessary complexity
- **Strengthen Where Needed**: Add rules to prevent recurring issues

**Feedback**: All contributors are encouraged to suggest governance improvements via pull requests.

---

**Document Version**: 1.0  
**Maintained By**: Project Team  
**Last Review**: 2026-09-23  
**Next Review**: 2026-12-23 (Quarterly)
