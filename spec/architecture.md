# Architecture Document

## Overview

The Support Ticket Management System follows a layered, three-tier architecture with clear separation of concerns between presentation, business logic, and data access layers.

---

## System Architecture

```
┌─────────────────────────────────────────┐
│          React / Next.js  (UI)           │  Browser
└────────────────┬────────────────────────┘
                 │  HTTP / JSON
┌────────────────▼────────────────────────┐
│         Spring Boot REST API             │  JVM / Java 21
│  ┌──────────┐  ┌──────────┐  ┌────────┐ │
│  │Controller│  │ Service  │  │  Repo  │ │
│  └──────────┘  └──────────┘  └───┬────┘ │
│                                   │      │
└───────────────────────────────────┼──────┘
                                    │  JDBC / JPA
           ┌────────────────────────▼──────────────┐
           │  PostgreSQL (prod) / H2 (test)          │
           └───────────────────────────────────────┘
```

---

## Layer Responsibilities

### Controller Layer
**Technology**: Spring Web MVC (`@RestController`)

**Responsibilities**:
- Parse HTTP requests and validate structure
- Convert between DTOs and domain entities
- Delegate business logic to service layer
- Return appropriate HTTP status codes
- Handle content negotiation (JSON)

**Rules**:
- NO business logic in controllers
- NO direct database access
- Use DTOs for all request/response bodies
- Let GlobalExceptionHandler handle exceptions

---

### Service Layer
**Technology**: Spring Services (`@Service`)

**Responsibilities**:
- Implement business rules and validation
- Orchestrate operations across multiple repositories
- Define transactional boundaries (`@Transactional`)
- Convert entities to DTOs for responses
- Throw business exceptions (not HTTP exceptions)

**Key Components**:
- `TicketService`: Core ticket CRUD and orchestration
- `CommentService`: Comment persistence logic
- `TicketStateMachine`: State transition validation

---

### Repository Layer
**Technology**: Spring Data JPA (`JpaRepository`)

**Responsibilities**:
- Data access through JPA
- Custom query methods
- Database transaction management (via Spring)

**Rules**:
- NO business logic in repositories
- Use method name derivation where possible
- Use `@Query` for complex queries

---

### Database Layer
**Technology**: PostgreSQL (production), H2 (tests)

**Responsibilities**:
- Data persistence
- Referential integrity enforcement
- Index management for performance

**Schema Management**: Flyway migrations

---

## Component Map

```
com.example.supportticket/
├── controller/                  # REST endpoints
│   ├── TicketController         # CRUD + status transition
│   └── CommentController        # POST /tickets/{id}/comments
│
├── service/                     # Business logic
│   ├── TicketService           # Ticket operations
│   ├── CommentService          # Comment operations
│   └── TicketStateMachine      # Validates status transitions
│
├── repository/                  # Data access
│   ├── TicketRepository        # Spring Data JPA
│   └── CommentRepository       # Spring Data JPA
│
├── model/                       # JPA entities
│   ├── Ticket                  # Core entity
│   ├── Comment                 # Comment entity
│   ├── TicketStatus            # Enum
│   └── Priority                # Enum
│
├── dto/                         # Data Transfer Objects
│   ├── request/
│   │   ├── CreateTicketRequest
│   │   ├── UpdateTicketRequest
│   │   └── StatusTransitionRequest
│   └── response/
│       ├── TicketSummaryResponse
│       ├── TicketDetailResponse
│       └── CommentResponse
│
├── exception/                   # Custom exceptions
│   ├── TicketNotFoundException
│   ├── InvalidStatusTransitionException
│   └── GlobalExceptionHandler  # @RestControllerAdvice
│
└── config/                      # Configuration
    └── SecurityConfig          # (placeholder for future auth)
```

---

## Frontend Architecture

```
pages/
  index.tsx                 # Ticket list page
  tickets/[id].tsx          # Ticket detail page

components/
  TicketTable.tsx            # Tabular ticket list
  TicketCard.tsx             # Card-layout summary
  CreateTicketForm.tsx       # Modal for creation
  UpdateTicketForm.tsx       # Edit form
  StatusTransitionPanel.tsx  # Valid next-status actions
  CommentList.tsx            # Comment display
  CommentForm.tsx            # Add comment
  SearchBar.tsx              # Keyword search
  StatusFilter.tsx           # Status dropdown
  ErrorNotification.tsx      # API error display

lib/
  api.ts                    # Typed API client
  types.ts                  # TypeScript types
```

---

## Key Design Decisions

### 1. Separate Status-Transition Endpoint
**Decision**: Use `PATCH /tickets/{id}/status` for status changes, separate from field updates.

**Rationale**: 
- Makes state machine enforcement explicit
- Clear separation of concerns
- Easier to audit and test
- Distinct error handling (422 vs 400)

---

### 2. DTO-Based API Contracts
**Decision**: Never expose JPA entities directly; always use DTOs.

**Rationale**:
- Decouples API from database schema
- Prevents Jackson serialization issues (lazy loading, circular refs)
- Allows different views (summary vs detail)
- Enables API versioning without entity changes

---

### 3. Global Exception Handler
**Decision**: Use `@RestControllerAdvice` to map all exceptions centrally.

**Rationale**:
- Consistent error response format
- Prevents stack trace leakage
- Centralizes error-to-HTTP mapping
- Simplifies controller code

---

### 4. Spring Profiles for Datasource
**Decision**: Use `test` profile for H2, default/prod for PostgreSQL.

**Rationale**:
- Fast in-memory tests
- No external dependencies for test suite
- Production uses real PostgreSQL
- No credentials in committed files

---

### 5. Flyway for Migrations
**Decision**: Version all schema changes as SQL migration files.

**Rationale**:
- Version-controlled schema evolution
- Automatic application on startup
- Reproducible across environments
- Rollback capability

---

## Technology Stack

### Backend
- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.x
- **Build Tool**: Gradle 8.x
- **Database (Prod)**: PostgreSQL 15+
- **Database (Test)**: H2 2.x
- **Migration**: Flyway 9.x
- **Validation**: Jakarta Bean Validation
- **Testing**: JUnit 5, jqwik (property-based)

### Frontend
- **Framework**: React 18+
- **Meta-framework**: Next.js 14+
- **Language**: TypeScript 5+
- **Testing**: Jest, React Testing Library
- **E2E**: Cypress/Playwright

---

## Communication Patterns

### Backend ↔ Frontend
- **Protocol**: HTTP/REST
- **Format**: JSON
- **Authentication**: None (Phase 1)
- **CORS**: Configured for local development

### Backend ↔ Database
- **Protocol**: JDBC
- **ORM**: Hibernate (via Spring Data JPA)
- **Connection Pool**: HikariCP (Spring Boot default)
- **Migration**: Flyway on startup

---

## Error Handling Architecture

### Exception Hierarchy
```
RuntimeException
├── TicketNotFoundException (404)
├── InvalidStatusTransitionException (422)
└── ... (other business exceptions)
```

### Exception-to-HTTP Mapping
| Exception | HTTP Status | Response Body |
|-----------|-------------|---------------|
| `MethodArgumentNotValidException` | 400 | Field-level errors |
| `TicketNotFoundException` | 404 | Single message |
| `InvalidStatusTransitionException` | 422 | Transition details |
| `Exception` (catch-all) | 500 | Generic message |

### GlobalExceptionHandler
Centralized component that:
- Catches all controller exceptions
- Maps to appropriate HTTP status
- Formats consistent error responses
- Logs internal errors
- Suppresses stack traces in responses

---

## Security Architecture

### Phase 1 (Current)
- **Authentication**: None
- **Authorization**: None
- **Secrets**: Environment variables only

### Future Phases
- Spring Security with JWT
- Role-based access control (RBAC)
- Roles: `USER`, `AGENT`, `ADMIN`

---

## Deployment Architecture

### Development Environment
```
┌─────────────┐      ┌─────────────┐
│  Next.js    │─────▶│ Spring Boot │
│  localhost  │      │  localhost  │
│  :3000      │      │  :8080      │
└─────────────┘      └──────┬──────┘
                            │
                     ┌──────▼──────┐
                     │  PostgreSQL │
                     │  localhost  │
                     │  :5432      │
                     └─────────────┘
```

### Test Environment
```
┌─────────────────┐
│   Test Suite    │
│  @SpringBootTest│
└────────┬────────┘
         │
    ┌────▼────┐
    │   H2    │
    │ in-mem  │
    └─────────┘
```

---

## Performance Considerations

### Database Indexes
- `ticket(status)` — supports status filters
- `ticket(created_at DESC)` — supports default ordering
- `comment(ticket_id)` — supports comment lookups

### Query Optimization
- Use projections for list views (summary fields only)
- Lazy loading for relationships
- Query parameter binding (no string concatenation)

### Future Optimizations
- Full-text search with PostgreSQL GIN indexes
- Caching layer (Redis)
- Connection pool tuning
- API rate limiting

---

## Monitoring and Observability

### Logging
- **Framework**: SLF4J + Logback
- **Levels**: ERROR, WARN, INFO, DEBUG, TRACE
- **Format**: Structured (JSON in production)

### Metrics (Future)
- Spring Boot Actuator
- Micrometer
- Prometheus + Grafana

---

## Scalability Considerations

### Current Architecture
- Single-instance deployment
- Direct database connection
- Suitable for <1000 concurrent users

### Future Enhancements
- Horizontal scaling (stateless services)
- Load balancer (nginx/ALB)
- Database connection pooling
- Caching layer
- Message queue for async operations

---

**Version**: 1.0  
**Last Updated**: 2026-09-23
