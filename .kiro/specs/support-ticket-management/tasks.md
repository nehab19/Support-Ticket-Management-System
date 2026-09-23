# Implementation Plan: Support Ticket Management System

## Overview

Implement a full-stack support ticket management system with a Spring Boot 3 / Java 21 REST API backed by PostgreSQL (H2 for tests), Flyway schema migrations, Spring Data JPA, and a React / Next.js frontend. A strictly enforced state machine governs ticket lifecycle transitions. All secrets are kept out of version control.

## Tasks

- [x] 1. Backend project scaffolding
  - [x] 1.1 Create Spring Boot 3 Maven project with Java 21
    - Add dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `flyway-core`, `postgresql` driver, `h2` (test scope), `jqwik` (test scope), `spring-boot-starter-test`
    - Configure `pom.xml` with `spring-boot-maven-plugin`
    - Create base package `com.example.supportticket`
    - _Requirements: 9.1, 9.3, 11.1_

  - [x] 1.2 Configure Spring profiles and application properties
    - Create `src/main/resources/application.properties` (app name only, no secrets)
    - Create `src/main/resources/application-prod.properties` reading `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}` from environment variables; enable Flyway
    - Create `src/test/resources/application-test.properties` with H2 in-memory config and Flyway enabled pointing to `classpath:db/migration`
    - _Requirements: 9.1, 9.3, 9.4, 11.1_

  - [x] 1.3 Create `.gitignore` and `.env.example`
    - Add `.gitignore` entries for `*.env`, `.env`, `application-local.properties`, build output, IDE files, OS files
    - Create `.env.example` documenting `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` with placeholder values (no real secrets)
    - _Requirements: 11.1, 11.2, 11.3_

- [x] 2. Database schema and Flyway migrations
  - [x] 2.1 Write `V1__create_ticket_table.sql`
    - Create `ticket` table with columns: `id` (BIGINT PK generated), `title` (VARCHAR 255 NOT NULL), `description` (TEXT NOT NULL), `priority` (VARCHAR 20 NOT NULL default `'MEDIUM'`), `status` (VARCHAR 20 NOT NULL default `'OPEN'`), `assignee` (VARCHAR 255), `created_at` (TIMESTAMPTZ NOT NULL default now()), `updated_at` (TIMESTAMPTZ NOT NULL default now())
    - Add index on `status` column; add index on `created_at DESC`
    - Place at `src/main/resources/db/migration/V1__create_ticket_table.sql`
    - _Requirements: 9.1, 9.4_

  - [x] 2.2 Write `V2__create_comment_table.sql`
    - Create `comment` table with columns: `id` (BIGINT PK generated), `ticket_id` (BIGINT FK → ticket(id) NOT NULL), `author` (VARCHAR 255 NOT NULL), `body` (TEXT NOT NULL), `created_at` (TIMESTAMPTZ NOT NULL default now())
    - Place at `src/main/resources/db/migration/V2__create_comment_table.sql`
    - _Requirements: 9.1, 9.4_

- [x] 3. Domain enums and JPA entities
  - [x] 3.1 Create `TicketStatus` and `Priority` enums
    - `TicketStatus`: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`
    - `Priority`: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
    - Place in `com.example.supportticket.model`
    - _Requirements: 1.6, 5.1_

  - [x] 3.2 Implement `Ticket` JPA entity
    - Fields: `id`, `title`, `description`, `priority` (default `MEDIUM`), `status` (default `OPEN`), `assignee`, `createdAt`, `updatedAt`, `comments` (OneToMany, cascade ALL, orphanRemoval, lazy, ordered by `createdAt ASC`)
    - `@PrePersist` sets both timestamps; `@PreUpdate` sets `updatedAt`
    - `@Enumerated(EnumType.STRING)` on `priority` and `status`
    - _Requirements: 1.1, 1.2, 2.2, 3.1, 9.1_

  - [x] 3.3 Implement `Comment` JPA entity
    - Fields: `id`, `ticket` (ManyToOne), `author`, `body`, `createdAt`
    - `@PrePersist` sets `createdAt`
    - `@Enumerated` not needed; all string columns
    - _Requirements: 6.1, 3.1_

- [x] 4. Repositories
  - [x] 4.1 Create `TicketRepository`
    - Extend `JpaRepository<Ticket, Long>`
    - Add `findAllByOrderByCreatedAtDesc()` for default list ordering
    - Add `findByStatusOrderByCreatedAtDesc(TicketStatus status)` for status filter
    - Add `findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(String titleKw, String descKw)` for keyword search
    - _Requirements: 2.1, 7.1, 8.1_

  - [x] 4.2 Create `CommentRepository`
    - Extend `JpaRepository<Comment, Long>`
    - _Requirements: 6.1_

- [x] 5. DTOs and request / response objects
  - [x] 5.1 Implement request DTOs with Bean Validation annotations
    - `CreateTicketRequest`: `@NotBlank @Size(max=255) title`, `@NotBlank description`, optional `priority`
    - `UpdateTicketRequest`: all fields optional (nullable), `@Size(max=255)` on title if present, validated priority if present
    - `StatusTransitionRequest`: `@NotNull status` (TicketStatus)
    - `CreateCommentRequest`: `@NotBlank author`, `@NotBlank body`
    - Place in `com.example.supportticket.dto`
    - _Requirements: 1.3, 1.4, 1.5, 1.6, 4.3, 4.4, 6.2, 6.3, 10.1_

  - [x] 5.2 Implement response DTOs
    - `TicketSummaryResponse`: `id`, `title`, `priority`, `status`, `assignee`, `createdAt`
    - `TicketDetailResponse`: all summary fields plus `description`, `updatedAt`, `List<CommentResponse>`
    - `CommentResponse`: `id`, `author`, `body`, `createdAt`
    - `ErrorResponse` record: `int status`, `String message`, `List<FieldError> errors`
    - `FieldError` record: `String field`, `String message`
    - _Requirements: 2.2, 3.1, 6.1, 10.1_

- [x] 6. State machine component
  - [x] 6.1 Implement `TicketStateMachine`
    - Encode allowed transitions in a static `Map<TicketStatus, Set<TicketStatus>>`
    - `validate(TicketStatus from, TicketStatus to)` method: throw `InvalidStatusTransitionException` with message `"Transition from {from} to {to} is not allowed"` for disallowed pairs; no-op for allowed pairs
    - _Requirements: 5.1, 5.2, 5.3_

  - [x] 6.2 Implement custom exceptions
    - `TicketNotFoundException(Long id)` with message `"Ticket {id} not found"`
    - `InvalidStatusTransitionException(TicketStatus from, TicketStatus to)` with message format above
    - _Requirements: 3.2, 4.2, 5.3, 5.4, 6.4_

- [x] 7. Global exception handler
  - [x] 7.1 Implement `GlobalExceptionHandler` with `@RestControllerAdvice`
    - Handle `MethodArgumentNotValidException` → HTTP 400, collect all field errors into `errors` array
    - Handle `ConstraintViolationException` → HTTP 400
    - Handle `MethodArgumentTypeMismatchException` (bad enum values) → HTTP 400 with descriptive message
    - Handle `TicketNotFoundException` → HTTP 404
    - Handle `InvalidStatusTransitionException` → HTTP 422
    - Handle `Exception` (catch-all) → HTTP 500 with generic message, no stack trace in response body
    - _Requirements: 10.1, 10.2, 10.3_

- [x] 8. Services
  - [x] 8.1 Implement `TicketService`
    - `createTicket(CreateTicketRequest)`: set status to OPEN, default priority to MEDIUM if absent, persist, return `TicketDetailResponse`
    - `listTickets(Optional<TicketStatus> status, Optional<String> q)`: delegate to repository for filter/search/all; return `List<TicketSummaryResponse>`; throw HTTP 400 (via `ResponseStatusException` or custom) if `q` is blank
    - `getTicket(Long id)`: fetch or throw `TicketNotFoundException`; return `TicketDetailResponse` with comments
    - `updateTicket(Long id, UpdateTicketRequest)`: apply only non-null fields; update `updatedAt` (handled by `@PreUpdate`); return `TicketDetailResponse`
    - `transitionStatus(Long id, StatusTransitionRequest)`: fetch ticket, call `TicketStateMachine.validate()`, set new status, persist, return `TicketDetailResponse`
    - _Requirements: 1.1, 1.2, 1.7, 2.1, 3.1, 4.1, 4.5, 5.1, 5.2, 7.1, 7.3, 8.1_

  - [x] 8.2 Implement `CommentService`
    - `addComment(Long ticketId, CreateCommentRequest)`: fetch ticket or throw `TicketNotFoundException`; create and persist `Comment`; return `CommentResponse`
    - _Requirements: 6.1, 6.4_

- [x] 9. REST controllers
  - [x] 9.1 Implement `TicketController`
    - `POST /api/tickets` → `createTicket`, returns HTTP 201
    - `GET /api/tickets` (optional `?status=` and `?q=`) → `listTickets`, returns HTTP 200
    - `GET /api/tickets/{id}` → `getTicket`, returns HTTP 200
    - `PATCH /api/tickets/{id}` → `updateTicket`, returns HTTP 200
    - `PATCH /api/tickets/{id}/status` → `transitionStatus`, returns HTTP 200
    - Use `@Valid` on all request bodies
    - _Requirements: 1.1, 2.1, 2.3, 3.1, 4.1, 5.2, 7.1, 8.1_

  - [x] 9.2 Implement `CommentController`
    - `POST /api/tickets/{ticketId}/comments` → `addComment`, returns HTTP 201
    - Use `@Valid` on request body
    - _Requirements: 6.1_

- [x] 10. Backend unit and integration tests
  - [x] 10.1 Write `TicketStateMachineTest` unit tests
    - Test all 5 allowed transitions succeed (no exception thrown)
    - Test representative disallowed transitions: `CLOSED→OPEN`, `RESOLVED→OPEN`, `CANCELLED→OPEN`, `CLOSED→IN_PROGRESS`, `RESOLVED→IN_PROGRESS`
    - Verify exception message contains both status names
    - _Requirements: 5.1, 5.3_

  - [x] 10.2 Write property test `StateMachinePropertyTest` (Property 8)
    - **Property 8: State machine — allowed transitions succeed; disallowed transitions are rejected with HTTP 422**
    - Generate all (from, to) enum pairs; assert allowed set succeeds and complement set throws with message containing both names
    - **Validates: Requirements 5.1, 5.2, 5.3**

  - [x] 10.3 Write `TicketRepositoryTest` with `@DataJpaTest`
    - Run against H2 with Flyway migrations applied (`@ActiveProfiles("test")`)
    - Test CRUD, ordering by `createdAt DESC`, keyword search (case-insensitive), status filter
    - _Requirements: 2.1, 7.1, 8.1, 9.3_

  - [x] 10.4 Write `TicketServiceTest` with mocked repository
    - Test `createTicket`: status defaults to OPEN, priority defaults to MEDIUM
    - Test `listTickets`: blank keyword throws 400
    - Test `updateTicket`: partial update leaves untouched fields unchanged
    - Test `transitionStatus`: delegates to state machine; propagates `InvalidStatusTransitionException`
    - _Requirements: 1.1, 1.7, 4.5, 5.2_

  - [x] 10.5 Write property test `TicketCreationPropertyTest` (Property 1)
    - **Property 1: Ticket creation invariants**
    - Generate valid `CreateTicketRequest` inputs; assert result has `status == OPEN`, non-null `id`, non-null `createdAt`
    - **Validates: Requirements 1.1**

  - [x] 10.6 Write property test `PartialUpdatePropertyTest` (Property 7)
    - **Property 7: Partial update preserves untouched fields**
    - Generate existing ticket + partial update request; assert only specified fields changed
    - **Validates: Requirements 4.5**

  - [x] 10.7 Write `GlobalExceptionHandlerTest` with `@WebMvcTest`
    - Test each exception type maps to the correct HTTP status code and response body shape (`message`, `status`, `errors`)
    - Verify 500 response does not contain stack trace
    - _Requirements: 10.1, 10.2, 10.3_

  - [x] 10.8 Write property test `ErrorResponseCompletenessPropertyTest` (Property 13)
    - **Property 13: Error response completeness**
    - Generate invalid requests (missing fields, bad enums, unknown IDs); assert all error responses include `message`, `status`, and `errors` array with at least one entry per failing field for HTTP 400
    - **Validates: Requirements 10.1, 10.2**

  - [x] 10.9 Write `TicketControllerTest` with `@WebMvcTest`
    - Test request parsing, `@Valid` enforcement, and JSON serialisation for each endpoint
    - Test `?q=` blank parameter returns 400; `?status=` invalid enum returns 400
    - _Requirements: 1.3, 1.4, 1.5, 1.6, 7.3, 8.2_

  - [x] 10.10 Write property test `TitleLengthValidationPropertyTest` (Property 3)
    - **Property 3: Title length validation (create and update)**
    - Generate strings of length 256–1000; assert HTTP 400 for both create and update endpoints
    - **Validates: Requirements 1.5, 4.3**

  - [x] 10.11 Write property test `PriorityValidationPropertyTest` (Property 4)
    - **Property 4: Priority enum validation (create and update)**
    - Generate arbitrary strings excluding valid priority values; assert HTTP 400 for both endpoints
    - **Validates: Requirements 1.6, 4.4**

  - [x] 10.12 Write `TicketIntegrationTest` with `@SpringBootTest` (RANDOM_PORT)
    - Full-stack happy paths: create → list → detail → update → transition → add comment
    - Error paths: 404 on unknown ID, 422 on invalid transition, 400 on blank fields
    - Use `TestRestTemplate` or `WebTestClient`; activate `test` profile
    - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.2, 6.1_

  - [x] 10.13 Write property test `TicketListOrderingPropertyTest` (Property 5)
    - **Property 5: Ticket list ordering**
    - Create N tickets with distinct timestamps; assert list response is sorted by `createdAt` descending
    - **Validates: Requirements 2.1, 8.1**

  - [x] 10.14 Write property test `TicketListFieldCompletenessPropertyTest` (Property 6)
    - **Property 6: Ticket list field completeness**
    - For each ticket in list / search / filter responses, assert `id`, `title`, `priority`, `status`, `assignee`, `createdAt` are all present and non-null (except nullable `assignee`)
    - **Validates: Requirements 2.2, 7.4**

  - [x] 10.15 Write property test `CommentCreationPropertyTest` (Property 9)
    - **Property 9: Comment creation round-trip**
    - Generate valid `CreateCommentRequest`; assert created comment has non-null `id`, non-null `createdAt`, and `author`/`body` matching request
    - **Validates: Requirements 6.1**

  - [x] 10.16 Write property test `KeywordSearchPropertyTest` (Property 10)
    - **Property 10: Keyword search correctness**
    - Generate a corpus of tickets and a keyword; assert response contains exactly the matching tickets (case-insensitive) and no others
    - **Validates: Requirements 7.1, 7.2**

  - [x] 10.17 Write property test `BlankKeywordPropertyTest` (Property 11)
    - **Property 11: Blank keyword rejection**
    - Generate whitespace-only and empty strings as `q` parameter; assert HTTP 400
    - **Validates: Requirements 7.3**

  - [x] 10.18 Write property test `StatusFilterPropertyTest` (Property 12)
    - **Property 12: Status filter correctness**
    - Create tickets of mixed statuses; for each valid `TicketStatus`, assert filter returns only matching tickets in `createdAt` descending order
    - **Validates: Requirements 8.1, 8.3**

  - [x] 10.19 Write property test `TicketIdUniquenessPropertyTest` (Property 2)
    - **Property 2: Unique ticket identifiers**
    - Create N tickets sequentially; assert all returned IDs are distinct
    - **Validates: Requirements 1.2**

  - [x] 10.20 Write `FlywayMigrationTest`
    - Assert `FlywayMigrationInfo` reports zero pending migrations after application context loads
    - _Requirements: 9.4_

- [x] 11. Checkpoint — Backend complete
  - Ensure all backend tests pass with `mvn test -Dspring.profiles.active=test`
  - Ensure H2 integration tests run without a real PostgreSQL instance
  - Ask the user if questions arise before proceeding to frontend.

- [x] 12. Frontend scaffolding (Next.js)
  - [x] 12.1 Initialise Next.js project with TypeScript
    - Create `frontend/` directory; initialise with `create-next-app` using TypeScript and App Router
    - Install dependencies: `axios` or native `fetch`, `react-query` or `swr` for data fetching
    - Install dev dependencies: `jest`, `@testing-library/react`, `@testing-library/jest-dom`, `@testing-library/user-event`, `jest-environment-jsdom`
    - Configure Jest with `jest.config.js` and `tsconfig` paths
    - _Requirements: 1.8, 2.4, 3.3_

  - [x] 12.2 Define shared TypeScript types and API base URL configuration
    - Create `frontend/lib/types.ts` with interfaces: `Ticket`, `TicketSummary`, `Comment`, `ErrorResponse`, `FieldError`, `CreateTicketPayload`, `UpdateTicketPayload`, `StatusTransitionPayload`, `CreateCommentPayload`
    - Read API base URL from `NEXT_PUBLIC_API_BASE_URL` environment variable; add to `.env.example`
    - _Requirements: 11.1, 11.2_

  - [x] 12.3 Create `frontend/lib/api.ts` typed API client
    - Implement typed fetch wrappers for all endpoints: `createTicket`, `listTickets` (with optional `status` and `q` params), `getTicket`, `updateTicket`, `transitionStatus`, `addComment`
    - On non-OK responses, parse and throw the `ErrorResponse` JSON body so callers can display `message`
    - _Requirements: 1.8, 2.4, 3.3, 4.6, 5.6, 6.5, 7.5, 8.4, 10.4_

- [x] 13. Frontend components
  - [x] 13.1 Implement `ErrorNotification` component
    - Accept an `error: string | null` prop; render a visible dismissible notification when non-null
    - _Requirements: 10.4_

  - [x] 13.2 Write Jest tests for `ErrorNotification`
    - Assert renders nothing when `error` is null
    - Assert renders error message string when provided
    - _Requirements: 10.4_

  - [x] 13.3 Implement `SearchBar` component
    - Controlled input with debounce; calls `onSearch(keyword)` callback on change
    - _Requirements: 7.5_

  - [x] 13.4 Write Jest tests for `SearchBar`
    - Assert `onSearch` is called with trimmed value after user input
    - _Requirements: 7.5_

  - [x] 13.5 Implement `StatusFilter` component
    - Dropdown listing all `TicketStatus` values plus an "All" option; calls `onStatusChange` callback on selection
    - _Requirements: 8.4_

  - [x] 13.6 Implement `TicketTable` component
    - Render tabular list of `TicketSummary[]` with columns: title, priority, status, createdAt
    - Each row links to the ticket detail page
    - _Requirements: 2.4_

  - [x] 13.7 Write Jest tests for `TicketTable`
    - Assert renders correct number of rows
    - Assert columns display expected field values
    - _Requirements: 2.4_

  - [x] 13.8 Implement `CreateTicketForm` component
    - Form fields: title (required), description (required), priority (select, default MEDIUM)
    - On submit: call `api.createTicket`; on success call `onCreated` callback; on error display via `ErrorNotification`
    - _Requirements: 1.8, 10.4_

  - [x] 13.9 Implement `UpdateTicketForm` component
    - Pre-populate fields from existing ticket; submit only changed fields
    - On success call `onUpdated` callback; on error display via `ErrorNotification`
    - _Requirements: 4.6, 10.4_

  - [x] 13.10 Implement `StatusTransitionPanel` component
    - Derive valid next statuses from current ticket status (matching state machine allowed transitions)
    - Render only valid transition buttons; on click call `api.transitionStatus`; on error display error message from API response
    - _Requirements: 5.5, 5.6_

  - [x] 13.11 Write Jest tests for `StatusTransitionPanel`
    - Assert only valid transition buttons render for each status (OPEN shows IN_PROGRESS and CANCELLED; CLOSED shows no buttons; etc.)
    - Assert error message from API is displayed on failed transition
    - _Requirements: 5.5, 5.6_

  - [x] 13.12 Implement `CommentList` component
    - Render list of `Comment[]` in chronological (ascending `createdAt`) order
    - Display `author`, `body`, and formatted `createdAt` for each comment
    - _Requirements: 3.3_

  - [x] 13.13 Implement `CommentForm` component
    - Fields: author (required), body (required textarea)
    - On submit: call `api.addComment`; on success call `onAdded` callback; on error display via `ErrorNotification`
    - _Requirements: 6.5, 10.4_

- [x] 14. Frontend pages
  - [x] 14.1 Implement ticket list page (`pages/index.tsx`)
    - Fetch ticket list using `api.listTickets` with `status` and `q` query params driven by `StatusFilter` and `SearchBar` state
    - Display results in `TicketTable`; show `CreateTicketForm` in a modal/drawer triggered by a "New Ticket" button
    - On ticket created, refresh list without full page reload
    - Show `ErrorNotification` for any API error
    - _Requirements: 1.8, 2.4, 7.5, 8.4, 10.4_

  - [x] 14.2 Implement ticket detail page (`pages/tickets/[id].tsx`)
    - Fetch full ticket using `api.getTicket(id)`
    - Display all ticket fields, `UpdateTicketForm`, `StatusTransitionPanel`, `CommentList`, and `CommentForm`
    - After any mutation (update, transition, add comment) re-fetch ticket data to reflect changes without full page reload
    - Show `ErrorNotification` for any API error
    - _Requirements: 3.3, 4.6, 5.5, 5.6, 6.5, 10.4_

- [x] 15. Final checkpoint — Full system complete
  - Ensure all backend tests pass: `mvn test -Dspring.profiles.active=test`
  - Ensure all frontend tests pass: `cd frontend && npx jest --run`
  - Verify `.gitignore` excludes `.env`, `application-local.properties`, and build artefacts; confirm `.env.example` lists all required variables with placeholder values
  - Ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP; core functionality is covered by non-optional tasks.
- Each task references specific requirements for traceability.
- Property-based tests use jqwik (`@Property(tries = 100)`) and must be annotated with a comment referencing the property number: `// Feature: support-ticket-management, Property N: <title>`.
- Spring profile `test` activates H2; no external PostgreSQL instance is needed for any test task.
- The `UpdateTicketRequest` uses nullable fields and applies only non-null values to satisfy the partial-update requirement (Requirements 4.5).
- The `StatusTransitionPanel` frontend component mirrors the server-side state machine to show only valid next actions — client-side enforcement is for UX only; server-side enforcement is authoritative.
- Secrets (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `NEXT_PUBLIC_API_BASE_URL`) must never appear in committed files; only placeholder values belong in `.env.example`.
