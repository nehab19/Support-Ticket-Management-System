# Project Constitution: Support Ticket Management System

**Version**: 1.0  
**Last Updated**: 2026-09-23  
**Status**: Active

---

## 1. Purpose and Scope

This document establishes the foundational engineering principles, architectural decisions, and governance rules for the Support Ticket Management System. All contributors—human and AI—must adhere to these principles.

### 1.1 Project Mission

Build a robust, maintainable, and testable support ticket management system using Spec-Driven Development methodology, with clear separation of concerns, strict business rule enforcement, and comprehensive validation.

### 1.2 Governance Authority

This constitution is the supreme technical authority for this project. Any code, design, or process that violates these principles must be rejected or refactored.

---

## 2. Development Methodology

### 2.1 Spec-Driven Development (SDD)

**Requirement**: All features MUST follow the Spec-Driven Development workflow.

**Process**:
1. **Requirements Definition**: Document user stories and acceptance criteria using EARS patterns
2. **Design Creation**: Define architecture, components, data models, and correctness properties
3. **Task Breakdown**: Create hierarchical, traceable implementation tasks
4. **Implementation**: Execute tasks with test-first approach
5. **Verification**: Validate against requirements through automated tests

**Artifact Storage**: All specs reside in `.kiro/specs/{feature-name}/`

**Workflow Selection**:
- **Requirements-First**: Use when user needs drive the solution
- **Design-First**: Use when technical architecture is primary driver
- **Bugfix**: Use for fixing incorrect behavior with reproduction tests

### 2.2 Test-Driven Development

**Requirement**: Tests MUST be written before or during feature implementation.

**Rule**: A feature is NOT complete until:
- All acceptance criteria have corresponding tests
- All tests pass
- Property-based tests run with ≥100 iterations (where applicable)
- Integration tests validate end-to-end behavior

---

## 3. Technology Stack

### 3.1 Backend Technology Standards

| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| **Java** | 21 LTS | Primary backend language | Latest LTS with modern language features |
| **Spring Boot** | 3.x | Application framework | Industry standard, comprehensive ecosystem |
| **Gradle** | 8.x | Build tool | Modern, flexible, better performance than Maven |
| **PostgreSQL** | 15+ | Production database | Robust, ACID-compliant, excellent JSON support |
| **H2** | 2.x | Test database | In-memory, fast, JPA-compatible |
| **Flyway** | 9.x | Schema migration | Version-controlled database changes |

**Non-Negotiable**: 
- Java 21 features (records, pattern matching, virtual threads) are encouraged
- Spring Boot conventions must be followed
- Gradle must be used for all build processes

### 3.2 Frontend Technology Standards

| Technology | Version | Purpose | Rationale |
|------------|---------|---------|-----------|
| **React** | 18+ | UI framework | Component-based, extensive ecosystem |
| **Next.js** | 14+ | React framework | SSR, routing, API routes, TypeScript support |
| **TypeScript** | 5+ | Type safety | Catch errors at compile time, better IDE support |

**Non-Negotiable**:
- All frontend code must be TypeScript (no plain JavaScript)
- Frontend MUST NOT implement business rules (backend-enforced only)
- Frontend MUST validate user input for UX, but CANNOT trust client-side validation alone

### 3.3 Testing Technology Standards

| Technology | Purpose | When to Use |
|------------|---------|-------------|
| **JUnit 5** | Unit testing | Testing individual components in isolation |
| **jqwik** | Property-based testing | Testing universal properties across input ranges |
| **Spring Boot Test** | Integration testing | Testing full application context with database |
| **TestContainers** | Containerized testing | Testing against real PostgreSQL (optional) |
| **MockMvc** | API testing | Testing REST endpoints without HTTP server |
| **Jest** | Frontend unit testing | Testing React components and utilities |
| **React Testing Library** | Frontend integration testing | Testing component behavior and user interactions |

---

## 4. Architectural Principles

### 4.1 Layered Architecture

**Requirement**: Backend MUST follow strict layered architecture.

```
┌─────────────────────────────────────┐
│   Controller Layer (REST API)       │  ← HTTP requests/responses only
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│   Service Layer (Business Logic)    │  ← Business rules, orchestration
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│   Repository Layer (Data Access)    │  ← JPA, database queries
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│   Database Layer (PostgreSQL/H2)    │  ← Persistence
└─────────────────────────────────────┘
```

**Rules**:
1. Controllers MUST NOT contain business logic
2. Services MUST NOT handle HTTP concerns (status codes, headers)
3. Repositories MUST NOT contain business logic
4. Each layer may only depend on the layer directly below it
5. Cross-cutting concerns (logging, security, transactions) use Spring AOP

### 4.2 Separation of Concerns

**Backend Responsibilities**:
- Business rule enforcement
- Data validation
- State machine enforcement
- Authorization and authentication
- Data persistence
- Error handling with meaningful messages

**Frontend Responsibilities**:
- User interface rendering
- Client-side UX validation (for immediate feedback only)
- State management for UI
- API communication
- User input handling

**Non-Negotiable**: Business rules MUST be enforced by the backend. Frontend validation is for user experience only and MUST NOT be relied upon for security or data integrity.

### 4.3 API Contract Design

**Requirement**: All API contracts MUST use DTOs (Data Transfer Objects).

**DTO Principles**:
1. **Request DTOs**: Separate from domain entities
   - Example: `CreateTicketRequest`, `UpdateTicketRequest`
   - Contain only fields needed for the operation
   - Annotated with validation constraints

2. **Response DTOs**: Separate from domain entities
   - Example: `TicketSummaryResponse`, `TicketDetailResponse`
   - Expose only necessary information (no internal IDs, sensitive data)
   - Consistent structure across all endpoints

3. **Never expose domain entities directly** through REST endpoints

**API Documentation Requirement**: All REST endpoints MUST be documented with:
- HTTP method and path
- Request body schema (if applicable)
- Query parameters (if applicable)
- Success response schema and status code
- Error response schemas and status codes
- Example requests and responses

**Location**: API documentation resides in:
- Design documents: `.kiro/specs/{feature-name}/design.md`
- OpenAPI/Swagger specification (optional): `docs/api/openapi.yaml`

---

## 5. Critical Business Rules

### 5.1 Ticket State Machine

**Requirement**: The ticket state machine MUST be strictly enforced by the backend.

**Allowed States**:
- `OPEN`: Initial state when ticket is created
- `IN_PROGRESS`: Ticket is being actively worked on
- `RESOLVED`: Work is complete, awaiting verification
- `CLOSED`: Ticket is finalized and archived
- `CANCELLED`: Ticket is abandoned without completion

**Allowed Transitions** (Exhaustive):
```
OPEN          → IN_PROGRESS   (Start work)
IN_PROGRESS   → RESOLVED      (Complete work)
RESOLVED      → CLOSED        (Verify and finalize)
OPEN          → CANCELLED     (Cancel before work starts)
IN_PROGRESS   → CANCELLED     (Cancel during work)
```

**Forbidden Transitions** (All others):
- Any transition FROM `CLOSED` (terminal state)
- Any transition FROM `CANCELLED` (terminal state)
- Any transition TO `OPEN` (cannot return to initial state)
- `OPEN` → `RESOLVED` (must go through IN_PROGRESS)
- `OPEN` → `CLOSED` (must go through RESOLVED)
- `IN_PROGRESS` → `CLOSED` (must go through RESOLVED)
- `RESOLVED` → `IN_PROGRESS` (cannot revert)
- `RESOLVED` → `OPEN` (cannot revert)
- `RESOLVED` → `CANCELLED` (use CLOSED instead)

**Enforcement Mechanism**:
- Component: `TicketStateMachine` (Service layer)
- Validation: Before ANY status change is persisted
- Error Response: HTTP 422 (Unprocessable Entity) with message indicating invalid transition
- Implementation: Allowlist approach (explicitly define valid transitions, reject all others)

**Non-Negotiable**: 
- Frontend MUST NOT implement transition logic independently
- Frontend MUST request transitions via API and handle rejection gracefully
- All transition requests MUST go through `TicketStateMachine` validation
- State machine logic MUST be covered by property-based tests

### 5.2 Backend Validation

**Requirement**: All input validation MUST occur on the backend.

**Validation Rules**:
1. **Required Fields**: All mandatory fields must be present and non-null
2. **String Constraints**: Length limits, format validation, non-blank
3. **Enum Validation**: Only valid enum values accepted
4. **Business Rule Validation**: State transitions, referential integrity
5. **Authorization**: User has permission to perform operation

**Validation Framework**: Jakarta Bean Validation (JSR-380)
- Use annotations: `@NotNull`, `@NotBlank`, `@Size`, `@Pattern`, `@Valid`
- Custom validators for complex business rules

**Error Response Format**: HTTP 400 (Bad Request) with field-level errors
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-09-23T10:00:00Z",
  "path": "/api/tickets",
  "errors": [
    {
      "field": "title",
      "message": "Title must not be blank"
    },
    {
      "field": "priority",
      "message": "Priority must be one of: LOW, MEDIUM, HIGH, CRITICAL"
    }
  ]
}
```

---

## 6. Error Handling Standards

### 6.1 Consistent Error Responses

**Requirement**: ALL error responses MUST follow a consistent structure.

**Standard Error Response Schema**:
```json
{
  "status": 400,              // HTTP status code
  "message": "Human-readable error description",
  "timestamp": "ISO-8601 timestamp",
  "path": "Request path"
}
```

**Validation Error Response Schema** (HTTP 400):
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "ISO-8601 timestamp",
  "path": "Request path",
  "errors": [
    {
      "field": "fieldName",
      "message": "Error description"
    }
  ]
}
```

### 6.2 HTTP Status Code Mapping

**Requirement**: Use appropriate HTTP status codes consistently.

| Status Code | Usage | Example |
|-------------|-------|---------|
| **200 OK** | Successful GET, PATCH, PUT | Retrieve ticket details |
| **201 Created** | Successful POST creating resource | Create new ticket |
| **204 No Content** | Successful DELETE | Delete ticket |
| **400 Bad Request** | Validation failure | Missing required field, invalid format |
| **401 Unauthorized** | Authentication required | No auth token provided |
| **403 Forbidden** | Authorization failure | User lacks permission |
| **404 Not Found** | Resource does not exist | Ticket ID not found |
| **409 Conflict** | Resource conflict | Duplicate unique constraint |
| **422 Unprocessable Entity** | Business rule violation | Invalid state transition |
| **500 Internal Server Error** | Unexpected server error | Uncaught exception |

### 6.3 Exception Handling

**Component**: `GlobalExceptionHandler` (annotated with `@RestControllerAdvice`)

**Responsibilities**:
1. Catch all exceptions thrown by controllers and services
2. Map exceptions to appropriate HTTP status codes
3. Format error responses consistently
4. Log errors without exposing sensitive information
5. Never expose stack traces to clients (return generic message for unexpected errors)

**Custom Exceptions**:
- `TicketNotFoundException` → 404
- `InvalidStatusTransitionException` → 422
- `DuplicateTicketException` → 409
- Validation exceptions → 400

---

## 7. Data Persistence Standards

### 7.1 Database Environment Configuration

**Requirement**: Use environment-specific database configuration.

| Environment | Database | Purpose |
|-------------|----------|---------|
| **Production** | PostgreSQL 15+ | Live data, ACID compliance |
| **Development** | PostgreSQL or H2 | Local development |
| **Test** | H2 (in-memory) | Fast unit/integration tests |

**Configuration Approach**: Spring Profiles
- `application.properties` (common settings)
- `application-prod.properties` (PostgreSQL connection)
- `application-test.properties` (H2 in-memory)

### 7.2 Schema Management

**Requirement**: All database schema changes MUST be version-controlled with Flyway.

**Migration File Rules**:
1. **Location**: `src/main/resources/db/migration/`
2. **Naming**: `V{version}__{description}.sql`
   - Example: `V1__create_ticket_table.sql`
   - Example: `V2__add_priority_column.sql`
3. **Immutability**: Once applied, migration files MUST NOT be modified
4. **Reversibility**: Consider rollback strategy for each migration
5. **Idempotency**: Migrations should be safe to re-run (use `IF NOT EXISTS` where applicable)

**Non-Negotiable**: 
- Never manually modify production database schema
- All schema changes must go through Flyway migrations
- Test migrations on H2 and PostgreSQL before production deployment

### 7.3 JPA/Hibernate Standards

**Entity Design Rules**:
1. Use `@Entity` for domain objects persisted to database
2. Use `@Table` to specify table names explicitly
3. Use `@Column` to specify column constraints (nullable, length, unique)
4. Use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-increment IDs
5. Use `@CreatedDate` and `@LastModifiedDate` for audit timestamps
6. Use `@Enumerated(EnumType.STRING)` for enums (never use ordinal)

**Repository Design Rules**:
1. Extend `JpaRepository<Entity, ID>`
2. Use method name query derivation where possible
3. Use `@Query` for complex queries
4. Never put business logic in repositories

---

## 8. Security and Secret Management

### 8.1 Secret Management

**CRITICAL REQUIREMENT**: Secrets MUST NEVER be committed to Git.

**Prohibited in Git**:
- Database passwords
- API keys
- OAuth tokens
- Private keys
- Encryption keys
- Any credential or sensitive configuration value

**Secret Storage Approach**:
1. **Environment Variables**: Primary mechanism for secrets
   - Example: `DATABASE_PASSWORD`, `JWT_SECRET`
2. **`.env` files**: For local development (MUST be in `.gitignore`)
3. **Secret Management Services**: For production (AWS Secrets Manager, Vault, etc.)

**Configuration Pattern**:
```properties
# application.properties (COMMITTED)
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

# .env (NOT COMMITTED, in .gitignore)
DATABASE_URL=jdbc:postgresql://localhost:5432/tickets
DATABASE_USERNAME=dev_user
DATABASE_PASSWORD=dev_password_123
```

**Verification**: 
- `.gitignore` MUST include: `*.env`, `.env.*`, `secrets/`, `credentials/`
- Pre-commit hooks should scan for potential secrets

### 8.2 Authentication and Authorization

**Future Requirement**: Authentication/authorization to be implemented in later phase.

**Planned Approach**:
- Spring Security with JWT tokens
- Role-based access control (RBAC)
- Roles: `USER`, `AGENT`, `ADMIN`

---

## 9. Testing Standards

### 9.1 Testing Requirements

**Requirement**: Code is NOT production-ready without comprehensive tests.

**Minimum Testing Coverage**:
1. **Unit Tests**: All service methods, utility functions, validators
2. **Integration Tests**: All REST endpoints with success and error cases
3. **Property-Based Tests**: Business logic with universal properties (≥100 iterations)
4. **Database Tests**: Repository queries, migrations

**Test Organization**:
```
src/test/java/com/example/supportticket/
  ├── unit/
  │   ├── service/
  │   │   └── TicketServiceTest.java
  │   └── validator/
  │       └── TicketValidatorTest.java
  ├── integration/
  │   └── controller/
  │       └── TicketControllerIntegrationTest.java
  ├── property/
  │   ├── TicketCreationPropertyTest.java
  │   └── StateMachinePropertyTest.java
  └── repository/
      └── TicketRepositoryTest.java
```

### 9.2 Property-Based Testing

**When to Use**: Apply property-based testing to:
- State machine transitions (verify all valid transitions succeed, all invalid fail)
- Data validation (verify all invalid inputs rejected)
- Invariants (verify required fields always set correctly)
- Round-trip operations (serialize → deserialize produces equivalent object)

**Configuration**:
```java
// Feature: support-ticket-management, Property 1: Ticket creation invariants
@Property(tries = 100)
void ticketCreationInvariants(@ForAll @StringLength(min=1, max=255) String title) {
    CreateTicketRequest request = new CreateTicketRequest(title, "Description", null);
    Ticket ticket = ticketService.createTicket(request);
    
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
    assertThat(ticket.getId()).isNotNull();
    assertThat(ticket.getCreatedAt()).isNotNull();
}
```

**Non-Negotiable**:
- All property tests MUST run ≥100 iterations
- Each property test MUST include feature name and property number in comment
- Property tests MUST reference requirements they validate

### 9.3 Integration Testing with H2

**Requirement**: Use H2 for fast, isolated integration tests.

**H2 Test Configuration**:
```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=none
spring.flyway.enabled=true
```

**Test Class Setup**:
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional  // Rollback after each test
class TicketControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Test
    void createTicket_validRequest_returns201() throws Exception {
        // Test implementation
    }
}
```

**Rules**:
1. Each test runs in a transaction that rolls back (isolated state)
2. Flyway migrations run on H2 startup (schema consistency)
3. Use `@Sql` annotations to load test data if needed
4. Never rely on test execution order

---

## 10. AI-Assisted Development

### 10.1 AI Usage Policy

**Allowed**: Use AI assistants (GitHub Copilot, Cursor, ChatGPT, Claude, etc.) to:
- Generate boilerplate code
- Suggest implementations
- Write tests
- Debug errors
- Create documentation
- Refactor code

**Required**: Human review and approval for ALL AI-generated code.

### 10.2 AI Code Review Process

**Requirement**: Every piece of AI-generated code MUST be reviewed by a human before merge.

**Review Criteria**:
1. **Correctness**: Does it satisfy the requirements?
2. **Safety**: Are there security vulnerabilities?
3. **Performance**: Are there obvious inefficiencies?
4. **Maintainability**: Is it readable and well-structured?
5. **Test Coverage**: Are there adequate tests?
6. **Compliance**: Does it follow this constitution?

**Process**:
1. AI generates code
2. Human reviews code line-by-line
3. Human tests code manually and via automated tests
4. If issues found, record in `docs/ai-review.md`
5. Human approves or requests AI regeneration
6. Only approved code proceeds to commit

### 10.3 AI Mistake Documentation

**Requirement**: Meaningful AI mistakes MUST be documented in `docs/ai-review.md`.

**What to Document**:
- **Significant Errors**: Logic errors, security issues, violations of requirements
- **Pattern Failures**: Repeated mistakes indicating AI misunderstanding
- **Architecture Violations**: Code that breaks layered architecture or separation of concerns
- **Test Failures**: AI-generated tests that don't properly validate behavior

**What NOT to Document**:
- Minor syntax errors (typos, missing semicolons)
- Formatting issues (whitespace, indentation)
- Easily caught and fixed issues

**Documentation Format** (in `docs/ai-review.md`):
```markdown
## 2026-09-23: State Machine Validation Omitted

**AI Tool**: Cursor
**Task**: Implement ticket status transition endpoint
**Issue**: AI generated endpoint that directly set ticket status without validating state machine rules.
**Impact**: Would have allowed invalid transitions (e.g., CLOSED → OPEN)
**Resolution**: Added `TicketStateMachine.validateTransition()` call before persisting status change.
**Lesson**: AI may skip critical business rule validation. Always verify.
```

### 10.4 Prompt History Preservation

**Requirement**: Important AI prompts MUST be preserved and documented.

**Storage Locations**:
1. **`.specstory/history/{number}-{description}.md`**: Full prompt text and AI responses
2. **`docs/prompt-history.md`**: Summary of all prompts with links to full history

**What Qualifies as "Important"**:
- Prompts that generated significant code (>100 lines)
- Prompts that defined architecture or design
- Prompts that resolved complex bugs
- Prompts that established patterns or conventions
- Prompts that created specs or documentation

**Prompt History Entry Format** (in `docs/prompt-history.md`):
```markdown
## 2026-09-23: Create Ticket State Machine

**Prompt Number**: 008  
**File**: `.specstory/history/008-ticket-state-machine.md`  
**Tool**: Cursor  
**Purpose**: Implement strict state machine for ticket status transitions  
**Outcome**: Created `TicketStateMachine` component with allowlist validation  
**Files Changed**: `TicketStateMachine.java`, `TicketService.java`, tests  
**Notes**: Required refinement to handle terminal state validation
```

---

## 11. Documentation Requirements

### 11.1 Required Documentation

**Minimum Documentation**:
1. **README.md**: Project overview, setup instructions, how to run
2. **DEVELOPMENT.md**: Developer guide, architecture, conventions
3. **PROJECT_CONSTITUTION.md**: This document
4. **API Documentation**: REST endpoint reference (in design specs or OpenAPI)
5. **AI Review Log**: `docs/ai-review.md`
6. **Prompt History**: `docs/prompt-history.md`

### 11.2 Code Documentation

**JavaDoc Requirements**:
- All public classes, interfaces, and methods MUST have JavaDoc
- Document purpose, parameters, return values, thrown exceptions
- Include examples for complex logic

**Inline Comments**:
- Explain "why", not "what" (code should be self-explanatory for "what")
- Comment complex algorithms and business rules
- Comment workarounds or non-obvious solutions

### 11.3 Spec Documentation

**Requirement**: Every feature MUST have a complete spec in `.kiro/specs/{feature-name}/`.

**Spec Components**:
1. **requirements.md**: User stories, acceptance criteria (EARS patterns)
2. **design.md**: Architecture, components, data models, correctness properties
3. **tasks.md**: Implementation tasks with requirement traceability
4. **.config.kiro**: Metadata (specId, workflowType, specType)

---

## 12. Version Control and Collaboration

### 12.1 Git Workflow

**Branching Strategy**:
- `main`: Production-ready code, protected
- `develop`: Integration branch for features
- `feature/{feature-name}`: Feature branches
- `bugfix/{bug-name}`: Bug fix branches

**Commit Message Format**:
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`

**Example**:
```
feat(ticket): implement state machine validation

- Add TicketStateMachine component with allowlist
- Validate all status transitions before persistence
- Return 422 for invalid transitions

Validates: Requirements 3.1, 3.2, 3.3
Closes: #42
```

### 12.2 Pull Request Requirements

**PR Checklist**:
- [ ] Code follows constitution and architectural principles
- [ ] All tests pass (unit, integration, property-based)
- [ ] New tests added for new functionality
- [ ] AI-generated code has been reviewed by human
- [ ] Documentation updated (if applicable)
- [ ] No secrets committed
- [ ] Database migrations added (if applicable)
- [ ] API contracts documented (if applicable)

---

## 13. Build and Deployment

### 13.1 Build Standards

**Gradle Configuration Requirements**:
- Java 21 compatibility
- Spring Boot plugin configured
- Flyway plugin for migrations
- Test execution for all test types
- Code coverage reporting (JaCoCo recommended)

**Build Commands**:
```bash
# Build application
./gradlew build

# Run tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run property-based tests
./gradlew propertyTest

# Package application
./gradlew bootJar
```

### 13.2 Environment Configuration

**Required Environment Variables** (Production):
- `DATABASE_URL`: PostgreSQL connection string
- `DATABASE_USERNAME`: Database user
- `DATABASE_PASSWORD`: Database password
- `JWT_SECRET`: JWT signing key (future)
- `SPRING_PROFILES_ACTIVE`: Active Spring profile (prod, dev, test)

---

## 14. Constitutional Amendments

### 14.1 Amendment Process

**Requirement**: Changes to this constitution require explicit approval.

**Process**:
1. Propose amendment in pull request
2. Document rationale for change
3. Review impact on existing code
4. Update version number and date
5. Require explicit approval from project lead or team consensus

### 14.2 Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0 | 2026-09-23 | Initial constitution | Project Team |

---

## 15. Enforcement

**Responsibility**: All contributors are responsible for upholding this constitution.

**Consequences of Violations**:
- **Minor Violations**: Request for correction in code review
- **Major Violations**: PR rejection, refactoring required
- **Critical Violations** (secrets, security): Immediate rollback, incident review

**Non-Compliance Examples**:
- Committing secrets to Git → Critical violation
- Implementing business logic only in frontend → Major violation
- Missing tests for new feature → Major violation
- Inconsistent error response format → Minor violation
- Missing JavaDoc on public method → Minor violation

---

## Appendix A: State Machine Diagram

```
                     ┌──────────────┐
                     │     OPEN     │ (Initial State)
                     └──────┬───────┘
                            │
                ┌───────────┴───────────┐
                │                       │
                ▼                       ▼
      ┌─────────────────┐     ┌────────────────┐
      │  IN_PROGRESS    │     │   CANCELLED    │ (Terminal)
      └────────┬────────┘     └────────────────┘
               │
   ┌───────────┴───────────┐
   │                       │
   ▼                       ▼
┌──────────┐      ┌────────────────┐
│ RESOLVED │      │   CANCELLED    │ (Terminal)
└────┬─────┘      └────────────────┘
     │
     ▼
┌──────────┐
│  CLOSED  │ (Terminal)
└──────────┘
```

**Allowed Transitions**:
1. OPEN → IN_PROGRESS
2. OPEN → CANCELLED
3. IN_PROGRESS → RESOLVED
4. IN_PROGRESS → CANCELLED
5. RESOLVED → CLOSED

**Terminal States** (no outbound transitions):
- CLOSED
- CANCELLED

---

## Appendix B: Quick Reference

**When in doubt, remember**:
- ✅ Business rules in backend (always)
- ✅ Validate on backend (always)
- ✅ Test before shipping (always)
- ✅ Review AI code (always)
- ✅ Document important decisions (always)
- ❌ Secrets in Git (never)
- ❌ Business logic in frontend (never)
- ❌ Untested code in production (never)
- ❌ Manual schema changes (never)
- ❌ Inconsistent error responses (never)

**Golden Rule**: When AI generates code that violates this constitution, reject it and regenerate. The constitution is supreme.

---

**End of Constitution**
