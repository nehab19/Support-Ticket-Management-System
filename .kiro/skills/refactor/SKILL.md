---
inclusion: manual
---

# Refactoring Skill

**Skill Name**: Code Refactoring  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Purpose

This skill provides a systematic approach to refactoring code while maintaining correctness, following best practices, and ensuring no regressions. Use this for improving code quality without changing external behavior.

**Scope**: Code restructuring, design improvement, technical debt reduction  
**Authority**: Follows PROJECT_CONSTITUTION.md and coding standards

---

## When to Use This Skill

Invoke this skill (`#refactor`) when:

- Code has duplication that needs extraction
- Methods or classes are too complex
- Code doesn't follow project standards
- Need to improve readability or maintainability
- Preparing code for new features
- Reducing technical debt
- Improving test coverage

---

## What is Refactoring?

**Refactoring**: Improving code structure WITHOUT changing external behavior

### ✅ Refactoring IS

- Extracting methods or classes
- Renaming for clarity
- Removing duplication
- Simplifying complex logic
- Improving structure
- Making tests clearer

### ❌ Refactoring IS NOT

- Adding new features
- Fixing bugs
- Changing behavior
- Optimizing performance (unless structure-related)
- Changing APIs

**Golden Rule**: All tests should pass before AND after refactoring

---

## Refactoring Workflow

### Step 1: Ensure Good Test Coverage

**Before refactoring anything, verify tests exist:**

- [ ] Unit tests cover the code to be refactored
- [ ] Tests are green (all passing)
- [ ] Tests verify behavior, not implementation
- [ ] Integration tests cover critical paths
- [ ] Tests run fast (<5 seconds)

**If tests are missing**:
1. Write tests first (characterization tests)
2. Document current behavior
3. Ensure tests pass
4. THEN refactor

**Why**: Tests are your safety net. Without them, you can't verify correctness.

---

### Step 2: Identify Refactoring Opportunity

**Common code smells:**

### Smell 1: Duplicate Code
```java
// ❌ Duplication
public void updateTitle(Long id, String title) {
    Ticket ticket = ticketRepository.findById(id)
        .orElseThrow(() -> new TicketNotFoundException(id));
    ticket.setTitle(title);
    ticketRepository.save(ticket);
}

public void updateDescription(Long id, String description) {
    Ticket ticket = ticketRepository.findById(id)  // Duplicate!
        .orElseThrow(() -> new TicketNotFoundException(id));
    ticket.setDescription(description);
    ticketRepository.save(ticket);
}
```

### Smell 2: Long Method (>30 lines)
```java
// ❌ Too long, does too much
public TicketDetailResponse createTicket(CreateTicketRequest request) {
    // Validation (10 lines)
    // Business logic (15 lines)
    // Persistence (5 lines)
    // Response mapping (10 lines)
    // Total: 40 lines - too much!
}
```

### Smell 3: Long Parameter List (>3 parameters)
```java
// ❌ Too many parameters
public Ticket createTicket(String title, String description, 
    Priority priority, String assignee, LocalDateTime dueDate, 
    String category, String customer) {
    // ...
}
```

### Smell 4: Complex Conditional
```java
// ❌ Hard to understand
if ((status == OPEN && targetStatus == IN_PROGRESS) ||
    (status == OPEN && targetStatus == CANCELLED) ||
    (status == IN_PROGRESS && targetStatus == RESOLVED) ||
    (status == IN_PROGRESS && targetStatus == CANCELLED) ||
    (status == RESOLVED && targetStatus == CLOSED)) {
    // ...
}
```

### Smell 5: Data Clumps
```java
// ❌ Same parameters always together
public void method1(String firstName, String lastName, String email) { }
public void method2(String firstName, String lastName, String email) { }
public void method3(String firstName, String lastName, String email) { }
```

### Smell 6: Feature Envy
```java
// ❌ Method more interested in other class's data
public boolean isHighPriorityTicket(Ticket ticket) {
    return ticket.getPriority() == Priority.HIGH &&
           ticket.getStatus() != TicketStatus.CLOSED &&
           ticket.getCreatedAt().isBefore(Instant.now().minus(7, ChronoUnit.DAYS));
}
// This should be a method ON Ticket class
```

---

### Step 3: Choose Refactoring Technique

**Common refactoring patterns:**

### Technique 1: Extract Method
**When**: Method too long or has duplicated code
**How**: Extract part into separate method

```java
// Before
public void processTicket(Ticket ticket) {
    // Validation logic (10 lines)
    // Business logic (15 lines)
}

// After
public void processTicket(Ticket ticket) {
    validateTicket(ticket);
    applyBusinessRules(ticket);
}

private void validateTicket(Ticket ticket) {
    // Validation logic (10 lines)
}

private void applyBusinessRules(Ticket ticket) {
    // Business logic (15 lines)
}
```

---

### Technique 2: Extract Class
**When**: Class has too many responsibilities
**How**: Move related fields and methods to new class

```java
// Before
public class Ticket {
    private String title;
    private String description;
    private TicketStatus status;
    
    // Customer fields (should be separate)
    private String customerName;
    private String customerEmail;
    private String customerPhone;
}

// After
public class Ticket {
    private String title;
    private String description;
    private TicketStatus status;
    private Customer customer;  // Extracted
}

public class Customer {
    private String name;
    private String email;
    private String phone;
}
```

---

### Technique 3: Rename
**When**: Name doesn't clearly express intent
**How**: Rename variable, method, or class

```java
// Before - unclear names
public List<Ticket> get(String s) {
    return ticketRepository.findByStatusAndKeywordContaining(s, s);
}

// After - clear names
public List<Ticket> searchTicketsByStatusAndKeyword(
    TicketStatus status, 
    String keyword
) {
    return ticketRepository.findByStatusAndKeywordContaining(status, keyword);
}
```

---

### Technique 4: Introduce Parameter Object
**When**: Many parameters always passed together
**How**: Create class to hold parameters

```java
// Before
public void createTicket(String title, String description, 
    Priority priority, String assignee) {
    // ...
}

// After
public void createTicket(CreateTicketRequest request) {
    // ...
}

public record CreateTicketRequest(
    String title,
    String description,
    Priority priority,
    String assignee
) {}
```

---

### Technique 5: Replace Conditional with Polymorphism
**When**: Type code with complex conditionals
**How**: Use inheritance or strategy pattern

```java
// Before - conditional logic
public double calculateDiscount(Customer customer, double amount) {
    if (customer.getType() == CustomerType.REGULAR) {
        return amount * 0.05;
    } else if (customer.getType() == CustomerType.PREMIUM) {
        return amount * 0.10;
    } else if (customer.getType() == CustomerType.VIP) {
        return amount * 0.20;
    }
    return 0;
}

// After - polymorphism
public interface DiscountStrategy {
    double calculate(double amount);
}

public class RegularDiscount implements DiscountStrategy {
    public double calculate(double amount) {
        return amount * 0.05;
    }
}

public class PremiumDiscount implements DiscountStrategy {
    public double calculate(double amount) {
        return amount * 0.10;
    }
}
```

---

### Technique 6: Simplify Conditional
**When**: Complex boolean expressions
**How**: Extract to well-named methods or variables

```java
// Before - complex condition
if ((status == OPEN && targetStatus == IN_PROGRESS) ||
    (status == OPEN && targetStatus == CANCELLED) ||
    (status == IN_PROGRESS && targetStatus == RESOLVED) ||
    (status == IN_PROGRESS && targetStatus == CANCELLED) ||
    (status == RESOLVED && targetStatus == CLOSED)) {
    // ...
}

// After - clear and maintainable
if (isValidTransition(status, targetStatus)) {
    // ...
}

private boolean isValidTransition(TicketStatus from, TicketStatus to) {
    return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
}
```

---

### Step 4: Make Small, Incremental Changes

**Refactoring in small steps:**

1. **One refactoring at a time** - Don't mix techniques
2. **Run tests after each change** - Immediate feedback
3. **Commit frequently** - Easy to revert if needed
4. **Keep working code** - Never break the build

**Example workflow**:
```bash
# Step 1: Extract method
git add .
git commit -m "refactor: extract ticket validation method"
./mvnw test  # All tests pass ✓

# Step 2: Rename variable
git add .
git commit -m "refactor: rename 't' to 'ticket' for clarity"
./mvnw test  # All tests pass ✓

# Step 3: Extract class
git add .
git commit -m "refactor: extract Customer class from Ticket"
./mvnw test  # All tests pass ✓
```

---

### Step 5: Verify No Behavior Change

**After each refactoring step:**

- [ ] All tests still pass
- [ ] No new warnings or errors
- [ ] Code coverage unchanged or improved
- [ ] API contracts unchanged
- [ ] External behavior identical

**Verification commands**:
```bash
# Run all tests
./mvnw test

# Check for compilation issues
./mvnw compile

# Run integration tests
./mvnw verify

# Check test coverage
./mvnw jacoco:report
```

**If tests fail**:
1. **Stop immediately**
2. Review the change
3. Fix the issue or revert
4. Don't proceed until tests pass

---

### Step 6: Improve Code Quality

**After structural refactoring, polish:**

### Add Documentation
```java
// Before - no documentation
public boolean isValid(String s, int n) {
    return s != null && s.length() <= n;
}

// After - documented
/**
 * Validates that a string is non-null and within maximum length.
 *
 * @param value the string to validate
 * @param maxLength maximum allowed length
 * @return true if valid, false otherwise
 */
public boolean isValidString(String value, int maxLength) {
    return value != null && value.length() <= maxLength;
}
```

### Improve Naming
```java
// Before - cryptic
List<Ticket> ts = repo.findAll();
for (Ticket t : ts) {
    if (t.getS() == Status.O) {
        // ...
    }
}

// After - clear
List<Ticket> openTickets = ticketRepository.findByStatus(TicketStatus.OPEN);
for (Ticket ticket : openTickets) {
    processOpenTicket(ticket);
}
```

### Add Constants
```java
// Before - magic numbers
if (ticket.getAge() > 7) {
    escalate(ticket);
}

// After - named constants
private static final int ESCALATION_THRESHOLD_DAYS = 7;

if (ticket.getAge() > ESCALATION_THRESHOLD_DAYS) {
    escalate(ticket);
}
```

---

### Step 7: Review and Clean Up

**Final review checklist:**

**Code Quality**:
- [ ] No duplication
- [ ] Methods <30 lines
- [ ] Classes <300 lines
- [ ] Clear, descriptive names
- [ ] Single responsibility per class/method
- [ ] Proper access modifiers

**Standards Compliance**:
- [ ] Follows .kiro/steering/java-springboot.md
- [ ] Constructor injection (not field)
- [ ] DTOs for API contracts
- [ ] Proper exception handling
- [ ] Follows naming conventions

**Testing**:
- [ ] All tests pass
- [ ] No test changes needed (good sign!)
- [ ] Coverage maintained or improved
- [ ] Tests still readable

**Documentation**:
- [ ] JavaDoc updated if needed
- [ ] Inline comments for complex logic
- [ ] Commit messages descriptive

---

## Refactoring Examples

### Example 1: Extract Method (Remove Duplication)

**Before**:
```java
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketDetailResponse updateTitle(Long id, String title) {
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new TicketNotFoundException(id));
        ticket.setTitle(title);
        Ticket saved = ticketRepository.save(ticket);
        return TicketMapper.toDetailResponse(saved);
    }

    public TicketDetailResponse updateDescription(Long id, String description) {
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new TicketNotFoundException(id));
        ticket.setDescription(description);
        Ticket saved = ticketRepository.save(ticket);
        return TicketMapper.toDetailResponse(saved);
    }
}
```

**After**:
```java
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketDetailResponse updateTitle(Long id, String title) {
        return updateTicketField(id, ticket -> ticket.setTitle(title));
    }

    public TicketDetailResponse updateDescription(Long id, String description) {
        return updateTicketField(id, ticket -> ticket.setDescription(description));
    }

    private TicketDetailResponse updateTicketField(
        Long id, 
        Consumer<Ticket> updater
    ) {
        Ticket ticket = findTicketById(id);
        updater.accept(ticket);
        Ticket saved = ticketRepository.save(ticket);
        return TicketMapper.toDetailResponse(saved);
    }

    private Ticket findTicketById(Long id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new TicketNotFoundException(id));
    }
}
```

**Benefits**:
- Eliminated duplication
- Easier to maintain
- Consistent error handling

---

### Example 2: Simplify Complex Conditional

**Before**:
```java
public void validateTransition(TicketStatus current, TicketStatus target) {
    if ((current == OPEN && target == IN_PROGRESS) ||
        (current == OPEN && target == CANCELLED) ||
        (current == IN_PROGRESS && target == RESOLVED) ||
        (current == IN_PROGRESS && target == CANCELLED) ||
        (current == RESOLVED && target == CLOSED)) {
        // Valid transition
        return;
    }
    throw new InvalidStatusTransitionException(current, target);
}
```

**After**:
```java
private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
    OPEN, Set.of(IN_PROGRESS, CANCELLED),
    IN_PROGRESS, Set.of(RESOLVED, CANCELLED),
    RESOLVED, Set.of(CLOSED)
);

public void validateTransition(TicketStatus current, TicketStatus target) {
    if (!isValidTransition(current, target)) {
        throw new InvalidStatusTransitionException(current, target);
    }
}

private boolean isValidTransition(TicketStatus current, TicketStatus target) {
    return ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target);
}
```

**Benefits**:
- More maintainable (add transitions easily)
- Clearer intent
- Data-driven approach

---

### Example 3: Extract Class

**Before**:
```java
public class Ticket {
    private Long id;
    private String title;
    private String description;
    private TicketStatus status;
    private Priority priority;
    
    // Customer data mixed in
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    
    // Many methods dealing with customer data
    public String getCustomerFullName() { }
    public void updateCustomerContact(String email, String phone) { }
}
```

**After**:
```java
public class Ticket {
    private Long id;
    private String title;
    private String description;
    private TicketStatus status;
    private Priority priority;
    private Customer customer;  // Extracted
}

public class Customer {
    private String name;
    private String email;
    private String phone;
    private String address;
    
    public String getFullName() { }
    public void updateContact(String email, String phone) { }
}
```

**Benefits**:
- Clear separation of concerns
- Customer logic in Customer class
- Easier to test

---

## Refactoring Anti-Patterns

### ❌ DON'T: Refactor and Add Features Together

```java
// Bad: Mixing refactoring with new feature
public void updateTicket(Long id, UpdateRequest request) {
    // Refactoring: Extract method
    Ticket ticket = findTicket(id);
    
    // New feature: Add notification (DON'T MIX!)
    sendNotification(ticket);
    
    ticket.update(request);
}
```

**Do instead**: Separate commits
1. Commit: Refactor (extract method)
2. Commit: Add feature (notifications)

---

### ❌ DON'T: Refactor Without Tests

```java
// Bad: No tests exist
public void complexMethod() {
    // 100 lines of complex logic
    // No tests covering this
}

// Refactoring without tests is risky!
```

**Do instead**: Write tests first, then refactor

---

### ❌ DON'T: Big Bang Refactoring

```java
// Bad: Refactoring entire codebase at once
// - Renamed 50 classes
// - Restructured packages
// - Changed all method signatures
// - All in one commit
```

**Do instead**: Incremental refactoring
- Small, focused changes
- Frequent commits
- Tests pass after each step

---

## When NOT to Refactor

**Avoid refactoring when:**

- Tests don't exist (write tests first)
- Code is about to be deleted
- Near a deadline (technical debt is okay temporarily)
- Working in unfamiliar codebase (understand first)
- No clear improvement (refactor with purpose)

---

## Refactoring Checklist

### Before Starting
- [ ] Tests exist and pass
- [ ] Understand current behavior
- [ ] Identify specific smell or issue
- [ ] Choose appropriate technique
- [ ] Have clear goal

### During Refactoring
- [ ] Make one change at a time
- [ ] Run tests after each change
- [ ] Commit frequently
- [ ] Keep code working

### After Refactoring
- [ ] All tests pass
- [ ] Behavior unchanged
- [ ] Code is clearer
- [ ] Duplication removed
- [ ] Standards compliant

---

## Commit Message Format

```
refactor: [specific change]

[Optional: Explain why this improves the code]

- [Detail 1]
- [Detail 2]

No behavior change. All tests pass.
```

**Examples**:
```
refactor: extract ticket validation into separate method

Reduces duplication across create and update methods.

refactor: rename 't' to 'ticket' for clarity

Improves readability in TicketService methods.

refactor: introduce TicketStateMachine class

Extracts state transition logic from TicketService for
better separation of concerns and testability.
```

---

**Skill Version**: 1.0  
**Last Updated**: 2026-09-23  
**Related Documents**: .kiro/steering/java-springboot.md, PROJECT_CONSTITUTION.md
