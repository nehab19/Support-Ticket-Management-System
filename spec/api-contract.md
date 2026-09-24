# API Contract Document

## Overview

This document defines the REST API contract for the Support Ticket Management System. All endpoints use JSON for request and response bodies.

**Base URL**: `/api`

---

## HTTP Status Codes

| Status Code | Meaning | Usage |
|-------------|---------|-------|
| **200 OK** | Success | GET, PATCH |
| **201 Created** | Resource created | POST (ticket, comment) |
| **400 Bad Request** | Validation failure | Invalid input, missing required fields |
| **404 Not Found** | Resource not found | Ticket ID doesn't exist |
| **422 Unprocessable Entity** | Business rule violation | Invalid status transition |
| **500 Internal Server Error** | Unexpected error | Server-side failure |

---

## Endpoints

### Tickets

| Method | Path | Description | Success Status |
|--------|------|-------------|----------------|
| `POST` | `/api/tickets` | Create a new ticket | 201 |
| `GET` | `/api/tickets` | List all tickets (with optional filters) | 200 |
| `GET` | `/api/tickets/{id}` | Get ticket details with comments | 200 |
| `PATCH` | `/api/tickets/{id}` | Update ticket fields | 200 |
| `PATCH` | `/api/tickets/{id}/status` | Transition ticket status | 200 |

### Comments

| Method | Path | Description | Success Status |
|--------|------|-------------|----------------|
| `POST` | `/api/tickets/{ticketId}/comments` | Add comment to ticket | 201 |

---

## Request/Response Schemas

### Create Ticket

**Endpoint**: `POST /api/tickets`

**Request Body**:
```json
{
  "title": "Login page throws 500",
  "description": "Reproducible on Chrome 124. Steps to reproduce: ...",
  "priority": "HIGH"
}
```

**Request Fields**:
| Field | Type | Required | Constraints | Default |
|-------|------|----------|-------------|---------|
| `title` | string | Yes | 1-255 characters, not blank | - |
| `description` | string | Yes | Not blank | - |
| `priority` | enum | No | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` | `MEDIUM` |

**Success Response** (201 Created):
```json
{
  "id": 1,
  "title": "Login page throws 500",
  "description": "Reproducible on Chrome 124. Steps to reproduce: ...",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": null,
  "createdAt": "2024-06-01T10:00:00Z",
  "updatedAt": "2024-06-01T10:00:00Z",
  "comments": []
}
```

**Error Response** (400 Bad Request):
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "title",
      "message": "must not be blank"
    },
    {
      "field": "title",
      "message": "size must be between 1 and 255"
    }
  ]
}
```

---

### List Tickets

**Endpoint**: `GET /api/tickets`

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `status` | enum | No | Filter by status: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED` |
| `q` | string | No | Search keyword (case-insensitive, searches title and description) |

**Examples**:
- `/api/tickets` — All tickets
- `/api/tickets?status=OPEN` — Only OPEN tickets
- `/api/tickets?q=login` — Tickets with "login" in title or description
- `/api/tickets?status=IN_PROGRESS&q=database` — IN_PROGRESS tickets with "database"

**Success Response** (200 OK):
```json
[
  {
    "id": 1,
    "title": "Login page throws 500",
    "priority": "HIGH",
    "status": "OPEN",
    "assignee": null,
    "createdAt": "2024-06-01T10:00:00Z"
  },
  {
    "id": 2,
    "title": "Database connection timeout",
    "priority": "CRITICAL",
    "status": "IN_PROGRESS",
    "assignee": "alice",
    "createdAt": "2024-06-01T09:30:00Z"
  }
]
```

**Empty Results** (200 OK):
```json
[]
```

**Error Response — Invalid Status** (400 Bad Request):
```json
{
  "status": 400,
  "message": "Invalid status value. Must be one of: OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED"
}
```

**Error Response — Blank Keyword** (400 Bad Request):
```json
{
  "status": 400,
  "message": "Search keyword must not be blank"
}
```

---

### Get Ticket Details

**Endpoint**: `GET /api/tickets/{id}`

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | long | Ticket ID |

**Success Response** (200 OK):
```json
{
  "id": 1,
  "title": "Login page throws 500",
  "description": "Reproducible on Chrome 124. Steps to reproduce: ...",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": null,
  "createdAt": "2024-06-01T10:00:00Z",
  "updatedAt": "2024-06-01T10:00:00Z",
  "comments": [
    {
      "id": 1,
      "author": "alice",
      "body": "Investigating now. Checking server logs.",
      "createdAt": "2024-06-01T11:00:00Z"
    },
    {
      "id": 2,
      "author": "bob",
      "body": "Found the root cause. Deploying fix.",
      "createdAt": "2024-06-01T12:00:00Z"
    }
  ]
}
```

**Error Response — Not Found** (404 Not Found):
```json
{
  "status": 404,
  "message": "Ticket with id 999 not found"
}
```

---

### Update Ticket Fields

**Endpoint**: `PATCH /api/tickets/{id}`

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | long | Ticket ID |

**Request Body** (partial update):
```json
{
  "title": "Login page throws 500 on Chrome",
  "assignee": "alice"
}
```

**Request Fields** (all optional):
| Field | Type | Constraints |
|-------|------|-------------|
| `title` | string | 1-255 characters, not blank |
| `description` | string | Not blank |
| `priority` | enum | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `assignee` | string | 0-255 characters |

**Success Response** (200 OK):
```json
{
  "id": 1,
  "title": "Login page throws 500 on Chrome",
  "description": "Reproducible on Chrome 124. Steps to reproduce: ...",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": "alice",
  "createdAt": "2024-06-01T10:00:00Z",
  "updatedAt": "2024-06-01T13:00:00Z",
  "comments": []
}
```

**Error Responses**:
- **404 Not Found**: Ticket ID doesn't exist
- **400 Bad Request**: Validation failure (same format as create)

---

### Transition Ticket Status

**Endpoint**: `PATCH /api/tickets/{id}/status`

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | long | Ticket ID |

**Request Body**:
```json
{
  "status": "IN_PROGRESS"
}
```

**Request Fields**:
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `status` | enum | Yes | `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED` |

**Allowed Transitions** (see [State Machine](state-machine.md)):
- `OPEN` → `IN_PROGRESS`
- `OPEN` → `CANCELLED`
- `IN_PROGRESS` → `RESOLVED`
- `IN_PROGRESS` → `CANCELLED`
- `RESOLVED` → `CLOSED`

**Success Response** (200 OK):
```json
{
  "id": 1,
  "title": "Login page throws 500",
  "description": "Reproducible on Chrome 124. Steps to reproduce: ...",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "assignee": "alice",
  "createdAt": "2024-06-01T10:00:00Z",
  "updatedAt": "2024-06-01T14:00:00Z",
  "comments": []
}
```

**Error Response — Invalid Transition** (422 Unprocessable Entity):
```json
{
  "status": 422,
  "message": "Cannot transition from OPEN to CLOSED. Allowed transitions from OPEN: IN_PROGRESS, CANCELLED"
}
```

**Error Response — Not Found** (404 Not Found):
```json
{
  "status": 404,
  "message": "Ticket with id 999 not found"
}
```

---

### Add Comment to Ticket

**Endpoint**: `POST /api/tickets/{ticketId}/comments`

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `ticketId` | long | Ticket ID |

**Request Body**:
```json
{
  "author": "alice",
  "body": "Investigating now. Checking server logs."
}
```

**Request Fields**:
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `author` | string | Yes | Not blank, max 255 characters |
| `body` | string | Yes | Not blank |

**Success Response** (201 Created):
```json
{
  "id": 1,
  "author": "alice",
  "body": "Investigating now. Checking server logs.",
  "createdAt": "2024-06-01T11:00:00Z"
}
```

**Error Response — Ticket Not Found** (404 Not Found):
```json
{
  "status": 404,
  "message": "Ticket with id 999 not found"
}
```

**Error Response — Validation Failure** (400 Bad Request):
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "author",
      "message": "must not be blank"
    },
    {
      "field": "body",
      "message": "must not be blank"
    }
  ]
}
```

---

## Error Response Format

All error responses follow this consistent structure:

### Standard Error Response

```json
{
  "status": 404,
  "message": "Human-readable error description"
}
```

### Validation Error Response

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "fieldName",
      "message": "Error message for this field"
    }
  ]
}
```

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| `status` | integer | HTTP status code |
| `message` | string | Human-readable error message |
| `errors` | array | Field-level errors (validation failures only) |

---

## Data Transfer Objects (DTOs)

### Request DTOs

**CreateTicketRequest**:
```java
public record CreateTicketRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title,
    
    @NotBlank(message = "Description is required")
    String description,
    
    Priority priority  // Optional
) {}
```

**UpdateTicketRequest**:
```java
public record UpdateTicketRequest(
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title,
    
    String description,
    Priority priority,
    
    @Size(max = 255, message = "Assignee must not exceed 255 characters")
    String assignee
) {}
```

**StatusTransitionRequest**:
```java
public record StatusTransitionRequest(
    @NotNull(message = "Status is required")
    TicketStatus status
) {}
```

**CreateCommentRequest**:
```java
public record CreateCommentRequest(
    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must not exceed 255 characters")
    String author,
    
    @NotBlank(message = "Body is required")
    String body
) {}
```

### Response DTOs

**TicketSummaryResponse** (for list views):
```java
public record TicketSummaryResponse(
    Long id,
    String title,
    Priority priority,
    TicketStatus status,
    String assignee,
    Instant createdAt
) {}
```

**TicketDetailResponse** (for detail view):
```java
public record TicketDetailResponse(
    Long id,
    String title,
    String description,
    Priority priority,
    TicketStatus status,
    String assignee,
    Instant createdAt,
    Instant updatedAt,
    List<CommentResponse> comments
) {}
```

**CommentResponse**:
```java
public record CommentResponse(
    Long id,
    String author,
    String body,
    Instant createdAt
) {}
```

**ErrorResponse**:
```java
public record ErrorResponse(
    int status,
    String message,
    List<FieldError> errors
) {}

public record FieldError(
    String field,
    String message
) {}
```

---

## Authentication & Authorization

**Phase 1**: No authentication required. All endpoints are publicly accessible.

**Future Phases**:
- JWT-based authentication
- Role-based access control (RBAC)
- Roles: `USER`, `AGENT`, `ADMIN`

---

## CORS Configuration

**Development**:
```properties
# Allow requests from Next.js dev server
spring.web.cors.allowed-origins=http://localhost:3000
spring.web.cors.allowed-methods=GET,POST,PATCH,DELETE
spring.web.cors.allowed-headers=*
```

**Production**: Configure based on deployment environment

---

## API Versioning Strategy

**Current**: No versioning (implicit v1)

**Future**: 
- URL-based versioning: `/api/v2/tickets`
- Or header-based versioning: `Accept: application/vnd.api.v2+json`

---

**Version**: 1.0  
**Last Updated**: 2026-09-23
