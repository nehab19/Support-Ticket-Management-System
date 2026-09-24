# State Machine Document

## Overview

The ticket state machine strictly governs all ticket status transitions. Only explicitly allowed transitions are permitted; all others are rejected with HTTP 422.

---

## States

| State | Type | Description |
|-------|------|-------------|
| `OPEN` | Initial | Ticket created, awaiting assignment |
| `IN_PROGRESS` | Active | Work has started on the ticket |
| `RESOLVED` | Active | Work completed, awaiting verification |
| `CLOSED` | Terminal | Ticket finalized and archived |
| `CANCELLED` | Terminal | Ticket abandoned without completion |

### State Characteristics

**Initial State**:
- `OPEN` — All newly created tickets start in this state

**Active States**:
- `OPEN`, `IN_PROGRESS`, `RESOLVED` — Can transition to other states

**Terminal States**:
- `CLOSED`, `CANCELLED` — No outbound transitions allowed; once reached, ticket cannot change status

---

## Allowed Transitions

```
                     ┌──────────────┐
                     │     OPEN     │ (Initial)
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

### Transition Table

| From | To | Description | HTTP Status |
|------|----|-----------|----|
| `OPEN` | `IN_PROGRESS` | Start work | 200 |
| `OPEN` | `CANCELLED` | Cancel before work starts | 200 |
| `IN_PROGRESS` | `RESOLVED` | Complete work | 200 |
| `IN_PROGRESS` | `CANCELLED` | Cancel during work | 200 |
| `RESOLVED` | `CLOSED` | Verify and finalize | 200 |

**All other transitions are FORBIDDEN** and return HTTP 422.

---

## Forbidden Transitions

### From OPEN
- `OPEN` → `RESOLVED` — Must go through `IN_PROGRESS`
- `OPEN` → `CLOSED` — Must go through `IN_PROGRESS` and `RESOLVED`

### From IN_PROGRESS
- `IN_PROGRESS` → `OPEN` — Cannot revert to initial state
- `IN_PROGRESS` → `CLOSED` — Must go through `RESOLVED`

### From RESOLVED
- `RESOLVED` → `OPEN` — Cannot revert to initial state
- `RESOLVED` → `IN_PROGRESS` — Cannot reopen work
- `RESOLVED` → `CANCELLED` — Use `CLOSED` instead

### From CLOSED (Terminal)
- `CLOSED` → ANY — No transitions allowed from terminal state

### From CANCELLED (Terminal)
- `CANCELLED` → ANY — No transitions allowed from terminal state

---

## Implementation

### TicketStateMachine Component

**Location**: `com.example.supportticket.service.TicketStateMachine`

**Responsibility**: Validates status transitions before they are persisted

**Interface**:
```java
@Component
public class TicketStateMachine {
    
    /**
     * Check if a transition is valid.
     * 
     * @param from Current status
     * @param to Requested status
     * @return true if transition is allowed, false otherwise
     */
    public boolean isValidTransition(TicketStatus from, TicketStatus to);
    
    /**
     * Validate a transition and throw exception if invalid.
     * 
     * @param from Current status
     * @param to Requested status
     * @throws InvalidStatusTransitionException if transition not allowed
     */
    public void validateTransition(TicketStatus from, TicketStatus to);
    
    /**
     * Get all valid next states from a given state.
     * 
     * @param from Current status
     * @return Set of allowed next statuses
     */
    public Set<TicketStatus> getAllowedNextStates(TicketStatus from);
}
```

### Transition Map Implementation

```java
private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
    TicketStatus.OPEN, Set.of(
        TicketStatus.IN_PROGRESS,
        TicketStatus.CANCELLED
    ),
    TicketStatus.IN_PROGRESS, Set.of(
        TicketStatus.RESOLVED,
        TicketStatus.CANCELLED
    ),
    TicketStatus.RESOLVED, Set.of(
        TicketStatus.CLOSED
    ),
    TicketStatus.CLOSED, Set.of(),      // Terminal: no transitions
    TicketStatus.CANCELLED, Set.of()    // Terminal: no transitions
);

public boolean isValidTransition(TicketStatus from, TicketStatus to) {
    if (from == to) {
        return false;  // No self-transitions
    }
    return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
}
```

---

## Error Handling

### Invalid Transition Exception

**Exception Class**: `InvalidStatusTransitionException`

**HTTP Status**: 422 Unprocessable Entity

**Error Response Format**:
```json
{
  "status": 422,
  "message": "Cannot transition from OPEN to CLOSED. Allowed transitions from OPEN: IN_PROGRESS, CANCELLED"
}
```

### Exception Implementation

```java
public class InvalidStatusTransitionException extends RuntimeException {
    private final TicketStatus from;
    private final TicketStatus to;
    
    public InvalidStatusTransitionException(TicketStatus from, TicketStatus to) {
        super(formatMessage(from, to));
        this.from = from;
        this.to = to;
    }
    
    private static String formatMessage(TicketStatus from, TicketStatus to) {
        Set<TicketStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        String allowedStr = allowed.isEmpty() 
            ? "none (terminal state)" 
            : allowed.stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));
        
        return String.format(
            "Cannot transition from %s to %s. Allowed transitions from %s: %s",
            from, to, from, allowedStr
        );
    }
    
    public TicketStatus getFrom() { return from; }
    public TicketStatus getTo() { return to; }
}
```

---

## Service Integration

### TicketService Usage

```java
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketStateMachine stateMachine;
    
    @Transactional
    public TicketDetailResponse transitionStatus(Long ticketId, TicketStatus newStatus) {
        // Fetch ticket
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));
        
        TicketStatus currentStatus = ticket.getStatus();
        
        // Validate transition BEFORE persisting
        stateMachine.validateTransition(currentStatus, newStatus);
        
        // Transition is valid; update status
        ticket.setStatus(newStatus);
        Ticket updated = ticketRepository.save(ticket);
        
        return toDetailResponse(updated);
    }
}
```

### Controller Endpoint

```java
@PatchMapping("/{id}/status")
public ResponseEntity<TicketDetailResponse> transitionStatus(
    @PathVariable Long id,
    @Valid @RequestBody StatusTransitionRequest request
) {
    TicketDetailResponse response = ticketService.transitionStatus(id, request.status());
    return ResponseEntity.ok(response);
}
```

---

## Frontend Integration

### UI State Management

The frontend MUST:
1. Fetch the current ticket status
2. Query `TicketStateMachine.getAllowedNextStates(currentStatus)` (or maintain client-side copy)
3. Display only valid transition actions to the user
4. Handle 422 errors gracefully when invalid transitions are attempted

### Example UI Logic (TypeScript)

```typescript
const allowedTransitions: Record<TicketStatus, TicketStatus[]> = {
  OPEN: ['IN_PROGRESS', 'CANCELLED'],
  IN_PROGRESS: ['RESOLVED', 'CANCELLED'],
  RESOLVED: ['CLOSED'],
  CLOSED: [],
  CANCELLED: []
};

function getValidActions(currentStatus: TicketStatus): TicketStatus[] {
  return allowedTransitions[currentStatus] || [];
}

// In component
const validActions = getValidActions(ticket.status);

return (
  <div>
    {validActions.map(action => (
      <Button key={action} onClick={() => transitionTo(action)}>
        {action}
      </Button>
    ))}
  </div>
);
```

---

## Testing Strategy

### Unit Tests

**Test Class**: `TicketStateMachineTest`

**Coverage**: All 25 possible (from, to) pairs

**Approach**:
- Test each of the 5 allowed transitions → expect `isValidTransition() == true`
- Test each of the 20 forbidden transitions → expect `isValidTransition() == false`
- Test self-transitions → expect `isValidTransition() == false`

### Property-Based Test

**Test Class**: `StateMachinePropertyTest`

**Property**: For any (from, to) status pair, if the pair is in the allowed set, the transition succeeds with HTTP 200; otherwise, it returns HTTP 422 with both status names in the error message.

**Iterations**: 100+

---

## Audit and Monitoring

### Future Enhancements

**Status Transition Audit Log**:
- Record all status transitions in `ticket_status_history` table
- Track: `ticket_id`, `from_status`, `to_status`, `changed_by`, `changed_at`

**Metrics**:
- Count of transitions by (from, to) pair
- Average time in each state
- Failed transition attempts (422 responses)

---

## Business Rules

### Why These Transitions?

**OPEN → IN_PROGRESS**:
- Agent accepts and starts work on ticket

**OPEN → CANCELLED**:
- Ticket closed before any work (duplicate, invalid, spam)

**IN_PROGRESS → RESOLVED**:
- Work completed, awaiting verification

**IN_PROGRESS → CANCELLED**:
- Work abandoned mid-stream (no longer needed, out of scope)

**RESOLVED → CLOSED**:
- Work verified and ticket archived

### Why NOT Other Transitions?

**OPEN → RESOLVED**:
- Tickets must go through IN_PROGRESS to track when work started

**OPEN → CLOSED**:
- Tickets must be resolved before closing (ensures verification step)

**IN_PROGRESS → CLOSED**:
- Must go through RESOLVED for verification

**RESOLVED → IN_PROGRESS**:
- Once work is complete, it shouldn't be reopened (create new ticket if needed)

**Any transition FROM terminal states**:
- CLOSED and CANCELLED are final states; reopening requires creating a new ticket

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-09-23 | Initial state machine definition |

---

**Version**: 1.0  
**Last Updated**: 2026-09-23
