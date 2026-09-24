# Requirements Document

## Introduction

The Support Ticket Management System is a full-stack web application that allows users to create, track, and manage support tickets through their lifecycle. The system exposes a REST API built with Java 21 and Spring Boot, persists data in PostgreSQL (H2 for tests), and provides a React/Next.js frontend. A strictly enforced state machine governs ticket status transitions, ensuring data integrity and predictable workflow progression.

---

## Glossary

- **System**: The Support Ticket Management System as a whole.
- **API**: The Spring Boot REST API backend.
- **UI**: The React/Next.js frontend application.
- **Ticket**: A support request entity with a title, description, priority, status, assignee, and associated comments.
- **Assignee**: A user identifier (name or ID) representing the person responsible for resolving a ticket.
- **Status**: The current lifecycle state of a Ticket. Valid values: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`.
- **Priority**: The urgency level of a Ticket. Valid values: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
- **Comment**: A timestamped text entry attached to a Ticket, authored by a user.
- **State_Machine**: The backend component that validates and enforces allowed ticket status transitions.
- **Validator**: The backend component responsible for input validation.
- **Repository**: The data access component responsible for persisting and retrieving entities from the database.
- **Search_Engine**: The backend component responsible for keyword-based ticket search.

---

## Requirements

### Requirement 1: Create a Ticket

**User Story:** As a user, I want to create a support ticket with a title, description, and priority, so that I can report an issue and track its resolution.

#### Acceptance Criteria

1. WHEN a valid ticket creation request is submitted, THE API SHALL create a ticket with status `OPEN` and return the created ticket with a generated ID and creation timestamp.
2. THE API SHALL assign a unique numeric identifier to each created ticket.
3. IF a ticket creation request omits the `title` field, THEN THE Validator SHALL reject the request with HTTP 400 and an error message identifying the missing field.
4. IF a ticket creation request omits the `description` field, THEN THE Validator SHALL reject the request with HTTP 400 and an error message identifying the missing field.
5. IF a ticket creation request provides a `title` longer than 255 characters, THEN THE Validator SHALL reject the request with HTTP 400 and a descriptive error message.
6. IF a ticket creation request provides a `priority` value outside `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`, THEN THE Validator SHALL reject the request with HTTP 400 and a descriptive error message.
7. WHERE no `priority` is specified in the creation request, THE API SHALL default the ticket priority to `MEDIUM`.
8. WHEN a ticket is created successfully, THE UI SHALL display the new ticket in the ticket list without requiring a full page reload.

---

### Requirement 2: List Tickets

**User Story:** As a user, I want to see a list of all support tickets, so that I can get an overview of open issues.

#### Acceptance Criteria

1. WHEN the ticket list is requested, THE API SHALL return all tickets ordered by creation timestamp descending.
2. THE API SHALL include `id`, `title`, `priority`, `status`, `assignee`, and `createdAt` for each ticket in the list response.
3. WHEN the ticket list endpoint is called, THE API SHALL return HTTP 200 with a JSON array, even when no tickets exist (returning an empty array).
4. WHEN the UI loads the ticket list page, THE UI SHALL display the `title`, `priority`, `status`, and `createdAt` of each ticket in a tabular or card layout.

---

### Requirement 3: View Ticket Details

**User Story:** As a user, I want to view the full details of a ticket, so that I can read its description, history, and comments.

#### Acceptance Criteria

1. WHEN a ticket detail request is made with a valid ticket ID, THE API SHALL return the full ticket including `id`, `title`, `description`, `priority`, `status`, `assignee`, `createdAt`, `updatedAt`, and the list of associated comments.
2. IF a ticket detail request is made with an ID that does not exist, THEN THE API SHALL return HTTP 404 with a descriptive error message.
3. WHEN the UI navigates to a ticket detail page, THE UI SHALL display all ticket fields and the list of comments in chronological order.

---

### Requirement 4: Update Ticket Fields

**User Story:** As a user, I want to update a ticket's title, description, priority, and assignee, so that I can correct or refine ticket information.

#### Acceptance Criteria

1. WHEN a valid update request is submitted for an existing ticket, THE API SHALL update the specified fields (`title`, `description`, `priority`, `assignee`) and return the updated ticket with a refreshed `updatedAt` timestamp.
2. IF an update request is submitted for a ticket ID that does not exist, THEN THE API SHALL return HTTP 404 with a descriptive error message.
3. IF an update request provides a `title` longer than 255 characters, THEN THE Validator SHALL reject the request with HTTP 400 and a descriptive error message.
4. IF an update request provides a `priority` value outside `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`, THEN THE Validator SHALL reject the request with HTTP 400 and a descriptive error message.
5. WHEN a ticket field is updated, THE API SHALL only modify the fields included in the request body and leave all other fields unchanged.
6. WHEN a ticket update succeeds, THE UI SHALL reflect the updated values immediately without requiring a full page reload.

---

### Requirement 5: Status Transition (State Machine)

**User Story:** As a user, I want to advance or cancel a ticket through its lifecycle, so that the team can track where each issue stands.

#### Acceptance Criteria

1. THE State_Machine SHALL permit only the following transitions: `OPEN → IN_PROGRESS`, `IN_PROGRESS → RESOLVED`, `RESOLVED → CLOSED`, `OPEN → CANCELLED`, `IN_PROGRESS → CANCELLED`.
2. WHEN a valid status transition request is submitted, THE API SHALL update the ticket status and return the updated ticket with HTTP 200.
3. IF a status transition request specifies a transition not listed in criterion 1 (e.g., `CLOSED → OPEN`, `RESOLVED → OPEN`, `CANCELLED → OPEN`, `CLOSED → IN_PROGRESS`), THEN THE State_Machine SHALL reject the request with HTTP 422 and an error message naming both the current status and the requested status.
4. IF a status transition request is submitted for a ticket ID that does not exist, THEN THE API SHALL return HTTP 404 with a descriptive error message.
5. WHEN the UI displays a ticket, THE UI SHALL show only the status transition actions that are valid for the ticket's current status.
6. WHEN an invalid status transition is attempted from the UI, THE UI SHALL display the error message returned by the API.

---

### Requirement 6: Add Comments

**User Story:** As a user, I want to add comments to a ticket, so that I can document updates, questions, and resolutions.

#### Acceptance Criteria

1. WHEN a valid comment creation request is submitted for an existing ticket, THE API SHALL persist the comment with the provided `author` and `body` fields and return the created comment with a generated ID and `createdAt` timestamp.
2. IF a comment creation request omits the `body` field, THEN THE Validator SHALL reject the request with HTTP 400 and a descriptive error message.
3. IF a comment creation request omits the `author` field, THEN THE Validator SHALL reject the request with HTTP 400 and a descriptive error message.
4. IF a comment creation request is submitted for a ticket ID that does not exist, THEN THE API SHALL return HTTP 404 with a descriptive error message.
5. WHEN a comment is added, THE UI SHALL append the comment to the ticket detail view without requiring a full page reload.

---

### Requirement 7: Search Tickets by Keyword

**User Story:** As a user, I want to search tickets by keyword, so that I can quickly locate tickets related to a specific topic.

#### Acceptance Criteria

1. WHEN a search request is submitted with a non-empty keyword, THE Search_Engine SHALL return all tickets whose `title` or `description` contains the keyword, using a case-insensitive match.
2. WHEN a search request is submitted with a keyword that matches no tickets, THE API SHALL return HTTP 200 with an empty JSON array.
3. IF a search request is submitted with a blank or empty keyword, THEN THE API SHALL return HTTP 400 with a descriptive error message.
4. WHEN search results are returned, THE API SHALL include the same fields as the ticket list response (`id`, `title`, `priority`, `status`, `assignee`, `createdAt`).
5. WHEN the user enters a keyword in the search field, THE UI SHALL display the filtered results in the same ticket list layout.

---

### Requirement 8: Filter Tickets by Status

**User Story:** As a user, I want to filter the ticket list by status, so that I can focus on tickets in a particular lifecycle stage.

#### Acceptance Criteria

1. WHEN a filter request is submitted with a valid status value, THE API SHALL return only tickets whose `status` matches the requested value, ordered by creation timestamp descending.
2. IF a filter request is submitted with a `status` value outside `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`, THEN THE Validator SHALL reject the request with HTTP 400 and a descriptive error message.
3. WHEN a filter request matches no tickets, THE API SHALL return HTTP 200 with an empty JSON array.
4. WHEN the user selects a status from the filter control, THE UI SHALL update the ticket list to show only tickets with the selected status.

---

### Requirement 9: Data Persistence

**User Story:** As a system operator, I want ticket and comment data persisted in a relational database, so that no data is lost when the application restarts.

#### Acceptance Criteria

1. THE Repository SHALL persist all created tickets and comments in a PostgreSQL database when running in production configuration.
2. WHEN the application is restarted, THE Repository SHALL return all previously created tickets and comments unchanged.
3. THE Repository SHALL use H2 in-memory database when running in the `test` Spring profile.
4. THE API SHALL apply schema migrations via Flyway or Liquibase on startup so the database schema is always consistent with the application version.

---

### Requirement 10: Input Validation and Error Responses

**User Story:** As a developer integrating with the API, I want consistent, descriptive error responses, so that I can diagnose and handle failures programmatically.

#### Acceptance Criteria

1. WHEN a request fails validation, THE API SHALL return an error response body containing at minimum a human-readable `message` field and an HTTP status code consistent with the error type (400 for validation errors, 404 for not-found, 422 for business rule violations).
2. THE API SHALL return all validation errors for a request in a single response, not one error at a time.
3. WHEN an unexpected server error occurs, THE API SHALL return HTTP 500 with a generic error message and SHALL NOT expose internal stack traces or implementation details in the response body.
4. WHEN the UI receives an error response from the API, THE UI SHALL display the `message` field from the error response body in a visible, user-facing notification or inline error element.

---

### Requirement 11: Security — No Secrets in Repository

**User Story:** As a security-conscious engineer, I want all secrets and credentials kept out of the source repository, so that sensitive data is never exposed via version control.

#### Acceptance Criteria

1. THE System SHALL store database credentials, API keys, and other secrets exclusively in environment variables or external configuration files that are excluded from version control via `.gitignore`.
2. THE System SHALL provide a `.env.example` or `application.properties.example` file documenting required environment variables without including actual secret values.
3. IF a secrets scan is run against the committed source tree, THEN THE System SHALL produce zero findings for credentials, tokens, or passwords.
