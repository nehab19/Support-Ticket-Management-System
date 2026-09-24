# Prompt 008: Create Project Constitution

**Date**: 2026-09-23  
**Session**: 1  
**Tool**: Kiro AI

---

## User Prompt

```
Create the project constitution for a Support Ticket Management System. This project is being developed using Spec-Driven Development. Establish engineering principles for:
- Java 21
- Spring Boot
- Gradle
- PostgreSQL as the primary database
- H2 for lightweight tests where appropriate
- REST API
- DTO-based API contracts
- React/Next.js frontend
- layered backend architecture
- backend ownership of business rules
- strict ticket state-machine enforcement
- backend validation
- consistent error handling
- automated testing
- integration testing
- database persistence
- security and secret management
- documentation
- AI-assisted development
- human review of AI-generated code

The application must not accept invalid ticket state transitions. Required state machine:
OPEN -> IN_PROGRESS
IN_PROGRESS -> RESOLVED
RESOLVED -> CLOSED
OPEN -> CANCELLED
IN_PROGRESS -> CANCELLED

All other transitions must be rejected.

The constitution must also require:
1. No secrets committed to Git.
2. Business rules must not be implemented only in the frontend.
3. API contracts must be documented.
4. Tests must be created before considering a feature complete.
5. AI-generated code must be reviewed by a human.
6. Meaningful AI mistakes must be recorded in docs/ai-review.md.
7. Every important AI prompt must be preserved in .specstory/history and summarized in docs/prompt-history.md.

Do not implement application code. Only create/update the project constitution and related governance artifacts.
```

---

## Context

The user requested creation of a formal project constitution to establish engineering standards, architectural principles, and governance rules for the Support Ticket Management System. This is a comprehensive governance document that will guide all development decisions.

The project is using:
- Spec-Driven Development (SDD) methodology
- AI-assisted development with Kiro
- Full-stack architecture (Java backend, React frontend)
- Test-driven approach with property-based testing

Key constraints:
- Strict state machine enforcement for ticket status transitions
- Backend must own all business rules (frontend is presentation only)
- Security-first approach (no secrets in Git)
- Mandatory human review of all AI-generated code
- Comprehensive documentation requirements

---

## AI Response

Created three governance artifacts:

### 1. PROJECT_CONSTITUTION.md

Comprehensive constitution with 15 sections:

**Section 1-2: Purpose and Methodology**
- Established Spec-Driven Development as mandatory methodology
- Defined three workflow types: Requirements-First, Design-First, Bugfix
- Required test-first development approach

**Section 3: Technology Stack**
- Java 21 LTS with Spring Boot 3.x
- Gradle 8.x for build management
- PostgreSQL 15+ for production, H2 2.x for tests
- React 18+, Next.js 14+, TypeScript 5+ for frontend
- JUnit 5, jqwik, Spring Boot Test for testing
- Flyway 9.x for schema migrations

**Section 4: Architectural Principles**
- Defined strict layered architecture:
  ```
  Controller (REST API)
    ↓
  Service (Business Logic)
    ↓
  Repository (Data Access)
    ↓
  Database (Persistence)
  ```
- Backend responsibilities: business rules, validation, state machine, auth, persistence
- Frontend responsibilities: UI rendering, UX validation, state management, API communication
- DTO-based API contracts (never expose entities directly)
- Comprehensive API documentation requirements

**Section 5: Critical Business Rules**
- **Ticket State Machine** (strictly enforced):
  - Allowed States: OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED
  - Allowed Transitions:
    1. OPEN → IN_PROGRESS (start work)
    2. IN_PROGRESS → RESOLVED (complete work)
    3. RESOLVED → CLOSED (verify and finalize)
    4. OPEN → CANCELLED (cancel before work)
    5. IN_PROGRESS → CANCELLED (cancel during work)
  - Terminal States: CLOSED, CANCELLED (no outbound transitions)
  - All other transitions rejected with HTTP 422
  - Implementation: TicketStateMachine component with allowlist validation
  - Testing: Property-based tests required

- **Backend Validation**:
  - Jakarta Bean Validation (JSR-380) required
  - Annotations: @NotNull, @NotBlank, @Size, @Pattern, @Valid
  - HTTP 400 for validation failures with field-level error details

**Section 6: Error Handling Standards**
- Consistent JSON error response structure
- HTTP status code mappings:
  - 200 OK (successful GET/PATCH/PUT)
  - 201 Created (successful POST)
  - 204 No Content (successful DELETE)
  - 400 Bad Request (validation failure)
  - 404 Not Found (resource not found)
  - 422 Unprocessable Entity (business rule violation, invalid state transition)
  - 500 Internal Server Error (unexpected errors)
- GlobalExceptionHandler with @RestControllerAdvice
- Never expose stack traces to clients

**Section 7: Data Persistence**
- Environment-specific database configuration via Spring Profiles
- Flyway for all schema changes (version-controlled migrations)
- Migration file naming: `V{version}__{description}.sql`
- JPA/Hibernate standards:
  - @Enumerated(EnumType.STRING) for enums (never ordinal)
  - @GeneratedValue(strategy = GenerationType.IDENTITY)
  - Audit timestamps with @CreatedDate, @LastModifiedDate

**Section 8: Security and Secret Management**
- **CRITICAL**: Secrets MUST NEVER be committed to Git
- Environment variables for all secrets
- .env files for local development (in .gitignore)
- Secret management services for production
- Pre-commit hooks to scan for secrets

**Section 9: Testing Standards**
- Code NOT production-ready without tests
- Test types:
  - Unit tests (service methods, validators)
  - Integration tests (REST endpoints with success/error cases)
  - Property-based tests (business logic, ≥100 iterations)
  - Repository tests (queries, migrations)
- H2 in-memory for fast integration tests
- Test organization: unit/, integration/, property/, repository/
- Property test format:
  ```java
  // Feature: feature-name, Property N: property-title
  @Property(tries = 100)
  void propertyTest() { ... }
  ```

**Section 10: AI-Assisted Development**
- AI tools allowed: GitHub Copilot, Cursor, ChatGPT, Claude, etc.
- **Mandatory human review** for ALL AI-generated code
- Review criteria: correctness, safety, performance, maintainability, test coverage, compliance
- AI mistake documentation in docs/ai-review.md
- Important prompt preservation in .specstory/history/

**Section 11: Documentation Requirements**
- Required docs: README.md, DEVELOPMENT.md, PROJECT_CONSTITUTION.md, API docs
- AI review log: docs/ai-review.md
- Prompt history: docs/prompt-history.md
- JavaDoc for all public APIs
- Complete specs in .kiro/specs/{feature-name}/

**Section 12-15: Version Control, Build, Amendments, Enforcement**
- Git workflow (main, develop, feature/, bugfix/)
- Commit message format
- Pull request checklist
- Gradle build standards
- Constitutional amendment process
- Enforcement and consequences of violations

### 2. docs/ai-review.md

Created template for documenting AI mistakes:

**Structure**:
- Review entry template with fields:
  - AI Tool used
  - Task description
  - Issue encountered
  - Impact if not caught
  - Resolution applied
  - Lesson learned
- Guidelines for what to document (logic errors, security issues, architecture violations)
- Guidelines for what NOT to document (syntax errors, formatting)
- Statistics tracking (reviews, issues, severity)
- Common pattern identification

**Purpose**: 
- Track AI assistant reliability
- Identify recurring mistake patterns
- Improve prompts based on documented issues
- Training resource for team

### 3. Updated docs/prompt-history.md

Added comprehensive entry for this prompt:

**Entry Includes**:
- Full prompt text
- Context and rationale
- Actions taken
- Artifacts created
- Key constitutional requirements
- State machine definition
- Outcome summary

---

## Deliverables

### Files Created
1. **PROJECT_CONSTITUTION.md** (1000+ lines)
   - 15 comprehensive sections
   - Appendices with state machine diagram and quick reference
   - Enforceable standards for all contributors

2. **docs/ai-review.md**
   - Template for AI mistake documentation
   - Review guidelines and statistics tracking
   - Ready for first entries

### Files Updated
3. **docs/prompt-history.md**
   - Added Prompt 8 entry
   - Documented constitution creation
   - Linked to this detailed history file

---

## Key Decisions

### 1. Gradle vs Maven
**Decision**: Constitution specifies Gradle
**Rationale**: Project currently uses Maven (pom.xml exists), but constitution establishes Gradle as standard
**Impact**: Future migration to Gradle required, or constitution amendment needed
**Note**: This is a discrepancy that should be addressed

### 2. State Machine Implementation
**Decision**: Allowlist approach (explicitly define valid transitions, reject all others)
**Rationale**: More secure than blocklist (default deny)
**Component**: TicketStateMachine in service layer
**Validation**: Before ANY status change is persisted
**Error**: HTTP 422 (Unprocessable Entity)

### 3. Testing Philosophy
**Decision**: Dual testing approach (example-based + property-based)
**Rationale**: Example tests for specific cases, property tests for universal behaviors
**Minimum**: ≥100 iterations for all property tests
**Coverage**: State machine, validation, invariants, round-trip

### 4. Frontend Role
**Decision**: Frontend is presentation layer only
**Prohibited**: Business logic, state transitions, validation (as sole enforcement)
**Allowed**: UX validation, state management, API communication
**Enforcement**: Backend MUST validate ALL requests

### 5. AI Governance
**Decision**: Human review mandatory, mistake documentation required
**Process**: AI generates → Human reviews → Test → Document issues → Approve/Reject
**Documentation**: docs/ai-review.md for mistakes, .specstory/history/ for prompts
**Rationale**: AI assistance accelerates development but requires oversight

---

## Constitutional Highlights

### Absolute Rules (Non-Negotiable)

1. **Security**:
   - ❌ Secrets in Git (never, critical violation)
   - ✅ Environment variables for all secrets
   - ✅ .gitignore for .env files

2. **Architecture**:
   - ❌ Business logic in frontend (major violation)
   - ✅ Backend owns all business rules
   - ✅ Strict layered architecture

3. **Testing**:
   - ❌ Shipping untested code (major violation)
   - ✅ Tests before feature completion
   - ✅ Property tests ≥100 iterations

4. **State Machine**:
   - ❌ Invalid transitions (must be rejected)
   - ✅ Only 5 allowed transitions
   - ✅ Terminal states enforced

5. **AI Development**:
   - ❌ Unreviewed AI code in production (major violation)
   - ✅ Mandatory human review
   - ✅ Document meaningful mistakes

### Golden Rule
"When AI generates code that violates this constitution, reject it and regenerate. The constitution is supreme."

---

## Technical Specifications

### Ticket State Machine

**States**:
1. OPEN (initial)
2. IN_PROGRESS (active work)
3. RESOLVED (work complete)
4. CLOSED (terminal, finalized)
5. CANCELLED (terminal, abandoned)

**Allowed Transitions Matrix**:
| From | To | Allowed | Use Case |
|------|----|---------| ---------|
| OPEN | IN_PROGRESS | ✅ | Agent starts work |
| OPEN | CANCELLED | ✅ | Ticket invalid/duplicate |
| IN_PROGRESS | RESOLVED | ✅ | Work completed |
| IN_PROGRESS | CANCELLED | ✅ | Work abandoned |
| RESOLVED | CLOSED | ✅ | Verified and finalized |
| CLOSED | * | ❌ | Terminal state |
| CANCELLED | * | ❌ | Terminal state |
| * | OPEN | ❌ | Cannot return to initial |
| OPEN | RESOLVED | ❌ | Must go through IN_PROGRESS |
| OPEN | CLOSED | ❌ | Must go through RESOLVED |
| IN_PROGRESS | CLOSED | ❌ | Must go through RESOLVED |
| RESOLVED | IN_PROGRESS | ❌ | Cannot revert |
| RESOLVED | OPEN | ❌ | Cannot revert |
| RESOLVED | CANCELLED | ❌ | Use CLOSED instead |

**Implementation Requirements**:
```java
public class TicketStateMachine {
    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
        TicketStatus.OPEN, Set.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
        TicketStatus.IN_PROGRESS, Set.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED),
        TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED)
    );
    
    public boolean isValidTransition(TicketStatus from, TicketStatus to) {
        return ALLOWED_TRANSITIONS
            .getOrDefault(from, Set.of())
            .contains(to);
    }
}
```

### Error Response Schemas

**Standard Error**:
```json
{
  "status": 422,
  "message": "Cannot transition from CLOSED to IN_PROGRESS",
  "timestamp": "2026-09-23T10:00:00Z",
  "path": "/api/tickets/1/status"
}
```

**Validation Error**:
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-09-23T10:00:00Z",
  "path": "/api/tickets",
  "errors": [
    { "field": "title", "message": "must not be blank" },
    { "field": "priority", "message": "must be one of: LOW, MEDIUM, HIGH, CRITICAL" }
  ]
}
```

---

## Impact Assessment

### Immediate Impact
- ✅ Clear governance structure established
- ✅ Engineering standards documented
- ✅ AI assistance guidelines in place
- ✅ Security requirements explicit
- ✅ Testing standards defined

### Future Impact
- 📋 All new code must comply with constitution
- 📋 Existing code should be refactored to comply (where violations exist)
- 📋 PR reviews will reference constitutional requirements
- 📋 AI prompts will include constitutional constraints
- 📋 Onboarding materials will reference constitution

### Potential Issues
1. **Maven vs Gradle**: Constitution specifies Gradle, but project uses Maven
   - **Resolution Needed**: Migrate to Gradle OR amend constitution to allow Maven
2. **Existing Code Compliance**: Ensure current code follows constitutional rules
   - **Action**: Audit current codebase against constitution
3. **State Machine Implementation**: Verify TicketStateMachine follows specification
   - **Action**: Review implementation, add property tests

---

## Next Steps

### Immediate Actions
1. ✅ Constitution created and documented
2. ✅ AI review log template created
3. ✅ Prompt history updated
4. 📋 Review existing code for constitutional compliance
5. 📋 Address Maven/Gradle discrepancy

### Documentation Tasks
1. 📋 Create DEVELOPMENT.md with developer guide
2. 📋 Create API documentation (OpenAPI/Swagger)
3. 📋 Document state machine in design specs
4. 📋 Add architecture diagrams to DEVELOPMENT.md

### Code Tasks
1. 📋 Audit TicketStateMachine implementation
2. 📋 Add state machine property tests (≥100 iterations)
3. 📋 Verify error handling consistency
4. 📋 Add secret scanning pre-commit hook

### Governance Tasks
1. 📋 Establish PR review process referencing constitution
2. 📋 Create constitutional compliance checklist
3. 📋 Schedule constitutional review (quarterly?)
4. 📋 Train team on constitutional requirements

---

## Lessons Learned

### About Constitution Creation
- Comprehensive governance documents provide clear decision-making framework
- Explicit state machine definition prevents ambiguity in implementation
- Security requirements must be enforceable (not just suggestions)
- AI governance is critical when using AI-assisted development

### About Spec-Driven Development
- Constitution complements SDD by establishing HOW specs are implemented
- Constitutional requirements flow into design documents
- Testing standards ensure specs are verifiable

### About AI-Assisted Development
- Human oversight is essential for AI-generated code
- Documenting AI mistakes creates valuable learning resource
- Clear prompts with constitutional constraints improve AI output

---

## References

### Related Documents
- `.kiro/specs/support-ticket-management/` - Feature spec
- `.kiro/specs/migrate-to-h2-database/` - Database migration spec
- `docs/prompt-history.md` - Complete prompt history
- `README.md` - Project overview

### Constitutional Sections
- Section 5.1: Ticket State Machine (detailed specification)
- Section 8.1: Secret Management (critical security requirements)
- Section 9: Testing Standards (comprehensive testing approach)
- Section 10: AI-Assisted Development (governance framework)

### External Resources
- Jakarta Bean Validation (JSR-380) specification
- Spring Boot documentation
- jqwik property-based testing framework
- Spec-Driven Development methodology

---

**Prompt Completed**: 2026-09-23  
**Artifacts Created**: 3 (1 new file, 2 updated files)  
**Lines Added**: ~1,200+  
**Constitutional Sections**: 15  
**State Transitions Defined**: 5 allowed, all others forbidden  
**Compliance Requirements**: 7 mandatory rules

---

## Appendix: Quick Reference

**When implementing features, always ask**:
1. Does this follow the layered architecture?
2. Are business rules in the backend?
3. Is validation on the backend?
4. Are invalid state transitions rejected?
5. Are tests written?
6. Is AI-generated code reviewed?
7. Are secrets excluded from Git?

**Constitutional Violations to Avoid**:
- ❌ Implementing business logic in frontend
- ❌ Committing secrets to Git
- ❌ Shipping untested code
- ❌ Allowing invalid state transitions
- ❌ Exposing entities directly via API
- ❌ Using unreviewed AI-generated code
- ❌ Inconsistent error responses

**Constitutional Compliance Checklist**:
- ✅ Backend validates all inputs
- ✅ State machine enforces 5 transitions
- ✅ DTOs for all API contracts
- ✅ Tests written (unit, integration, property)
- ✅ Secrets in environment variables
- ✅ Error responses follow standard schema
- ✅ AI code reviewed by human
- ✅ Meaningful AI mistakes documented
- ✅ Important prompts preserved

---

**End of Prompt History Document**
