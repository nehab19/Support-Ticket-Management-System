# Design-First Workflow Guide

## Overview

The Design-First Workflow is a systematic approach for developing features when the technical architecture and component design are the primary drivers. This workflow progresses from **Design → Requirements → Tasks → Implementation**, ensuring that technical decisions are documented first, then user-facing behaviors are derived from those design choices.

**When to use this workflow:**
- Technical architecture is clearly defined before user behaviors
- Building infrastructure, libraries, or APIs with known interfaces
- Migrating or refactoring existing systems with established patterns
- Internal tools where technical structure drives requirements
- Performance optimization or architectural improvements

**When NOT to use this workflow:**
- User stories and needs are the primary driver (use Requirements-First instead)
- Fixing a specific bug (use Bugfix workflow instead)
- User experience should drive technical decisions (use Requirements-First instead)
- Unclear technical direction (research first, then choose workflow)

---

## Workflow Phases

The Design-First workflow consists of five sequential phases with user review checkpoints:

```
┌────────────────────────────────────────────────────────────┐
│ Phase 1: Design Creation                                   │
│ Input: Technical architecture ideas, component sketches    │
│ Output: design.md (initial, without properties)            │
│ Checkpoint: ✓ User review required                         │
└──────────────────────┬─────────────────────────────────────┘
                       │
┌──────────────────────▼─────────────────────────────────────┐
│ Phase 2: Requirements Derivation                           │
│ Input: Initial design.md                                   │
│ Output: requirements.md                                    │
│ Checkpoint: ✓ User approval required                       │
└──────────────────────┬─────────────────────────────────────┘
                       │
┌──────────────────────▼─────────────────────────────────────┐
│ Phase 3: Complete Design with Properties                   │
│ Input: Approved requirements.md                            │
│ Output: design.md (complete with properties)               │
│ Checkpoint: ✓ User review required                         │
└──────────────────────┬─────────────────────────────────────┘
                       │
┌──────────────────────▼─────────────────────────────────────┐
│ Phase 4: Task Breakdown                                    │
│ Input: Complete design.md                                  │
│ Output: tasks.md                                           │
│ Checkpoint: ✓ User review required                         │
└──────────────────────┬─────────────────────────────────────┘
                       │
┌──────────────────────▼─────────────────────────────────────┐
│ Phase 5: Implementation                                    │
│ Input: tasks.md                                            │
│ Output: Working, tested code                               │
│ Verification: All tests pass, all tasks completed          │
└────────────────────────────────────────────────────────────┘
```

---

## Phase 1: Design Creation

### Goal

Create a technical design specifying architecture, components, interfaces, and data models based on known technical requirements or constraints.

### Input

- Technical architecture ideas or diagrams
- Component interface specifications
- Performance or scalability requirements
- Integration constraints (APIs, databases, protocols)
- Technology stack decisions

### Process

#### Step 1: Document Technical Context

Begin with a clear overview of the technical goals:
- What problem does this architecture solve?
- What are the key technical constraints?
- What technology choices have been made and why?
- What are the performance, scalability, or reliability goals?

**Example (database migration):**
```markdown
## Overview

### Goals

- Migrate the support ticket system from PostgreSQL to H2 embedded database
- Reduce operational complexity by eliminating external database dependency
- Maintain data integrity and all existing functionality
- Enable easier local development and testing

### Scope

This design covers:
- Database engine replacement (PostgreSQL → H2)
- Data migration strategy
- Configuration changes
- Connection pool updates

This design does NOT cover:
- Changes to application logic or APIs
- UI modifications
- New features
```

#### Step 2: Create Architecture Diagram

Document the high-level system structure showing:
- Component layers and their responsibilities
- Data flow between components
- Technology choices at each layer
- Integration points and boundaries

**Use ASCII art or Mermaid for diagrams:**

```
Current Architecture:
┌──────────┐
│   API    │  Spring Boot REST
└────┬─────┘
     │ JDBC
┌────▼─────────┐
│ PostgreSQL   │  External database
│ (Port 5432)  │
└──────────────┘

Target Architecture:
┌──────────┐
│   API    │  Spring Boot REST
└────┬─────┘
     │ JDBC
┌────▼─────────┐
│   H2 DB      │  Embedded file-based
│ (./data/*.db)│
└──────────────┘
```

Include rationale for architectural decisions:
```markdown
### Architecture Decision: Embedded vs. Server Mode

**Decision:** Use H2 in embedded file-based mode

**Rationale:**
- Simplifies deployment (no separate database process)
- Reduces memory footprint
- Sufficient for single-instance application
- Maintains data persistence across restarts

**Tradeoffs:**
- No concurrent access from multiple JVMs
- Slightly reduced query performance vs. PostgreSQL
```

#### Step 3: Define Components and Interfaces

Document all major components with their responsibilities and interfaces.

**For APIs/Services:**
```markdown
### Data Access Layer

**Component:** JPA Repositories

**Interfaces:**
- `TicketRepository extends JpaRepository<Ticket, Long>`
- `CommentRepository extends JpaRepository<Comment, Long>`

**Changes from current:**
- No interface changes required
- JPA abstraction isolates database implementation
- Query methods remain identical
```

**For REST endpoints (if designing new APIs):**
```markdown
### REST Endpoints

**POST /api/tickets**
- Request: `CreateTicketRequest { title, description, priority? }`
- Response: `TicketDetailResponse` (HTTP 201)
- Errors: HTTP 400 (validation), HTTP 500 (server error)

**GET /api/tickets**
- Query params: `status?, keyword?`
- Response: `List<TicketSummaryResponse>` (HTTP 200)
```

**For Libraries/Utilities:**
```markdown
### StringUtils Module

**Functions:**
- `normalize(input: string): string` — Remove extra whitespace, lowercase
- `truncate(input: string, maxLength: number): string` — Truncate with ellipsis
- `sanitize(input: string): string` — Remove dangerous characters

**Dependencies:** None (pure functions)
```

#### Step 4: Define Data Models

Specify database schemas, entity definitions, file formats, or data structures.

**For database schemas:**
```markdown
### Ticket Entity

| Field | Type | Constraints | Changes |
|---|---|---|---|
| id | Long | Primary Key, Auto-increment | None |
| title | String | NOT NULL, Max 255 | None |
| description | String | NOT NULL | None |
| status | Enum | NOT NULL, Default OPEN | None |
| priority | Enum | NOT NULL, Default MEDIUM | None |
| createdAt | Timestamp | NOT NULL | None |
| updatedAt | Timestamp | NOT NULL | None |

**H2-Specific Considerations:**
- Auto-increment uses `IDENTITY` instead of `SERIAL`
- Timestamp columns use `TIMESTAMP` type
- Enum types stored as `VARCHAR` with `@Enumerated(STRING)`

**Indexes:**
- Primary: `id`
- Composite: `(status, createdAt DESC)`
```

**For file formats or APIs:**
```markdown
### Configuration File Schema

```yaml
database:
  type: h2 | postgresql
  url: string
  username: string
  password: string
  pool:
    maxSize: number (default: 10)
    timeout: number (default: 30000)
```
```

#### Step 5: Document Migration or Integration Strategy

If the design involves migrating from an existing system or integrating with external components, document the strategy:

```markdown
## Migration Strategy

### Phase 1: Add H2 Dependencies

- Add H2 driver to pom.xml
- Keep PostgreSQL driver temporarily for testing

### Phase 2: Update Configuration

- Modify `application.properties` with H2 connection string
- Update Flyway migration dialect
- Configure H2 compatibility mode if needed

### Phase 3: Test with H2

- Run full test suite against H2
- Verify all Flyway migrations execute
- Verify all queries return expected results

### Phase 4: Data Migration (if needed)

- Export data from PostgreSQL
- Transform to H2-compatible SQL
- Import into H2 database file
```

#### Step 6: Identify Risks and Constraints

Document technical risks, limitations, or constraints:

```markdown
## Risks and Constraints

### Technical Constraints

- **Single JVM limitation**: H2 embedded mode supports only one JVM connection
- **SQL dialect differences**: Some PostgreSQL-specific syntax requires changes
- **Performance**: H2 may be slower for complex queries (acceptable tradeoff)

### Mitigation Strategies

- Use JPA abstraction to minimize dialect-specific code
- Run comprehensive test suite to catch compatibility issues
- Profile performance on representative workload
```

#### Step 7: User Review Checkpoint

Present initial `design.md` to technical stakeholders:
- Verify architecture satisfies technical goals
- Confirm component interfaces are appropriate
- Review technology choices and tradeoffs
- Clarify any design decisions

**Do not proceed to Phase 2 until design is reviewed.**

### Output

An initial `design.md` document containing:
- Overview with goals, scope, and technical context
- Architecture section with diagrams and decision rationale
- Components and interfaces
- Data models with schemas or formats
- Migration/integration strategy (if applicable)
- Risks and constraints

**Note:** This initial design does NOT yet include:
- Correctness properties (added in Phase 3)
- Error handling details (added in Phase 3)
- Testing strategy (added in Phase 3)

---

## Phase 2: Requirements Derivation

### Goal

Extract user-facing behaviors and acceptance criteria from the technical design using EARS patterns and INCOSE quality standards.

### Input

Initial `design.md` (from Phase 1)

### Process

#### Step 1: Identify User-Facing Behaviors

Review the design and identify all behaviors that affect users, APIs, or external systems:

**From architecture:**
- System startup/shutdown behavior
- Resource management (connections, files)
- Performance characteristics

**From component interfaces:**
- API request/response behavior
- Error conditions
- Validation rules

**From data models:**
- Data persistence guarantees
- Query result ordering
- Constraint enforcement

**Example (database migration):**

From design: "H2 embedded file-based database stored in `./data/`"

User-facing behaviors:
- Application must connect to H2 on startup
- Data must persist across application restarts
- Queries must return same results as with PostgreSQL

#### Step 2: Convert Behaviors into User Stories

For each behavior, write a user story:

```
As a [role], I want [capability], so that [benefit].
```

**Guidelines:**
- Role may be developer, operator, end-user, or "the system"
- Focus on the value provided by the technical design
- Keep stories atomic (one capability per story)

**Example:**
```markdown
### Requirement 1: Database Connection

**User Story:** As a developer, I want the application to connect to an H2 embedded database on startup, so that I can run the system without external database dependencies.
```

```markdown
### Requirement 2: Data Persistence

**User Story:** As an operator, I want ticket data to persist across application restarts, so that no data is lost when the system is stopped or restarted.
```

#### Step 3: Write Acceptance Criteria Using EARS Patterns

For each user story, derive specific, testable acceptance criteria from the design.

**The Six EARS Patterns:**

##### 1. Ubiquitous (Always-Active Behavior)

**Format:** `THE {system_name} SHALL {capability}`

**Use for:** Behaviors with no triggering event, always active

**Example (from design):**
```
THE Database_Engine SHALL persist all created tickets and comments to disk.
```

##### 2. Event-Driven (Response to Trigger)

**Format:** `WHEN {trigger} [, {optional precondition}] THEN THE {system_name} SHALL {response}`

**Use for:** Specific events that trigger system responses

**Example (from design):**
```
WHEN the application starts, THEN THE Database_Connection_Manager SHALL establish a connection to the H2 database file at ./data/supporttickets.mv.db.
```

##### 3. State-Driven (Behavior While in State)

**Format:** `WHILE {system state} [, {optional condition}] THE {system_name} SHALL {behavior}`

**Use for:** Behavior depends on system being in a specific state

**Example (from design):**
```
WHILE the application is running, THE Database_Engine SHALL maintain an exclusive lock on the database file.
```

##### 4. Unwanted Event (Error Conditions)

**Format:** `IF {undesired condition/event} [, WHEN {trigger}] THEN THE {system_name} SHALL {response}`

**Use for:** Error conditions derived from design constraints

**Example (from design):**
```
IF the database file is corrupted, WHEN the application starts, THEN THE System SHALL log an error message and fail to start with exit code 1.
```

##### 5. Optional Feature (Conditional Capabilities)

**Format:** `WHERE {feature is included} [, {condition}] THE {system_name} SHALL {capability}`

**Use for:** Optional features or default behaviors from design

**Example (from design):**
```
WHERE no database URL is specified in configuration, THE System SHALL default to ./data/supporttickets for the H2 database file location.
```

##### 6. Complex (Combination of Patterns)

Combine patterns for complex requirements.

**Example:**
```
WHILE the database connection pool is at maximum capacity,
WHEN a new request arrives,
THEN THE Connection_Manager SHALL wait up to 30 seconds for an available connection,
IF no connection becomes available within 30 seconds,
THEN THE System SHALL reject the request with HTTP 503.
```

#### Step 4: Build Domain Glossary

Extract technical terms from the design and define them:

**Rules:**
- Every system name in EARS patterns (THE **Database_Engine**, THE **System**) MUST be defined
- Define technical components from architecture diagram
- Define domain entities from data models
- Include configuration parameters and their meanings

**Example:**
```markdown
## Glossary

- **Database_Engine**: The H2 embedded database engine responsible for data storage and retrieval.
- **Database_Connection_Manager**: The Spring Boot component managing JDBC connections to H2.
- **Ticket**: A record representing a customer support issue stored in the database.
- **Flyway_Migration**: Database schema version control scripts executed on startup.
- **Connection_Pool**: A pool of reusable JDBC connections maintained by HikariCP.
```

#### Step 5: Map Design Sections to Requirements

Create traceability from design to requirements:

**For each component in design:**
- Identify what requirements describe its behavior
- Ensure all public interfaces have requirements
- Verify error conditions are captured

**Traceability notes** (optional in requirements.md):
```markdown
#### Acceptance Criteria

1. WHEN the application starts, THEN THE Database_Connection_Manager SHALL establish a connection to H2.
   - _Design: Components → Data Access Layer_
```

#### Step 6: Review for INCOSE Quality Rules

Verify each requirement against the six INCOSE quality rules:

**Rule 1: Clarity** — Unambiguous, precise language
- ✓ Good: "THE System SHALL connect to H2 embedded database at ./data/supporttickets.mv.db"
- ✗ Bad: "The system should use H2 somehow"

**Rule 2: Testability** — Can be verified through testing, inspection, analysis, or demonstration
- ✓ Good: "THE Database_Engine SHALL persist all tickets to disk" (testable: create ticket, restart, verify exists)
- ✗ Bad: "The database should be reliable"

**Rule 3: Completeness** — All necessary information including error cases
- ✓ Good: Include both success (connection established) and failure (corrupted file) cases
- ✗ Bad: Only specifying successful connection

**Rule 4: Positive Statements** — State what system SHALL do, not what it SHALL NOT do
- ✓ Good: "THE Connection_Manager SHALL enforce a maximum of 10 concurrent connections"
- ✗ Bad: "THE Connection_Manager SHALL NOT allow more than 10 connections"

**Rule 5: Consistency** — No contradictions with other requirements
- Verify: Cross-check all requirements and design for conflicts

**Rule 6: Atomic** — Each requirement addresses a single concern
- ✓ Good: Separate requirements for "connection establishment" and "data persistence"
- ✗ Bad: Combining multiple behaviors in one requirement

#### Step 7: User Approval Checkpoint

Present `requirements.md` to stakeholders for review:
- Verify requirements accurately reflect design behaviors
- Confirm all design components have corresponding requirements
- Clarify any ambiguous terms or conditions
- Add missing requirements discovered during review

**Do not proceed to Phase 3 until requirements are approved.**

### Output

A complete `requirements.md` document containing:
- Introduction paragraph
- Glossary with all domain and technical terms from design
- Requirements section with:
  - Numbered requirements (1, 2, 3...)
  - User story for each requirement
  - Acceptance criteria using EARS patterns
  - All criteria validated against INCOSE rules
  - Traceability to design sections (optional)

---

## Phase 3: Complete Design with Properties

### Goal

Return to the design document and complete it with correctness properties, error handling, and testing strategy based on the now-defined requirements.

### Input

- Initial `design.md` (from Phase 1)
- Approved `requirements.md` (from Phase 2)

### Process

#### Step 1: Assess Property-Based Testing Applicability

**Before writing correctness properties**, determine if PBT is appropriate for this feature.

**PBT works best for:**
- Pure functions with clear input/output behavior
- Universal properties holding across all inputs
- Parsers, serializers, data transformations, algorithms
- Business logic with invariants
- Large or infinite input spaces

**PBT is NOT appropriate for:**
- Infrastructure as Code (IaC) — Terraform, CDK, CloudFormation
- UI rendering and layout — React components, HTML templates
- Simple CRUD with no transformation — Direct database reads/writes
- Configuration validation — Schema checks
- Side-effect-only operations — Sending emails, logging
- External service integration with no logic

**Decision:**
- **If PBT applies**: Proceed to steps 2-4
- **If PBT does NOT apply**: Skip to step 5, document alternative testing approach

**Example (database migration):**
```markdown
## PBT Applicability Assessment

**Decision:** PBT NOT applicable for this feature

**Rationale:**
- Database migration is infrastructure configuration
- No algorithmic logic or data transformations
- Testing focuses on integration (does H2 work correctly?)
- Success measured by "system starts and queries work"

**Alternative Testing Approach:**
- Integration tests with H2 in-memory and file modes
- Flyway migration verification tests
- Query result comparison tests (PostgreSQL vs. H2)
```

#### Step 2: Run Prework Tool (PBT Applicable)

**Purpose:** Analyze each acceptance criterion for testability classification.

**Process:**

For each acceptance criterion, determine:
1. **Does this test YOUR code or external services?**
   - External → INTEGRATION
2. **Does behavior vary meaningfully with input?**
   - No → EXAMPLE/SMOKE
3. **Would 100 iterations find more bugs than 2-3 examples?**
   - No → EXAMPLE/INTEGRATION
4. **Cost-effective to run 100+ times?**
   - No (high cost) → INTEGRATION or mock-based PROPERTY

**Output format:**
```
X.Y Criteria Name
  Thoughts: [Step-by-step reasoning about testability]
  Classification: PROPERTY | EXAMPLE | EDGE_CASE | INTEGRATION | SMOKE
  Test Strategy: [How to test, what varies, what's verified]
```

**Example:**
```
2.3 WHEN parsing a configuration file, THE Parser SHALL extract all database parameters
  Thoughts: This tests parsing logic (our code). Input varies (different YAML structures).
    Universal property: ALL valid YAML configs with required fields parse successfully.
    Can generate many variations (different orderings, whitespace, optional fields).
  Classification: PROPERTY
  Test Strategy: Generate varied valid YAML configs, verify all parse correctly.

2.5 WHEN the application starts, THE Database_Connection_Manager SHALL establish connection
  Thoughts: This tests integration with H2 (external). Not pure logic, more integration.
    Success/failure is binary (connection works or doesn't).
  Classification: INTEGRATION
  Test Strategy: Unit test with H2 in-memory, verify connection established.
```

#### Step 3: Perform Property Reflection (PBT Applicable)

**Purpose:** Eliminate redundant properties by identifying logical implications.

**Process:**

1. List all properties identified as PROPERTY classification in prework
2. Identify redundant properties:
   - **Logically implied:** One property implies another
   - **Combinable:** Multiple properties can merge into comprehensive property
3. Mark redundant properties for removal
4. Ensure each remaining property provides unique validation

**Examples of redundancy:**

- "Configuration parser extracts URL field" + "Configuration parser extracts username field"
  - **Decision:** Combine into "Configuration parser extracts all required fields"

- "String normalizer removes leading whitespace" + "String normalizer removes trailing whitespace"
  - **Decision:** Combine into "String normalizer removes leading and trailing whitespace"

#### Step 4: Write Correctness Properties Section (PBT Applicable)

**Format for each property:**

```markdown
### Property N: [Title]

*For any/all* [universal quantification statement]

**Validates: Requirements X.Y, X.Z**
```

**Common Property Patterns:**

**Pattern 1: Invariants** — Properties that remain constant

```markdown
### Property 1: Configuration Parsing Completeness

*For any* valid configuration file containing all required fields, the parser SHALL extract database URL, username, password, and pool settings.

**Validates: Requirements 2.2, 2.3**
```

**Pattern 2: Round-Trip** — Operation followed by inverse returns to original

```markdown
### Property 3: Data Migration Round-Trip

*For any* ticket entity, exporting to SQL and re-importing SHALL produce an equivalent entity with matching ID, title, description, status, and timestamps.

**Validates: Requirements 4.1**
```

**CRITICAL:** Always include round-trip properties for parsers and serializers.

**Pattern 3: Idempotence** — f(x) = f(f(x))

```markdown
### Property 5: Migration Script Idempotence

*For any* database state, running Flyway migrations twice SHALL produce the same schema as running them once.

**Validates: Requirements 3.2**
```

**Pattern 4: Metamorphic** — Relationship between variants

```markdown
### Property 7: Query Result Equivalence

*For any* query against the ticket table, results from H2 SHALL match results from PostgreSQL (same IDs, order, and field values).

**Validates: Requirements 5.1, 5.2**
```

**Pattern 5: Error Conditions**

```markdown
### Property 9: Invalid Configuration Rejection

*For any* configuration file missing required database fields, the parser SHALL reject with a clear error message indicating which fields are missing.

**Validates: Requirements 2.6**
```

#### Step 5: Write Error Handling Section

Define error conditions, response codes, and error formats:

**For APIs:**
```markdown
## Error Handling

| Condition | HTTP Status | Error Response |
|---|---|---|
| Database connection failure | 500 Internal Server Error | Generic error message, details in logs |
| Invalid configuration file | 500 Internal Server Error | Error message indicating which field is invalid |
| Database file corrupted | 500 Internal Server Error | Error message, application exits |
| Connection pool exhausted | 503 Service Unavailable | Retry-After header with timeout |

### ErrorResponse Structure

```json
{
  "message": "Human-readable error message",
  "timestamp": "ISO-8601 timestamp",
  "path": "Request path"
}
```
```

**For libraries or CLI tools:**
```markdown
## Error Handling

### Exceptions

| Exception | Thrown When | Message Format |
|---|---|---|
| `ConfigurationException` | Invalid config file | "Invalid configuration: {details}" |
| `DatabaseConnectionException` | Connection fails | "Failed to connect to database: {reason}" |
| `MigrationException` | Flyway migration fails | "Migration failed: {migration_version}: {error}" |

### Exit Codes (CLI)

| Code | Meaning |
|---|---|
| 0 | Success |
| 1 | Configuration error |
| 2 | Database connection error |
| 3 | Migration failure |
```

#### Step 6: Write Testing Strategy Section

Document the testing approach, including both property tests (if applicable) and integration tests:

**If PBT applies:**
```markdown
## Testing Strategy

### Property-Based Testing

**Library:** jqwik (Java)
**Configuration:** Minimum 100 iterations per property (`@Property(tries = 100)`)

**Property Test Coverage:**
- Property 1: Configuration parsing completeness
- Property 3: Data migration round-trip
- Property 5: Migration script idempotence

**Test Tag Format:**
```java
// Feature: migrate-to-h2-database, Property 1: Configuration parsing completeness
@Property(tries = 100)
void configurationParsingCompleteness(@ForAll ConfigGenerator config) {
    // Test implementation
}
```

### Integration Testing

**Framework:** JUnit 5 + Spring Boot Test
**Database:** H2 in-memory and file modes

**Integration Test Coverage:**
- Application startup with H2 configuration
- All Flyway migrations execute successfully
- CRUD operations work identically to PostgreSQL
- Connection pool behavior under load
```

**If PBT does NOT apply:**
```markdown
## Testing Strategy

### Integration Testing

**Framework:** JUnit 5 + Spring Boot Test
**Database:** H2 in-memory and file modes

**Test Coverage:**
- **Startup tests**: Application starts with H2 configuration, database file created
- **Migration tests**: All Flyway migrations execute successfully without errors
- **CRUD tests**: Create, read, update, delete operations work correctly
- **Query tests**: Filtering, sorting, and pagination return expected results
- **Persistence tests**: Data survives application restart

### Comparison Testing

**Approach:** Run same test suite against PostgreSQL and H2 to verify equivalent behavior

**Test Process:**
1. Run full test suite with PostgreSQL configuration
2. Run full test suite with H2 configuration
3. Compare results: all tests should pass in both configurations
4. Verify query result equivalence for key operations

### Performance Testing

**Approach:** Measure query performance with representative dataset

**Metrics:**
- Create ticket latency
- List tickets (1000 records) latency
- Keyword search latency
- Status transition latency

**Acceptance:** H2 performance within 20% of PostgreSQL for all operations
```

#### Step 7: User Review Checkpoint

Present completed `design.md` to stakeholders and technical reviewers:
- Verify correctness properties (if applicable) capture critical behaviors
- Review error handling completeness
- Confirm testing strategy is appropriate
- Clarify any design decisions

**Do not proceed to Phase 4 until design is approved.**

### Output

A complete `design.md` document containing:
- Overview with goals and scope (from Phase 1)
- Architecture section with diagrams (from Phase 1)
- Components and interfaces (from Phase 1)
- Data models with schemas (from Phase 1)
- Migration/integration strategy (from Phase 1, if applicable)
- **Correctness properties (NEW, if PBT applicable)**
- **Error handling specifications (NEW)**
- **Testing strategy (NEW)**
- Risks and constraints (from Phase 1)

---

## Phase 4: Task Breakdown

### Goal

Decompose implementation into hierarchical, traceable tasks with requirement references.

### Input

Complete `design.md` (from Phase 3)

### Process

#### Step 1: Identify Top-Level Tasks

Group implementation work into major areas based on design components:

```markdown
## Tasks

- [ ] 1. Update project dependencies
- [ ] 2. Configure H2 database connection
- [ ] 3. Update Flyway migrations for H2 compatibility
- [ ] 4. Implement data migration scripts (if needed)
- [ ] 5. Write integration tests
- [ ] 6. Write property-based tests (if applicable)
- [ ] 7. Verify query equivalence
- [ ] 8. Update documentation
```

#### Step 2: Decompose into Subtasks

Break each top-level task into specific, actionable subtasks:

```markdown
- [ ] 2. Configure H2 database connection
  - [ ] 2.1 Update application.properties with H2 connection URL
    - Set spring.datasource.url to jdbc:h2:file:./data/supporttickets
    - Set spring.datasource.driver-class-name to org.h2.Driver
    - _Requirements: 1.1, 1.2_
  - [ ] 2.2 Configure H2 compatibility mode
    - Add MODE=PostgreSQL to connection string
    - _Requirements: 1.2_
  - [ ] 2.3 Update connection pool settings
    - Configure HikariCP for H2-specific parameters
    - _Requirements: 1.3_
```

#### Step 3: Annotate with Requirement Traceability

Each task MUST reference the requirements it implements:

```markdown
- [ ] 3.2 Verify Flyway migrations execute successfully
  - Run application startup
  - Check logs for migration success
  - Verify database schema matches expected state
  - _Requirements: 3.1, 3.2, 3.3_
```

#### Step 4: Annotate Property Test Tasks (If Applicable)

Tasks that implement property tests MUST reference property numbers:

```markdown
- [ ] 6.1 Write ConfigurationParsingPropertyTest
  - Generate varied valid configuration files
  - Verify all required fields extracted
  - Run 100 iterations
  - _Requirements: 2.2, 2.3_
  - _Property: 1_
```

#### Step 5: Identify Dependencies and Ordering

Note dependencies between tasks and recommended execution order:

```markdown
- [ ] 3. Update Flyway migrations for H2 compatibility
  - [ ] 3.1 Review existing migrations for PostgreSQL-specific syntax
    - Depends on: 2.1 (need H2 connection to test)
    - _Requirements: 3.1_
  - [ ] 3.2 Update migrations with H2-compatible SQL
    - Replace SERIAL with IDENTITY for auto-increment
    - Update timestamp syntax
    - Depends on: 3.1
    - _Requirements: 3.1, 3.2_
```

#### Step 6: Add Verification Tasks

Include explicit verification and testing tasks:

```markdown
- [ ] 7. Verify query equivalence
  - [ ] 7.1 Create comparison test suite
    - _Requirements: 5.1, 5.2_
  - [ ] 7.2 Run tests against PostgreSQL baseline
    - Capture expected results
    - _Requirements: 5.1_
  - [ ] 7.3 Run tests against H2 configuration
    - Compare results to PostgreSQL baseline
    - _Requirements: 5.1, 5.2_
  - [ ] 7.4 Document any differences and assess impact
    - _Requirements: 5.2_
```

#### Step 7: User Review Checkpoint

Present `tasks.md` to team for review:
- Verify task granularity is appropriate
- Confirm requirement coverage is complete
- Identify any missing tasks
- Adjust ordering based on dependencies

**Do not proceed to Phase 5 until tasks are approved.**

### Output

A complete `tasks.md` document containing:
- Overview of implementation approach
- Hierarchical task list with numbering (1, 1.1, 1.2...)
- Status markers ([ ] not started, [-] in progress, [x] completed)
- Requirement traceability annotations
- Property number annotations for property test tasks (if applicable)
- Dependencies and execution order
- Verification and testing tasks

---

## Phase 5: Implementation

### Goal

Execute all tasks to produce working, tested code that satisfies requirements and design.

### Input

`tasks.md` with defined tasks

### Process

#### Step 1: Set Up Development Environment

Ensure all tools and dependencies are ready:
- IDE configured (Cursor/Kiro/VS Code)
- Build tools installed (Maven, npm, etc.)
- Property-based testing library added to project (if applicable)
- Test framework configured

#### Step 2: Execute Tasks Sequentially

For each task:

1. **Update task status to in progress:**
   ```markdown
   - [-] 2.1 Update application.properties with H2 connection URL
   ```

2. **Implement the task:**
   - Write code according to design specifications
   - Follow project conventions and style
   - Add appropriate comments and documentation

3. **Write tests for the task:**
   - Integration tests for infrastructure changes
   - Property tests if annotated with property number
   - Verify tests pass

4. **Update task status to completed:**
   ```markdown
   - [x] 2.1 Update application.properties with H2 connection URL
   ```

#### Step 3: Write Property-Based Tests (If Applicable)

For each property test task:

1. **Set up test class:**
   ```java
   import net.jqwik.api.*;
   
   class ConfigurationParsingPropertyTest {
       // Test implementation
   }
   ```

2. **Add feature and property tag comment:**
   ```java
   // Feature: migrate-to-h2-database, Property 1: Configuration parsing completeness
   @Property(tries = 100)
   void configurationParsingCompleteness(/* ... */) {
       // Test implementation
   }
   ```

3. **Configure test iterations (≥100):**
   - jqwik: `@Property(tries = 100)`
   - Hypothesis: `@settings(max_examples=100)`
   - fast-check: `{ numRuns: 100 }`

4. **Write property assertion:**
   ```java
   @Property(tries = 100)
   void configurationParsingCompleteness(
       @ForAll @ConfigGenerator DatabaseConfig config
   ) {
       ConfigParser parser = new ConfigParser();
       ParsedConfig result = parser.parse(config.toYaml());
       
       // Property: ALL valid configs parse successfully and extract required fields
       assertThat(result.getUrl()).isNotNull();
       assertThat(result.getUsername()).isNotNull();
       assertThat(result.getPassword()).isNotNull();
       assertThat(result.getPoolSize()).isGreaterThan(0);
   }
   ```

5. **Run property test and verify:**
   ```bash
   mvn test -Dtest=ConfigurationParsingPropertyTest
   ```

   **Expected output:**
   ```
   ConfigurationParsingPropertyTest:configurationParsingCompleteness
     tries = 100                | OK
   ```

#### Step 4: Write Integration Tests

For each component or configuration change:
- Test integration with H2
- Test startup and initialization
- Test error conditions

```java
@SpringBootTest
class H2DatabaseIntegrationTest {
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Test
    void applicationStartsWithH2Database() {
        // Verify application context loads successfully
        assertThat(ticketRepository).isNotNull();
    }
    
    @Test
    void ticketPersistsAcrossRepositoryOperations() {
        Ticket ticket = new Ticket("Test", "Description", Priority.HIGH);
        Ticket saved = ticketRepository.save(ticket);
        
        Optional<Ticket> found = ticketRepository.findById(saved.getId());
        
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test");
    }
}
```

#### Step 5: Verify Requirement Traceability

For each requirement:
1. Find all tasks with `_Requirements: X.Y_`
2. Verify all referenced tasks are completed
3. Verify code implements the requirement
4. Verify tests validate the requirement

#### Step 6: Run Full Test Suite

Before completing implementation:

```bash
# Run all tests
mvn clean test

# Verify all property tests run ≥100 iterations (if applicable)
# Verify all integration tests pass
# Verify code coverage (optional)
```

#### Step 7: Final Verification Checklist

- [ ] All tasks marked `[x]` completed
- [ ] All property tests pass with ≥100 iterations (if applicable)
- [ ] All integration tests pass
- [ ] All requirements traced to implementation
- [ ] Error handling matches design specification
- [ ] Code follows project conventions
- [ ] Documentation updated (README, configuration guides, etc.)

### Output

- Working, tested code
- All tests passing
- Completed `tasks.md` (all tasks marked `[x]`)
- Implementation satisfies all requirements and design

---

## Checkpoints and Feedback Loops

### User Review Checkpoints

**After Phase 1 (Initial Design):**
- Review architecture with technical team
- Verify component interfaces are appropriate
- Review technology choices and tradeoffs
- **Decision:** Approve and proceed, or iterate on design

**After Phase 2 (Requirements):**
- Verify requirements accurately reflect design behaviors
- Confirm all design components have corresponding requirements
- Clarify any ambiguous terms
- **Decision:** Approve and proceed, or iterate on requirements

**After Phase 3 (Complete Design):**
- Review correctness properties (if applicable)
- Verify error handling completeness
- Confirm testing strategy is appropriate
- **Decision:** Approve and proceed, or iterate on design/requirements

**After Phase 4 (Tasks):**
- Review task breakdown with team
- Verify completeness and granularity
- Confirm ordering and dependencies
- **Decision:** Approve and proceed, or adjust tasks

### Iterative Refinement

**Returning to Design from Requirements:**

When to iterate:
- Requirements reveal missing design components
- Requirements conflict with design decisions
- Edge cases discovered during requirements derivation

Process:
1. Update affected sections in `design.md`
2. Return to Phase 2 to update requirements based on new design
3. Seek user approval again
4. Proceed with updated documents

**Returning to Requirements from Complete Design:**

When to iterate:
- Property analysis reveals missing requirements
- Error handling analysis reveals missing acceptance criteria
- Testing strategy reveals untested behaviors

Process:
1. Add/modify requirements in `requirements.md`
2. Update affected sections in `design.md`
3. Seek approval again
4. Proceed with updated documents

**Handling Ambiguities:**

When ambiguity is discovered:
1. Document the ambiguity clearly
2. Present options to stakeholders
3. Get explicit decision
4. Update spec documents accordingly
5. Continue from updated state

---

## When to Choose Design-First vs. Requirements-First

### Choose Design-First When:

1. **Technical architecture is the primary constraint**
   - Example: Migrating to a specific database engine
   - Example: Implementing a specific protocol or interface

2. **Building infrastructure or foundational components**
   - Example: Creating a logging library
   - Example: Building a configuration management system

3. **Technical performance or scalability drives decisions**
   - Example: Optimizing query performance
   - Example: Implementing caching layer

4. **Integrating with existing technical architecture**
   - Example: Adding a new microservice to existing ecosystem
   - Example: Implementing API gateway

5. **Internal tooling with clear technical requirements**
   - Example: Build pipeline automation
   - Example: Development environment setup

### Choose Requirements-First When:

1. **User needs drive design decisions**
   - Example: Building user-facing features
   - Example: Implementing business workflows

2. **Stakeholders can articulate behaviors before architecture**
   - Example: Features requested by product team
   - Example: Compliance requirements

3. **User experience is the primary concern**
   - Example: UI/UX improvements
   - Example: User journey optimization

4. **Multiple technical solutions exist**
   - Example: Feature could be implemented several ways
   - Example: Technology choices are flexible

### Hybrid Approach:

Some projects may benefit from a hybrid:
1. Start with high-level design (Phase 1)
2. Switch to requirements-first for user-facing aspects
3. Return to design-first for infrastructure components

**Example:**
- Use design-first for data migration architecture
- Use requirements-first for new API endpoints built on that architecture

---

## Common Patterns and Examples

### Pattern 1: Database Migration

**Situation:** Migrating from PostgreSQL to H2

**Process:**
1. **Phase 1:** Document current and target architectures, migration strategy
2. **Phase 2:** Derive requirements from design (connection establishment, data persistence, query equivalence)
3. **Phase 3:** Complete design with integration testing strategy (PBT likely not applicable)
4. **Phase 4:** Break down into tasks (dependencies, configuration, migrations, verification)
5. **Phase 5:** Execute migration, verify with comparison tests

**Key considerations:**
- PBT typically not applicable (infrastructure change)
- Focus on integration and comparison testing
- Explicit verification tasks critical

### Pattern 2: Library or Utility Development

**Situation:** Building a string manipulation utility library

**Process:**
1. **Phase 1:** Document API interfaces, function signatures, performance goals
2. **Phase 2:** Derive requirements from each function (normalize, truncate, sanitize behaviors)
3. **Phase 3:** Complete design with correctness properties (round-trip, idempotence, invariants)
4. **Phase 4:** Break down into tasks per function + property tests
5. **Phase 5:** Implement with property-based testing (PBT highly applicable)

**Key considerations:**
- PBT highly applicable (pure functions)
- Round-trip properties for encoding/decoding
- Idempotence for normalization functions

### Pattern 3: API Gateway Implementation

**Situation:** Implementing an API gateway with routing, auth, and rate limiting

**Process:**
1. **Phase 1:** Document architecture (layers, routing logic, middleware pipeline)
2. **Phase 2:** Derive requirements from design (routing rules, auth behaviors, rate limit policies)
3. **Phase 3:** Complete design with correctness properties (routing correctness, rate limit enforcement)
4. **Phase 4:** Break down into tasks (core routing, auth middleware, rate limiter, integration)
5. **Phase 5:** Implement with mixed testing (properties for routing logic, integration for middleware)

**Key considerations:**
- PBT applicable for routing logic (many route combinations)
- Integration tests for middleware pipeline
- Property tests for rate limiting algorithm

### Pattern 4: Performance Optimization

**Situation:** Optimizing query performance with caching and indexing

**Process:**
1. **Phase 1:** Document current vs. target performance, caching strategy, index design
2. **Phase 2:** Derive requirements from design (cache hit rate, query latency, consistency guarantees)
3. **Phase 3:** Complete design with performance properties and testing strategy
4. **Phase 4:** Break down into tasks (add indexes, implement cache, benchmark)
5. **Phase 5:** Implement and verify with performance tests

**Key considerations:**
- PBT may apply for cache correctness properties (cache hit = DB hit)
- Performance benchmarks with representative data
- Before/after comparison critical

### Pattern 5: Refactoring with Behavior Preservation

**Situation:** Extracting state machine into separate component

**Process:**
1. **Phase 1:** Document current architecture, target architecture, extraction plan
2. **Phase 2:** Derive requirements from design (same transition rules, same validation)
3. **Phase 3:** Complete design with properties capturing current behavior (behavior preservation)
4. **Phase 4:** Break down into tasks (extract component, update callers, verify)
5. **Phase 5:** Implement with property tests ensuring equivalence

**Key considerations:**
- Write properties capturing current behavior BEFORE refactoring
- Use properties to verify refactoring preserves behavior
- PBT excellent for behavior preservation verification

---

## IDE Integration Guidance

### Cursor/Kiro/VS Code Usage

#### Invoking Design-First Workflow

From chat interface:
```
Create a new spec for [feature name] using design-first workflow
```

**The subagent will:**
1. Create `.kiro/specs/{feature-name}/` directory
2. Generate `.config.kiro` with UUID, workflowType="design-first", specType="feature"
3. Guide you through Phase 1 (design creation)
4. Pause for user approval checkpoint
5. Continue through phases 2-5 with checkpoints

#### Navigating Spec Documents

**File tree navigation:**
```
.kiro/
└── specs/
    └── my-feature/
        ├── .config.kiro
        ├── design.md          ← Start here in Phase 1
        ├── requirements.md    ← Created in Phase 2
        └── tasks.md           ← Created in Phase 4
```

**Open all three in tabs** for easy cross-referencing during Phases 3-5.

**Use search (Cmd/Ctrl+F):**
- Find requirement: Search for "Requirement 1:"
- Find property: Search for "Property 5:"
- Find task: Search for "_Requirements: 1.2_"

### Running Tests from IDE

**Java/Maven:**
```bash
# Run all tests
mvn test

# Run integration tests only
mvn test -Dtest=*IntegrationTest

# Run property tests (if applicable)
mvn test -Dtest=*PropertyTest
```

**Python/pytest:**
```bash
# Run all tests
pytest

# Run integration tests
pytest tests/integration/

# Run property tests (if applicable)
pytest tests/properties/
```

**JavaScript/Jest:**
```bash
# Run all tests
npm test

# Run integration tests
npm test -- integration

# Run property tests (if applicable)
npm test -- property.test.js
```

### Task Tracking

**Manual updates in `tasks.md`:**
```markdown
- [ ] 2.1 Update configuration       ← Not started
- [-] 2.2 Update migrations          ← In progress
- [x] 2.3 Write integration tests    ← Completed
```

**Use IDE outline view** (if available) to navigate task hierarchy.

**Search for tasks by requirement:**
```
Search: "_Requirements: 2.2_"
Result: All tasks implementing requirement 2.2
```

---

## Quick Start Example

**Scenario:** Migrate support ticket system from PostgreSQL to H2 embedded database

### Phase 1: Design Creation (15 minutes)

```markdown
# Design Document — Migrate to H2 Database

## Overview

### Goals

- Replace PostgreSQL with H2 embedded database
- Reduce operational complexity
- Maintain all existing functionality

### Architecture

Current: Spring Boot → PostgreSQL (external)
Target: Spring Boot → H2 (embedded file-based)

## Components

### Data Access Layer

- JPA Repositories (no interface changes)
- Flyway migrations (update for H2 syntax)

## Data Models

Ticket and Comment entities (no schema changes)

## Risks

- H2 embedded mode = single JVM only
- SQL dialect differences require testing
```

**✓ User reviews**

### Phase 2: Requirements Derivation (15 minutes)

```markdown
## Glossary

- **Database_Engine**: H2 embedded database
- **Connection_Manager**: Spring Boot JDBC connection manager

## Requirements

### Requirement 1: Database Connection

**User Story:** As a developer, I want the application to connect to H2 on startup, so that I can run without external database.

#### Acceptance Criteria

1. WHEN the application starts, THEN THE Connection_Manager SHALL establish connection to H2 at ./data/supporttickets.mv.db.
2. THE Database_Engine SHALL persist all tickets and comments to disk.

### Requirement 2: Data Persistence

**User Story:** As an operator, I want data to persist across restarts, so that no data is lost.

#### Acceptance Criteria

1. WHEN the application restarts, THEN THE Database_Engine SHALL reload all previously saved data.
```

**✓ User approves**

### Phase 3: Complete Design with Properties (10 minutes)

```markdown
## PBT Applicability Assessment

**Decision:** PBT NOT applicable (infrastructure change)

**Alternative Testing:** Integration tests with H2

## Error Handling

| Condition | Response |
|---|---|
| Connection failure | HTTP 500, log error, application exits |
| Corrupted database | HTTP 500, log error, application exits |

## Testing Strategy

### Integration Testing

- Application startup with H2
- CRUD operations work correctly
- Data persists across restart
- Query results match PostgreSQL baseline
```

**✓ User reviews**

### Phase 4: Task Breakdown (10 minutes)

```markdown
## Tasks

- [ ] 1. Update dependencies
  - [ ] 1.1 Add H2 dependency to pom.xml
    - _Requirements: 1.1_

- [ ] 2. Configure H2 connection
  - [ ] 2.1 Update application.properties
    - _Requirements: 1.1, 1.2_

- [ ] 3. Update Flyway migrations
  - [ ] 3.1 Update migrations for H2 syntax
    - _Requirements: 1.1_

- [ ] 4. Write integration tests
  - [ ] 4.1 Test application startup
    - _Requirements: 1.1_
  - [ ] 4.2 Test data persistence
    - _Requirements: 2.1_
```

**✓ User approves**

### Phase 5: Implementation (30 minutes)

**Execute tasks, write tests, verify:**

```bash
mvn clean test
```

**✓ All tests pass, all tasks complete**

---

## Completion Checklist

Use this checklist to verify your spec is complete:

### Phase 1: Initial Design
- [ ] Overview with goals, scope, and technical context
- [ ] Architecture section with diagrams
- [ ] Components and interfaces documented
- [ ] Data models with schemas or formats
- [ ] Migration/integration strategy (if applicable)
- [ ] Risks and constraints identified
- [ ] User has reviewed initial design

### Phase 2: Requirements Document
- [ ] Introduction paragraph present
- [ ] Glossary includes all domain and technical terms from design
- [ ] All system names in EARS patterns defined in glossary
- [ ] Each requirement has user story
- [ ] All acceptance criteria use EARS patterns
- [ ] All criteria pass INCOSE rules (clarity, testability, completeness, positive, consistency, atomic)
- [ ] User has approved requirements

### Phase 3: Complete Design
- [ ] PBT applicability assessed
- [ ] If PBT applicable: Prework completed, reflection performed, correctness properties written
- [ ] If PBT not applicable: Alternative testing documented
- [ ] Each property references requirements (if applicable)
- [ ] Error handling section complete
- [ ] Testing strategy documented
- [ ] User has approved complete design

### Phase 4: Tasks Document
- [ ] Overview of implementation approach
- [ ] Hierarchical task numbering (1, 1.1, 1.2...)
- [ ] Each task references requirements (`_Requirements: X.Y_`)
- [ ] Property test tasks reference property numbers (if applicable)
- [ ] Dependencies and execution order identified
- [ ] User has approved tasks

### Phase 5: Implementation
- [ ] All tasks marked `[x]` completed
- [ ] All property tests pass with ≥100 iterations (if applicable)
- [ ] All integration tests pass
- [ ] Code matches design architecture
- [ ] Error responses match design specification
- [ ] All requirements traced to implementation
- [ ] Documentation updated

---

## Next Steps

1. **Read the full design document** (`.kiro/specs/spec-driven-development-workflow/design.md`) for detailed guidance on EARS patterns, INCOSE rules, and property patterns
2. **Review the requirements-first workflow guide** (`.kiro/docs/workflows/requirements-first-workflow.md`) for complementary perspective
3. **Review the migrate-to-h2-database spec** in `.kiro/specs/migrate-to-h2-database/` as a complete design-first example
4. **Start your first design-first spec:**
   - Identify a feature where technical architecture is clear
   - Invoke the design-first workflow subagent
   - Follow the phases with checkpoints
5. **Iterate and refine** based on user feedback at each phase

For questions or guidance on specific workflow phases, refer to the detailed sections in this document or the main design document.
