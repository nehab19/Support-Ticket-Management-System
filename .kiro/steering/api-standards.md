---
inclusion: auto
---

# API Standards and Guidelines

**Project**: Support Ticket Management System  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Purpose

These guidelines establish consistent REST API design standards for the Support Ticket Management System. All APIs—whether written by humans or AI—must follow these conventions.

**Scope**: REST API design, HTTP semantics, error handling  
**Authority**: Complements PROJECT_CONSTITUTION.md with API-specific guidance

---

## Core Principles

### RESTful Design

1. **Resource-Oriented** — URLs represent resources, not actions
2. **HTTP Verbs** — Use standard verbs for operations (GET, POST, PATCH, DELETE)
3. **Stateless** — Each request contains all necessary information
4. **HATEOAS-Ready** — Prepare for hypermedia links (future enhancement)
5. **Consistent** — Predictable patterns across all endpoints

---

## URL Design

### Resource Naming

**Use Nouns, Not Verbs**:
```
✅ GOOD:
GET    /api/tickets
POST   /api/tickets
GET    /api/tickets/123
PATCH  /api/tickets/123

❌ BAD:
POST   /api/createTicket
GET    /api/getTicket/123
POST   /api/updateTicket
```

**Use Plural Nouns**:
```
✅ GOOD:
/api/tickets
/api/tickets/123/comments

❌ BAD:
/api/ticket
/api/tickets/123/comment
```

**Use Kebab-Case for Multi-Word Resources**:
```
✅ GOOD:
/api/support-tickets
/api/ticket-attachments

❌ BAD:
/api/supportTickets
/api/ticket_attachments
/api/SupportTickets
```

### Hierarchical Resources

**Use Nesting for Relationships**:
```
✅ GOOD:
POST   /api/tickets/123/comments        # Create comment on ticket 123
GET    /api/tickets/123/comments        # List comments for ticket 123
GET    /api/tickets/123/comments/456    # Get specific comment

❌ BAD:
POST   /api/comments?ticketId=123
GET    /api/getCommentsForTicket?id=123
```

**Limit Nesting Depth (Max 2 Levels)**:
```
✅ GOOD:
/api/tickets/123/comments

❌ BAD: (too deep)
/api/departments/5/teams/7/users/9/tickets/123
```

---

## HTTP Methods

### Standard Verb Usage

| Method | Purpose | Idempotent | Safe | Response Body |
|--------|---------|------------|------|---------------|
| `GET` | Retrieve resource(s) | Yes | Yes | Resource data |
| `POST` | Create new resource | No | No | Created resource |
| `PATCH` | Partial update | No | No | Updated resource |
| `PUT` | Full replacement | Yes | No | Updated resource |
| `DELETE` | Remove resource | Yes | No | Empty or status |

### Method Guidelines

**GET — Retrieve Resources**:
```java
// ✅ GOOD: Retrieve without side effects
@GetMapping("/api/tickets/{id}")
public ResponseEntity<TicketDetailResponse> getTicket(@PathVariable Long id) {
    TicketDetailResponse ticket = ticketService.getTicketById(id);
    return ResponseEntity.ok(ticket);
}

// ❌ BAD: Side effects in GET
@GetMapping("/api/tickets/{id}/increment-view-count")  // Should be POST
```

**POST — Create New Resources**:
```java
// ✅ GOOD: Create and return with 201
@PostMapping("/api/tickets")
public ResponseEntity<TicketDetailResponse> createTicket(
    @Valid @RequestBody CreateTicketRequest request
) {
    TicketDetailResponse ticket = ticketService.createTicket(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .header("Location", "/api/tickets/" + ticket.id())
        .body(ticket);
}
```

**PATCH — Partial Updates**:
```java
// ✅ GOOD: Partial update (only specified fields change)
@PatchMapping("/api/tickets/{id}")
public ResponseEntity<TicketDetailResponse> updateTicket(
    @PathVariable Long id,
    @Valid @RequestBody UpdateTicketRequest request
) {
    TicketDetailResponse ticket = ticketService.updateTicket(id, request);
    return ResponseEntity.ok(ticket);
}

// Use PATCH for partial, PUT for full replacement
```

**DELETE — Remove Resources**:
```java
// ✅ GOOD: Delete with 204 (no content)
@DeleteMapping("/api/tickets/{id}")
public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
    ticketService.deleteTicket(id);
    return ResponseEntity.noContent().build();
}
```

---

## HTTP Status Codes

### Standard Status Codes

**Success Codes (2xx)**:
```
200 OK                — GET, PATCH, PUT succeeded
201 Created           — POST created new resource
204 No Content        — DELETE succeeded (no response body)
```

**Client Error Codes (4xx)**:
```
400 Bad Request       — Validation failure, malformed request
401 Unauthorized      — Authentication required
403 Forbidden         — Authenticated but not authorized
404 Not Found         — Resource doesn't exist
409 Conflict          — Resource conflict (duplicate unique key)
422 Unprocessable     — Business rule violation
429 Too Many Requests — Rate limit exceeded
```

**Server Error Codes (5xx)**:
```
500 Internal Server Error — Unexpected error
503 Service Unavailable   — Temporary outage
```

### Status Code Usage

**200 OK**:
```java
// ✅ GOOD: Successful retrieval or update
@GetMapping("/api/tickets/{id}")
public ResponseEntity<TicketDetailResponse> getTicket(@PathVariable Long id) {
    return ResponseEntity.ok(ticketService.getTicketById(id));
}
```

**201 Created**:
```java
// ✅ GOOD: Created with Location header
@PostMapping("/api/tickets")
public ResponseEntity<TicketDetailResponse> createTicket(@RequestBody CreateTicketRequest request) {
    TicketDetailResponse ticket = ticketService.createTicket(request);
    return ResponseEntity
        .created(URI.create("/api/tickets/" + ticket.id()))
        .body(ticket);
}
```

**400 Bad Request**:
```java
// ✅ GOOD: Validation errors
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    return ResponseEntity.badRequest().body(buildErrorResponse(ex));
}
```

**404 Not Found**:
```java
// ✅ GOOD: Resource not found
@ExceptionHandler(TicketNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(TicketNotFoundException ex) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(404, ex.getMessage()));
}
```

**422 Unprocessable Entity**:
```java
// ✅ GOOD: Business rule violation
@ExceptionHandler(InvalidStatusTransitionException.class)
public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex) {
    return ResponseEntity
        .status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(new ErrorResponse(422, ex.getMessage()));
}
```

---

## Request Design

### Content-Type

**Always Use JSON**:
```
Content-Type: application/json
```

### Request Body Validation

**Use Bean Validation Annotations**:
```java
// ✅ GOOD: Declarative validation
public record CreateTicketRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title,
    
    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    String description,
    
    Priority priority  // Optional
) {}
```

**Validate in Controller**:
```java
// ✅ GOOD: @Valid triggers validation
@PostMapping("/api/tickets")
public ResponseEntity<TicketDetailResponse> createTicket(
    @Valid @RequestBody CreateTicketRequest request  // @Valid here
) {
    return ResponseEntity.status(201).body(ticketService.createTicket(request));
}
```

### Query Parameters

**Use for Filtering, Sorting, Pagination**:
```
✅ GOOD:
GET /api/tickets?status=OPEN&sort=createdAt&order=desc&page=0&size=20
GET /api/tickets?q=login&priority=HIGH

❌ BAD:
GET /api/tickets/status/OPEN
POST /api/tickets/search (body: {"query": "login"})
```

**Query Parameter Naming**:
```
✅ GOOD:
?status=OPEN           # Singular for filters
?q=keyword             # Short for common params
?sort=createdAt        # Field name
?order=desc            # asc or desc
?page=0&size=20        # Pagination

❌ BAD:
?ticketStatus=OPEN
?query=keyword
?sortBy=created_at
?pageNumber=1
```

---

## Response Design

### Response Body Structure

**Single Resource**:
```json
{
  "id": 1,
  "title": "Login issue",
  "status": "OPEN",
  "priority": "HIGH",
  "createdAt": "2024-06-01T10:00:00Z"
}
```

**Collection of Resources**:
```json
[
  {
    "id": 1,
    "title": "Login issue",
    "status": "OPEN"
  },
  {
    "id": 2,
    "title": "Database timeout",
    "status": "IN_PROGRESS"
  }
]
```

**Paginated Collections (Future)**:
```json
{
  "content": [
    {"id": 1, "title": "..."},
    {"id": 2, "title": "..."}
  ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

### Field Naming

**Use camelCase**:
```json
✅ GOOD:
{
  "id": 1,
  "createdAt": "2024-06-01T10:00:00Z",
  "updatedAt": "2024-06-01T11:00:00Z"
}

❌ BAD:
{
  "id": 1,
  "created_at": "...",
  "UpdatedAt": "..."
}
```

**Be Consistent**:
```
✅ GOOD: (consistent across all endpoints)
createdAt, updatedAt, closedAt

❌ BAD: (inconsistent)
createdAt, updated_at, ClosedTime
```

### Timestamp Format

**Use ISO 8601 with UTC**:
```json
{
  "createdAt": "2024-06-01T10:00:00Z"  // ISO 8601 with Z (UTC)
}
```

```java
// ✅ GOOD: Jackson serializes Instant as ISO 8601
public record TicketResponse(
    Long id,
    Instant createdAt  // Serialized as "2024-06-01T10:00:00Z"
) {}
```

---

## Error Handling

### Standard Error Response

**Format**:
```json
{
  "status": 404,
  "message": "Ticket with id 123 not found",
  "timestamp": "2024-06-01T10:00:00Z",
  "path": "/api/tickets/123"
}
```

**DTO**:
```java
public record ErrorResponse(
    int status,
    String message,
    Instant timestamp,
    String path
) {}
```

### Validation Error Response

**Format** (400 Bad Request):
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2024-06-01T10:00:00Z",
  "path": "/api/tickets",
  "errors": [
    {
      "field": "title",
      "message": "Title is required"
    },
    {
      "field": "description",
      "message": "Description must not exceed 5000 characters"
    }
  ]
}
```

**DTO**:
```java
public record ValidationErrorResponse(
    int status,
    String message,
    Instant timestamp,
    String path,
    List<FieldError> errors
) {}

public record FieldError(
    String field,
    String message
) {}
```

### Error Message Guidelines

**Be Specific and Helpful**:
```
✅ GOOD:
"Ticket with id 123 not found"
"Title must not exceed 255 characters"
"Cannot transition from OPEN to CLOSED. Allowed transitions: IN_PROGRESS, CANCELLED"

❌ BAD:
"Not found"
"Invalid input"
"Error"
```

**Never Expose Stack Traces**:
```java
// ✅ GOOD: Generic message for unexpected errors
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
    log.error("Unexpected error", ex);  // Log full stack trace internally
    
    return ResponseEntity
        .status(500)
        .body(new ErrorResponse(
            500,
            "An unexpected error occurred",  // Generic message to client
            Instant.now(),
            request.getDescription(false)
        ));
}
```

---

## Versioning

### URL Versioning (Recommended)

**Current** (No version = implicit v1):
```
/api/tickets
/api/tickets/123
```

**Future** (Explicit versioning):
```
/api/v1/tickets
/api/v2/tickets
```

### Header Versioning (Alternative)

```
Accept: application/vnd.tickets.v1+json
Accept: application/vnd.tickets.v2+json
```

---

## Security

### Authentication (Future)

**Bearer Token** (JWT):
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

### CORS Configuration

**Development**:
```properties
spring.web.cors.allowed-origins=http://localhost:3000
spring.web.cors.allowed-methods=GET,POST,PATCH,DELETE
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true
```

**Production**:
```properties
spring.web.cors.allowed-origins=https://tickets.example.com
```

### Input Sanitization

**Always Validate and Sanitize**:
```java
// ✅ GOOD: Validation prevents injection
@NotBlank
@Size(max = 255)
@Pattern(regexp = "^[a-zA-Z0-9 .,!?-]+$", message = "Invalid characters in title")
String title
```

---

## Documentation

### OpenAPI / Swagger (Future)

**Add Springdoc Dependency**:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

**Annotate Controllers**:
```java
@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Ticket management operations")
public class TicketController {
    
    @Operation(
        summary = "Create new ticket",
        description = "Creates a new support ticket with the provided details"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ticket created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping
    public ResponseEntity<TicketDetailResponse> createTicket(
        @Valid @RequestBody CreateTicketRequest request
    ) {
        // Implementation
    }
}
```

---

## Performance

### Response Size

**Use Summary Views for Lists**:
```java
// ✅ GOOD: Lightweight summary for lists
public record TicketSummaryResponse(
    Long id,
    String title,
    TicketStatus status,
    Priority priority,
    Instant createdAt
) {}

// ✅ GOOD: Full details only when requested
public record TicketDetailResponse(
    Long id,
    String title,
    String description,  // Full description
    TicketStatus status,
    Priority priority,
    Instant createdAt,
    Instant updatedAt,
    List<CommentResponse> comments  // Include related data
) {}
```

### Caching (Future)

**Use Cache-Control Headers**:
```java
@GetMapping("/api/tickets/{id}")
public ResponseEntity<TicketDetailResponse> getTicket(@PathVariable Long id) {
    TicketDetailResponse ticket = ticketService.getTicketById(id);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
        .body(ticket);
}
```

### Compression

**Enable GZIP Compression**:
```properties
server.compression.enabled=true
server.compression.mime-types=application/json
server.compression.min-response-size=1024
```

---

## Testing

### MockMvc for Controller Tests

```java
@Test
void createTicket_validRequest_returns201() throws Exception {
    CreateTicketRequest request = new CreateTicketRequest("Title", "Description", Priority.HIGH);
    
    mockMvc.perform(post("/api/tickets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.title").value("Title"))
        .andExpect(jsonPath("$.status").value("OPEN"));
}
```

### Test All Status Codes

```java
// Test 200 OK
@Test
void getTicket_existingId_returns200() { }

// Test 201 Created
@Test
void createTicket_validRequest_returns201() { }

// Test 400 Bad Request
@Test
void createTicket_blankTitle_returns400() { }

// Test 404 Not Found
@Test
void getTicket_nonExistentId_returns404() { }

// Test 422 Unprocessable Entity
@Test
void transitionStatus_invalidTransition_returns422() { }
```

---

## API Design Checklist

Before finalizing an API endpoint:

- [ ] URL uses resource nouns (not verbs)
- [ ] HTTP method matches operation semantics
- [ ] Uses appropriate status codes
- [ ] Request body uses DTOs with validation
- [ ] Response body uses DTOs (never entities)
- [ ] Error responses follow standard format
- [ ] Field names use camelCase
- [ ] Timestamps use ISO 8601 with UTC
- [ ] Documentation includes examples
- [ ] Tests cover success and error cases
- [ ] Follows existing API patterns

---

## Examples

### Complete CRUD Endpoint Example

```java
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    
    // CREATE
    @PostMapping
    public ResponseEntity<TicketDetailResponse> create(
        @Valid @RequestBody CreateTicketRequest request
    ) {
        TicketDetailResponse ticket = ticketService.createTicket(request);
        return ResponseEntity
            .created(URI.create("/api/tickets/" + ticket.id()))
            .body(ticket);
    }
    
    // READ (list)
    @GetMapping
    public ResponseEntity<List<TicketSummaryResponse>> list(
        @RequestParam(required = false) TicketStatus status,
        @RequestParam(required = false) String q
    ) {
        List<TicketSummaryResponse> tickets = ticketService.listTickets(status, q);
        return ResponseEntity.ok(tickets);
    }
    
    // READ (single)
    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailResponse> get(@PathVariable Long id) {
        TicketDetailResponse ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }
    
    // UPDATE
    @PatchMapping("/{id}")
    public ResponseEntity<TicketDetailResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateTicketRequest request
    ) {
        TicketDetailResponse ticket = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(ticket);
    }
    
    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

**Version**: 1.0  
**Last Updated**: 2026-09-23
