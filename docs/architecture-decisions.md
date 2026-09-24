# Architecture Decision Records (ADRs)

This document records significant architectural and design decisions made during the development of the Support Ticket Management System.

---

## ADR Format

Each ADR follows this structure:

- **Title**: Short descriptive name
- **Status**: Proposed | Accepted | Deprecated | Superseded
- **Date**: When decision was made
- **Context**: What forces are at play
- **Decision**: What we decided to do
- **Consequences**: Positive and negative outcomes

---

## ADR-001: Use Separate Status Transition Endpoint

**Status**: Accepted  
**Date**: 2026-09-23

### Context

Ticket status transitions are governed by a strict state machine with only 5 allowed transitions out of 25 possible (from, to) pairs. This is fundamentally different from field updates where any valid value is acceptable.

We need to decide whether to:
1. Use a single `PATCH /tickets/{id}` endpoint for both field updates and status changes
2. Create a separate endpoint specifically for status transitions

### Decision

We will create a separate endpoint: `PATCH /tickets/{id}/status`

### Consequences

**Positive**:
- State machine enforcement is explicit and obvious
- Different HTTP status codes: 400 for field validation, 422 for business rule violations
- Easier to audit status transitions separately
- Clearer API documentation
- Simplified testing (separate test suites)

**Negative**:
- One more endpoint to maintain
- Frontend needs to use different endpoints for different operations

---

## ADR-002: Never Expose JPA Entities in API

**Status**: Accepted  
**Date**: 2026-09-23

### Context

Spring Data JPA entities can be serialized to JSON, making it tempting to return them directly from REST controllers. However, this creates tight coupling between API contracts and database schema.

We need to decide whether to:
1. Return JPA entities directly from controllers
2. Create separate DTOs for all API requests and responses

### Decision

We will use DTOs (Data Transfer Objects) for all API request and response bodies. JPA entities are internal implementation details.

### Consequences

**Positive**:
- API contract decoupled from database schema
- Avoids Jackson serialization issues (lazy loading, circular references)
- Allows different views (summary vs detail responses)
- Enables API versioning without entity changes
- Prevents accidental exposure of internal fields

**Negative**:
- More boilerplate code (DTO classes + conversion methods)
- Potential performance overhead from entity-to-DTO conversion
- Need to keep DTOs and entities in sync manually

---

## ADR-003: Use H2 for Tests, PostgreSQL for Production

**Status**: Accepted  
**Date**: 2026-09-23

### Context

Integration tests need a database. We could use:
1. PostgreSQL for both tests and production (requires external database)
2. H2 in-memory for tests, PostgreSQL for production (no external dependencies)
3. Testcontainers with PostgreSQL Docker image (real database, slow startup)

### Decision

We will use H2 in-memory database for tests and PostgreSQL for production.

### Consequences

**Positive**:
- Fast test execution (no external dependencies)
- Tests can run in CI/CD without setup
- Each test suite gets clean database state
- No network latency during tests

**Negative**:
- H2 and PostgreSQL have minor SQL dialect differences
- Risk of tests passing on H2 but failing on PostgreSQL
- Need to test Flyway migrations on both databases

**Mitigation**: 
- Run integration test suite against PostgreSQL before releases
- Keep SQL in migrations standard (avoid database-specific features)

---

## ADR-004: Use Flyway for Schema Migrations

**Status**: Accepted  
**Date**: 2026-09-23

### Context

Database schema needs to evolve over time. Options:
1. Hibernate `ddl-auto=update` (automatic schema generation)
2. Flyway or Liquibase (versioned migrations)
3. Manual SQL scripts (no automation)

### Decision

We will use Flyway for version-controlled schema migrations.

### Consequences

**Positive**:
- Schema evolution is version-controlled with code
- Migrations are reproducible across environments
- Explicit control over schema changes
- Rollback capability
- Audit trail of all schema changes

**Negative**:
- Cannot modify existing migrations once applied
- Need to write SQL manually (no automatic generation)
- Migration failures can block application startup

**Best Practices**:
- Never modify existing migrations
- Always test migrations on both H2 and PostgreSQL
- Use descriptive migration names: `V1__create_ticket_table.sql`

---

## ADR-005: Global Exception Handler with Consistent Error Format

**Status**: Accepted  
**Date**: 2026-09-23

### Context

Spring Boot applications can throw various exceptions (validation errors, not found, business rule violations, unexpected errors). We need a consistent way to convert these to HTTP responses.

Options:
1. Handle exceptions individually in each controller
2. Use `@RestControllerAdvice` for centralized exception handling
3. Custom HandlerExceptionResolver

### Decision

We will use `@RestControllerAdvice` (GlobalExceptionHandler) to map all exceptions to consistent JSON error responses.

### Consequences

**Positive**:
- Consistent error format across all endpoints
- Centralized exception-to-HTTP mapping
- Controllers remain focused on happy path
- Stack traces never exposed to clients
- Easier to maintain error handling logic

**Negative**:
- One more component to understand
- All exceptions go through single handler (could become complex)

**Error Format**:
```json
{
  "status": 400,
  "message": "Human-readable error",
  "errors": [
    {"field": "title", "message": "must not be blank"}
  ]
}
```

---

## ADR-006: Constructor Injection Over Field Injection

**Status**: Accepted  
**Date**: 2026-09-23

### Context

Spring supports three dependency injection styles:
1. Field injection (`@Autowired` on fields)
2. Setter injection (`@Autowired` on setters)
3. Constructor injection (dependencies as constructor parameters)

### Decision

We will use constructor injection for all Spring-managed beans.

### Consequences

**Positive**:
- Dependencies are explicit and visible
- Immutable fields (can use `final`)
- Easier to test (can construct without Spring context)
- Prevents circular dependencies (fails at startup)
- IDE support for required dependencies

**Negative**:
- More verbose (without Lombok)
- Large constructors indicate design smell (too many dependencies)

**Implementation**: Use Lombok `@RequiredArgsConstructor` to reduce boilerplate

---

## ADR-007: Store Enums as Strings in Database

**Status**: Accepted  
**Date**: 2026-09-23

### Context

JPA supports two enum mapping strategies:
1. `EnumType.ORDINAL` — stores enum position (0, 1, 2...)
2. `EnumType.STRING` — stores enum name ("OPEN", "IN_PROGRESS"...)

### Decision

We will always use `@Enumerated(EnumType.STRING)` for all enum fields.

### Consequences

**Positive**:
- Database values are human-readable
- Safe to reorder enum values in code
- Safe to add new enum values in the middle
- Easier debugging (can read database directly)

**Negative**:
- Slightly more storage space (strings vs integers)
- Cannot rename enum values without migration

**Critical**: Using `ORDINAL` is fragile — reordering enum values breaks database data.

---

## ADR-008: Property-Based Testing with jqwik

**Status**: Accepted  
**Date**: 2026-09-23

### Context

Traditional example-based tests validate specific inputs. Property-based testing validates universal properties across input ranges.

Options:
1. Only example-based tests (JUnit)
2. Property-based tests with jqwik
3. Property-based tests with QuickTheories or other library

### Decision

We will use jqwik for property-based testing, running ≥100 iterations per property.

### Consequences

**Positive**:
- Validates correctness properties from design document
- Discovers edge cases developers wouldn't think of
- Shrinking finds minimal failing examples
- Provides stronger correctness guarantees
- Good integration with JUnit 5

**Negative**:
- Longer test execution time
- More complex test setup
- Steeper learning curve
- Flaky tests if properties are poorly defined

**Usage**: One property test per design document property (13 total)

---

## ADR-009: No Authentication in Phase 1

**Status**: Accepted  
**Date**: 2026-09-23

### Context

Authentication and authorization add complexity. We need to decide whether to implement them in Phase 1.

Options:
1. Full authentication/authorization from start (Spring Security, JWT)
2. No authentication in Phase 1, add later
3. Basic auth or API keys

### Decision

Phase 1 will have no authentication. All endpoints are publicly accessible.

### Consequences

**Positive**:
- Faster initial development
- Simpler testing
- Easier frontend integration
- Focus on core features first

**Negative**:
- Cannot deploy Phase 1 to public internet
- Need to add authentication before production use
- May need to refactor API contracts later

**Future**: Add Spring Security with JWT authentication and role-based authorization in Phase 2.

---

## ADR-010: React/Next.js for Frontend

**Status**: Accepted  
**Date**: 2026-09-23

### Context

We need a frontend framework for the ticket management UI.

Options:
1. React with Create React App
2. Next.js (React meta-framework)
3. Vue.js or Angular
4. Plain HTML/JavaScript

### Decision

We will use Next.js 14+ with TypeScript for the frontend.

### Consequences

**Positive**:
- Server-side rendering (SSR) for better performance
- Built-in routing (file-based)
- API routes (if needed for BFF pattern)
- TypeScript support out of the box
- Large ecosystem and community
- Good developer experience

**Negative**:
- Heavier framework than plain React
- Learning curve for Next.js-specific features
- May be overkill for simple CRUD app

---

## Future ADRs

As the project evolves, additional ADRs will be added for:
- Authentication strategy (JWT, OAuth, etc.)
- Caching approach (Redis, in-memory)
- File attachment storage (S3, local filesystem)
- Real-time updates (WebSocket, Server-Sent Events)
- API versioning strategy
- Deployment architecture (containerization, cloud provider)

---

**Version**: 1.0  
**Last Updated**: 2026-09-23
