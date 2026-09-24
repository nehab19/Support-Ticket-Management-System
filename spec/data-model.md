# Data Model Document

## Overview

The data model consists of two primary entities: **Ticket** and **Comment**, with a one-to-many relationship. All timestamps use `TIMESTAMP WITH TIME ZONE` for timezone awareness.

---

## Entity: Ticket

### Database Table: `ticket`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `BIGINT` | PRIMARY KEY, GENERATED | Unique ticket identifier |
| `title` | `VARCHAR(255)` | NOT NULL | Ticket summary/title |
| `description` | `TEXT` | NOT NULL | Detailed description |
| `priority` | `VARCHAR(20)` | NOT NULL, DEFAULT 'MEDIUM' | Urgency level |
| `status` | `VARCHAR(20)` | NOT NULL, DEFAULT 'OPEN' | Lifecycle state |
| `assignee` | `VARCHAR(255)` | NULL | Assigned user identifier |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | NOT NULL, DEFAULT NOW() | Creation timestamp |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | NOT NULL, DEFAULT NOW() | Last update timestamp |

### Enums

**Priority** (stored as STRING):
- `LOW`
- `MEDIUM` (default)
- `HIGH`
- `CRITICAL`

**TicketStatus** (stored as STRING):
- `OPEN` (initial state)
- `IN_PROGRESS`
- `RESOLVED`
- `CLOSED` (terminal)
- `CANCELLED` (terminal)

### Indexes

- **idx_ticket_status**: `ticket(status)` — Supports status filter queries
- **idx_ticket_created_at**: `ticket(created_at DESC)` — Supports default ordering

---

## Entity: Comment

### Database Table: `comment`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `BIGINT` | PRIMARY KEY, GENERATED | Unique comment identifier |
| `ticket_id` | `BIGINT` | FK → ticket(id), NOT NULL, ON DELETE CASCADE | Associated ticket |
| `author` | `VARCHAR(255)` | NOT NULL | Comment author identifier |
| `body` | `TEXT` | NOT NULL | Comment content |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | NOT NULL, DEFAULT NOW() | Creation timestamp |

### Foreign Key Constraints

- **fk_comment_ticket**: `comment(ticket_id)` → `ticket(id)`
  - **On Delete**: CASCADE (deleting ticket deletes all comments)

### Indexes

- **idx_comment_ticket_id**: `comment(ticket_id)` — Supports comment lookups by ticket

---

## Relationships

### Ticket ↔ Comment (One-to-Many)

```
Ticket (1) ────────────── (*) Comment
```

- One ticket can have zero or more comments
- Each comment belongs to exactly one ticket
- Comments are ordered by `created_at ASC` when retrieved
- Deleting a ticket cascades to delete all associated comments

---

## JPA Entity Mapping

### Ticket Entity (Java)

```java
@Entity
@Table(name = "ticket", indexes = {
    @Index(name = "idx_ticket_status", columnList = "status"),
    @Index(name = "idx_ticket_created_at", columnList = "created_at")
})
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.OPEN;
    
    @Column(length = 255)
    private String assignee;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @OneToMany(
        mappedBy = "ticket",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("createdAt ASC")
    private List<Comment> comments = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Getters, setters, helper methods...
}
```

### Comment Entity (Java)

```java
@Entity
@Table(name = "comment", indexes = {
    @Index(name = "idx_comment_ticket_id", columnList = "ticket_id")
})
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    
    @Column(nullable = false, length = 255)
    private String author;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
    
    // Getters, setters...
}
```

---

## Database Migration Strategy

### Flyway Migrations

**Location**: `src/main/resources/db/migration/`

**Naming Convention**: `V{version}__{description}.sql`

### Migration V1: Create Ticket Table

**File**: `V1__create_ticket_table.sql`

```sql
CREATE TABLE ticket (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    assignee VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_created_at ON ticket(created_at DESC);
```

### Migration V2: Create Comment Table

**File**: `V2__create_comment_table.sql`

```sql
CREATE TABLE comment (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    author VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id)
        REFERENCES ticket(id) ON DELETE CASCADE
);

CREATE INDEX idx_comment_ticket_id ON comment(ticket_id);
```

---

## Query Patterns

### Common Queries

#### Find all tickets ordered by creation date (descending)
```sql
SELECT * FROM ticket ORDER BY created_at DESC;
```

#### Find tickets by status
```sql
SELECT * FROM ticket WHERE status = :status ORDER BY created_at DESC;
```

#### Keyword search (case-insensitive)
```sql
SELECT * FROM ticket
WHERE LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%'))
   OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%'))
ORDER BY created_at DESC;
```

#### Combined status filter + keyword search
```sql
SELECT * FROM ticket
WHERE status = :status
  AND (LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%')))
ORDER BY created_at DESC;
```

#### Get ticket with all comments
```sql
SELECT t.*, c.*
FROM ticket t
LEFT JOIN comment c ON c.ticket_id = t.id
WHERE t.id = :ticketId
ORDER BY c.created_at ASC;
```

---

## Database Configuration

### Production (PostgreSQL)

**Configuration File**: `application-prod.properties`

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

**Environment Variables** (required):
- `DB_URL`: `jdbc:postgresql://localhost:5432/supporttickets`
- `DB_USERNAME`: Database user
- `DB_PASSWORD`: Database password

### Test (H2)

**Configuration File**: `application-test.properties`

```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

---

## Future Enhancements

### Indexing
- **Full-Text Search**: Add PostgreSQL GIN index on `tsvector` column for `title` and `description`
- **Composite Index**: `ticket(status, created_at DESC)` for common query pattern

### Schema Evolution
- Add `resolved_at` timestamp (when status becomes RESOLVED)
- Add `closed_at` timestamp (when status becomes CLOSED)
- Add `attachments` table for file uploads
- Add `ticket_history` table for audit trail

### Partitioning
- Partition `ticket` table by `created_at` for large-scale deployments
- Archive old CLOSED/CANCELLED tickets to separate table

---

**Version**: 1.0  
**Last Updated**: 2026-09-23
