# UI Flow Document

## Overview

This document defines the user interface flows and component interactions for the Support Ticket Management System frontend (React/Next.js).

---

## Page Structure

### 1. Ticket List Page

**Route**: `/` (index)

**Purpose**: Display all tickets with search, filter, and create capabilities

**Layout**:
```
┌─────────────────────────────────────────────────────┐
│  [Logo] Support Ticket System         [Create] btn  │
├─────────────────────────────────────────────────────┤
│  [Search: _______________]  [Filter: All ▼]        │
├─────────────────────────────────────────────────────┤
│  ┌───────────────────────────────────────────────┐ │
│  │ #1  Login page throws 500        HIGH  OPEN   │ │
│  │     alice                    2024-06-01 10:00 │ │
│  └───────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────┐ │
│  │ #2  Database timeout         CRITICAL  IN_PRO │ │
│  │     bob                      2024-06-01 09:30 │ │
│  └───────────────────────────────────────────────┘ │
│  ...                                               │
└─────────────────────────────────────────────────────┘
```

**Components**:
- `SearchBar`: Keyword input with debouncing
- `StatusFilter`: Dropdown for status filtering
- `TicketTable` or `TicketCard`: List display
- `CreateTicketButton`: Opens create modal/drawer

---

### 2. Ticket Detail Page

**Route**: `/tickets/[id]`

**Purpose**: View full ticket details, comments, and perform actions

**Layout**:
```
┌─────────────────────────────────────────────────────┐
│  [← Back]                                           │
├─────────────────────────────────────────────────────┤
│  #1  Login page throws 500                          │
│  Status: OPEN        Priority: HIGH                 │
│  Assignee: alice     Created: 2024-06-01 10:00     │
│                                                     │
│  [Start Work] [Cancel]                             │
├─────────────────────────────────────────────────────┤
│  Description:                                       │
│  Reproducible on Chrome 124. Steps to reproduce:   │
│  ...                                                │
├─────────────────────────────────────────────────────┤
│  Comments:                                          │
│  ┌─────────────────────────────────────────────┐  │
│  │ alice @ 2024-06-01 11:00                     │  │
│  │ Investigating now. Checking server logs.     │  │
│  └─────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────┐  │
│  │ bob @ 2024-06-01 12:00                       │  │
│  │ Found the root cause. Deploying fix.         │  │
│  └─────────────────────────────────────────────┘  │
│                                                     │
│  [Add Comment]                                      │
│  ┌─────────────────────────────────────────────┐  │
│  │ Author: [_______]                            │  │
│  │ Comment: [________________________]          │  │
│  │          [________________________]          │  │
│  │                          [Submit] [Cancel]   │  │
│  └─────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

**Components**:
- `TicketDetailHeader`: Title, status, priority, assignee, timestamps
- `StatusTransitionPanel`: Valid action buttons
- `TicketDescriptionSection`: Full description
- `CommentList`: Chronological comments
- `CommentForm`: Add new comment

---

## User Flows

### Flow 1: Create Ticket

```
User clicks "Create" button
  ↓
Modal/Drawer opens with form
  ↓
User fills in:
  - Title (required)
  - Description (required)
  - Priority (optional, defaults to MEDIUM)
  ↓
User clicks "Create"
  ↓
Frontend validates input (client-side)
  ↓
POST /api/tickets
  ↓
Backend validates and creates ticket
  ↓
Success (201):
  - Modal closes
  - Ticket list refreshes
  - New ticket appears at top
  - Success notification shown
  ↓
Error (400):
  - Inline field errors displayed
  - User can correct and retry
```

**Validation Rules**:
- Title: 1-255 characters, not blank
- Description: Not blank
- Priority: One of LOW, MEDIUM, HIGH, CRITICAL (or empty)

**Error Handling**:
- Display field-level errors next to inputs
- Keep modal open for correction
- Do not clear valid fields

---

### Flow 2: Search Tickets

```
User types in search box
  ↓
Debounce 300ms
  ↓
GET /api/tickets?q={keyword}
  ↓
Success (200):
  - Ticket list updates with matching tickets
  - Show count: "5 results for 'login'"
  ↓
No results:
  - Show "No tickets found for 'login'"
  - Option to clear search
  ↓
Error (400 - blank keyword):
  - Show error message
  - Disable search button
```

**Debouncing**: Wait 300ms after user stops typing before sending request

**Empty State**: Show all tickets when search is cleared

---

### Flow 3: Filter by Status

```
User clicks status filter dropdown
  ↓
User selects status (e.g., "IN_PROGRESS")
  ↓
GET /api/tickets?status=IN_PROGRESS
  ↓
Success (200):
  - Ticket list updates with matching tickets
  - Filter badge shown: "Filtered by: IN_PROGRESS [×]"
  ↓
User clicks [×] to clear filter
  ↓
GET /api/tickets
  ↓
All tickets displayed
```

**Status Options**:
- All (no filter)
- OPEN
- IN_PROGRESS
- RESOLVED
- CLOSED
- CANCELLED

---

### Flow 4: View Ticket Details

```
User clicks ticket in list
  ↓
Navigate to /tickets/[id]
  ↓
GET /api/tickets/{id}
  ↓
Success (200):
  - Render ticket details
  - Render comments (if any)
  - Show valid status transition actions
  ↓
Error (404):
  - Show "Ticket not found" page
  - Button to return to ticket list
```

---

### Flow 5: Transition Ticket Status

```
User views ticket detail page
  ↓
UI shows only valid actions based on current status
  - OPEN: [Start Work] [Cancel]
  - IN_PROGRESS: [Mark Resolved] [Cancel]
  - RESOLVED: [Close Ticket]
  - CLOSED/CANCELLED: (no actions)
  ↓
User clicks action button (e.g., "Start Work")
  ↓
Confirmation modal (optional for destructive actions)
  ↓
User confirms
  ↓
PATCH /api/tickets/{id}/status
  body: { "status": "IN_PROGRESS" }
  ↓
Success (200):
  - Ticket detail refreshes
  - Status badge updates
  - Valid actions update
  - Success notification: "Ticket moved to IN_PROGRESS"
  ↓
Error (422 - invalid transition):
  - Error notification: "Cannot transition from OPEN to CLOSED"
  - Ticket remains unchanged
  ↓
Error (404):
  - Error notification: "Ticket not found"
  - Redirect to ticket list
```

**Valid Actions by Status**:
- **OPEN**: Start Work (→ IN_PROGRESS), Cancel (→ CANCELLED)
- **IN_PROGRESS**: Mark Resolved (→ RESOLVED), Cancel (→ CANCELLED)
- **RESOLVED**: Close Ticket (→ CLOSED)
- **CLOSED**: (no actions)
- **CANCELLED**: (no actions)

---

### Flow 6: Update Ticket Fields

```
User views ticket detail page
  ↓
User clicks "Edit" button
  ↓
Form fields become editable:
  - Title
  - Description
  - Priority
  - Assignee
  ↓
User modifies fields
  ↓
User clicks "Save"
  ↓
PATCH /api/tickets/{id}
  body: { "title": "...", "assignee": "..." }
  ↓
Success (200):
  - Form reverts to read-only
  - Updated values displayed
  - Success notification: "Ticket updated"
  ↓
Error (400 - validation):
  - Inline field errors displayed
  - Form remains editable
  - User can correct and retry
  ↓
User clicks "Cancel"
  ↓
Form reverts to read-only
  - Changes discarded
```

---

### Flow 7: Add Comment

```
User views ticket detail page
  ↓
User scrolls to comment section
  ↓
User clicks "Add Comment" or form is always visible
  ↓
User fills in:
  - Author (required)
  - Body (required)
  ↓
User clicks "Submit"
  ↓
POST /api/tickets/{id}/comments
  body: { "author": "...", "body": "..." }
  ↓
Success (201):
  - Comment appended to list
  - Form clears
  - Scroll to new comment
  - Success notification: "Comment added"
  ↓
Error (400 - validation):
  - Inline field errors displayed
  - Form remains filled
  - User can correct and retry
  ↓
Error (404 - ticket not found):
  - Error notification: "Ticket not found"
  - Redirect to ticket list
```

---

## Component Specifications

### SearchBar Component

**Props**:
- `onSearch: (keyword: string) => void`
- `placeholder?: string`

**State**:
- `keyword: string`

**Behavior**:
- Debounce input (300ms)
- Call `onSearch` with keyword
- Clear button when keyword non-empty

---

### StatusFilter Component

**Props**:
- `onFilter: (status: TicketStatus | null) => void`
- `currentStatus?: TicketStatus | null`

**Behavior**:
- Dropdown with all status options + "All"
- Call `onFilter` when selection changes
- Show selected status as badge

---

### TicketTable Component

**Props**:
- `tickets: TicketSummary[]`
- `onTicketClick: (id: number) => void`

**Display**:
- Table with columns: ID, Title, Priority, Status, Assignee, Created
- Clickable rows
- Empty state when no tickets

---

### StatusTransitionPanel Component

**Props**:
- `currentStatus: TicketStatus`
- `onTransition: (newStatus: TicketStatus) => Promise<void>`

**Behavior**:
- Show only valid transitions for current status
- Disable during API call (loading state)
- Handle success/error responses

**Valid Transitions**:
```typescript
const validTransitions: Record<TicketStatus, TicketStatus[]> = {
  OPEN: ['IN_PROGRESS', 'CANCELLED'],
  IN_PROGRESS: ['RESOLVED', 'CANCELLED'],
  RESOLVED: ['CLOSED'],
  CLOSED: [],
  CANCELLED: []
};
```

---

### CommentForm Component

**Props**:
- `ticketId: number`
- `onCommentAdded: (comment: Comment) => void`

**State**:
- `author: string`
- `body: string`
- `loading: boolean`
- `errors: { author?: string; body?: string }`

**Behavior**:
- Validate required fields
- Call POST /api/tickets/{id}/comments
- Clear form on success
- Show inline errors on failure

---

### ErrorNotification Component

**Props**:
- `message: string`
- `type: 'error' | 'warning' | 'success'`
- `onClose: () => void`

**Behavior**:
- Toast notification
- Auto-dismiss after 5 seconds
- Manual dismiss button

---

## State Management

### Client-Side State

**Ticket List Page**:
- `tickets: TicketSummary[]`
- `searchKeyword: string`
- `statusFilter: TicketStatus | null`
- `loading: boolean`
- `error: string | null`

**Ticket Detail Page**:
- `ticket: TicketDetail | null`
- `loading: boolean`
- `error: string | null`

**Global State** (optional):
- `user: User | null` (future: authentication)

---

## Responsive Design

### Mobile (<768px)
- Stack layout
- Card-based ticket list
- Full-screen modals
- Collapsible sections

### Tablet (768px-1024px)
- Two-column layout where appropriate
- Drawer-based modals

### Desktop (>1024px)
- Multi-column layout
- Modal dialogs
- Side-by-side views

---

## Accessibility

### Keyboard Navigation
- All interactive elements tabbable
- Keyboard shortcuts for common actions
- Escape to close modals

### Screen Reader Support
- ARIA labels on all buttons
- Live regions for dynamic updates
- Semantic HTML elements

### Color Contrast
- WCAG AA compliance
- Status badges with icons (not color-only)

---

## Future Enhancements

- Real-time updates (WebSocket)
- Bulk actions (multi-select tickets)
- Advanced filtering (date range, assignee)
- Ticket templates
- File attachments
- Email notifications

---

**Version**: 1.0  
**Last Updated**: 2026-09-23
