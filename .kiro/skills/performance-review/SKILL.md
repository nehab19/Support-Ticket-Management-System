---
inclusion: manual
---

# Performance Review Skill

**Skill Name**: Performance Review  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Purpose

This skill provides a systematic approach to identifying, analyzing, and optimizing performance issues in the Support Ticket Management System. Use this for investigating slow queries, high memory usage, or inefficient algorithms.

**Scope**: Performance analysis, optimization, and benchmarking  
**Authority**: Follows PROJECT_CONSTITUTION.md while maintaining code quality

---

## When to Use This Skill

Invoke this skill (`#performance-review`) when:

- API responses are slow (>1 second)
- Database queries are inefficient
- Memory usage is high
- Application startup is slow
- Need to optimize existing code
- Preparing for production load
- Investigating performance regression

---

## Performance Review Workflow

### Step 1: Establish Baseline

**Before optimizing, measure current performance:**

**Key Metrics**:
- [ ] Response times (API endpoints)
- [ ] Database query execution times
- [ ] Memory usage (heap, non-heap)
- [ ] CPU utilization
- [ ] Thread pool usage
- [ ] Database connection pool usage

**Tools**:
```bash
# Spring Boot Actuator metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# JVM memory
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Database metrics
curl http://localhost:8080/actuator/metrics/hikaricp.connections
```

**Document baseline**:
```markdown
## Performance Baseline

**Date**: 2026-09-23
**Environment**: Local (H2)

### API Response Times
- GET /api/tickets: 45ms
- POST /api/tickets: 120ms
- GET /api/tickets/{id}: 25ms
- PATCH /api/tickets/{id}/status: 80ms

### Database Queries
- findAll(): 350ms (1000 records)
- findById(): 15ms
- save(): 45ms

### Memory
- Heap used: 256 MB
- Heap committed: 512 MB
```

---

### Step 2: Identify Performance Issues

**Common performance problems:**

### Issue 1: N+1 Query Problem

**Symptom**: Many small queries instead of one optimized query

```java
// ❌ N+1 Problem
public List<TicketDetailResponse> getAllTickets() {
    List<Ticket> tickets = ticketRepository.findAll();  // 1 query
    return tickets.stream()
        .map(ticket -> {
            // N queries (one per ticket)
            List<Comment> comments = commentRepository.findByTicketId(ticket.getId());
            return mapToResponse(ticket, comments);
        })
        .toList();
}
```

**Detection**:
- Enable SQL logging
- Count queries vs records
- Look for queries in loops

---

### Issue 2: Missing Database Indexes

**Symptom**: Slow queries on large tables

```sql
-- Slow without index
SELECT * FROM ticket WHERE status = 'OPEN';  -- Table scan

-- Fast with index
CREATE INDEX idx_ticket_status ON ticket(status);
```

**Detection**:
- Check query execution plans
- Look for full table scans
- Analyze slow query logs

---

### Issue 3: Fetching Too Much Data

**Symptom**: Large result sets, high memory usage

```java
// ❌ Fetching everything
public List<Ticket> getAllTickets() {
    return ticketRepository.findAll();  // Could be thousands!
}

// ✅ Paginate
public Page<Ticket> getTickets(Pageable pageable) {
    return ticketRepository.findAll(pageable);
}
```

---

### Issue 4: Inefficient Algorithms

**Symptom**: O(n²) or worse complexity

```java
// ❌ O(n²) - inefficient
public List<Ticket> findDuplicates(List<Ticket> tickets) {
    List<Ticket> duplicates = new ArrayList<>();
    for (Ticket t1 : tickets) {
        for (Ticket t2 : tickets) {
            if (t1.getTitle().equals(t2.getTitle())) {
                duplicates.add(t1);
            }
        }
    }
    return duplicates;
}

// ✅ O(n) - efficient
public List<Ticket> findDuplicates(List<Ticket> tickets) {
    Set<String> seen = new HashSet<>();
    return tickets.stream()
        .filter(t -> !seen.add(t.getTitle()))
        .toList();
}
```

---

### Issue 5: Excessive Object Creation

**Symptom**: High GC pressure, frequent collections

```java
// ❌ Creates many objects
public String format(List<Ticket> tickets) {
    String result = "";
    for (Ticket ticket : tickets) {
        result += ticket.getTitle() + "\n";  // Creates new String each time
    }
    return result;
}

// ✅ Efficient string building
public String format(List<Ticket> tickets) {
    return tickets.stream()
        .map(Ticket::getTitle)
        .collect(Collectors.joining("\n"));
}
```

---

### Step 3: Analyze with Profiling Tools

**Enable SQL logging**:
```properties
# application-local.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

**Enable performance logging**:
```properties
logging.level.org.springframework.web=DEBUG
logging.level.com.example.supportticket=DEBUG
```

**Use Spring Boot Actuator**:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

### Step 4: Optimize

**Optimization techniques by category:**

### Database Optimization

#### Technique 1: Fix N+1 with JOIN FETCH

```java
// Before - N+1 problem
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAll();  // Lazy loads comments
}

// After - Eager fetch with JOIN
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.comments")
    List<Ticket> findAllWithComments();
}
```

#### Technique 2: Add Database Indexes

```sql
-- Identify slow queries
EXPLAIN SELECT * FROM ticket WHERE status = 'OPEN';

-- Add index
CREATE INDEX idx_ticket_status ON ticket(status);

-- Verify improvement
EXPLAIN SELECT * FROM ticket WHERE status = 'OPEN';
```

**In migration**:
```sql
-- V3__add_performance_indexes.sql
CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_priority ON ticket(priority);
CREATE INDEX idx_ticket_created_at ON ticket(created_at);
CREATE INDEX idx_comment_ticket_id ON comment(ticket_id);
```

#### Technique 3: Use Pagination

```java
// Before - Load everything
public List<TicketSummaryResponse> getAllTickets() {
    return ticketRepository.findAll()
        .stream()
        .map(TicketMapper::toSummaryResponse)
        .toList();
}

// After - Paginate
public Page<TicketSummaryResponse> getTickets(Pageable pageable) {
    return ticketRepository.findAll(pageable)
        .map(TicketMapper::toSummaryResponse);
}
```

#### Technique 4: Use Projections for Read-Only Queries

```java
// Before - Fetch entire entity
public List<String> getAllTitles() {
    return ticketRepository.findAll()
        .stream()
        .map(Ticket::getTitle)
        .toList();
}

// After - Projection
public interface TitleProjection {
    String getTitle();
}

@Query("SELECT t.title as title FROM Ticket t")
List<TitleProjection> findAllTitles();
```

---

### Caching Optimization

#### Technique 5: Add Caching for Frequent Reads

```java
// Before - Always hits database
public Ticket getTicket(Long id) {
    return ticketRepository.findById(id)
        .orElseThrow(() -> new TicketNotFoundException(id));
}

// After - Cached
@Cacheable(value = "tickets", key = "#id")
public Ticket getTicket(Long id) {
    return ticketRepository.findById(id)
        .orElseThrow(() -> new TicketNotFoundException(id));
}

@CacheEvict(value = "tickets", key = "#id")
public void updateTicket(Long id, UpdateTicketRequest request) {
    // Update logic
}
```

**Enable caching**:
```java
@SpringBootApplication
@EnableCaching
public class SupportTicketApplication {
    // ...
}
```

---

### Code Optimization

#### Technique 6: Optimize Loops

```java
// Before - Inefficient
public int countOpenTickets(List<Ticket> tickets) {
    int count = 0;
    for (Ticket ticket : tickets) {
        if (ticket.getStatus() == TicketStatus.OPEN) {
            count++;
        }
    }
    return count;
}

// After - Stream (readable and optimizable)
public long countOpenTickets(List<Ticket> tickets) {
    return tickets.stream()
        .filter(ticket -> ticket.getStatus() == TicketStatus.OPEN)
        .count();
}
```

#### Technique 7: Use Appropriate Data Structures

```java
// Before - O(n) lookup
List<Long> processedIds = new ArrayList<>();
if (processedIds.contains(ticketId)) {  // O(n)
    // ...
}

// After - O(1) lookup
Set<Long> processedIds = new HashSet<>();
if (processedIds.contains(ticketId)) {  // O(1)
    // ...
}
```

#### Technique 8: Lazy Initialization

```java
// Before - Always creates (even if not used)
public class TicketService {
    private final List<TicketValidator> validators = createValidators();
    
    private List<TicketValidator> createValidators() {
        // Expensive operation
    }
}

// After - Create only when needed
public class TicketService {
    private List<TicketValidator> validators;
    
    private List<TicketValidator> getValidators() {
        if (validators == null) {
            validators = createValidators();
        }
        return validators;
    }
}
```

---

### Step 5: Benchmark and Verify

**Measure after optimization:**

```java
// JMH Benchmark example
@State(Scope.Thread)
public class TicketServiceBenchmark {
    
    private TicketService ticketService;
    
    @Setup
    public void setup() {
        // Initialize
    }
    
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkGetAllTickets() {
        ticketService.getAllTickets();
    }
}
```

**Compare metrics**:
```markdown
## Performance Comparison

### Before Optimization
- GET /api/tickets: 450ms (1000 records)
- Database queries: 1001 (N+1 problem)
- Memory: 512 MB

### After Optimization
- GET /api/tickets: 45ms (1000 records) ✅ 10x faster
- Database queries: 1 (JOIN FETCH) ✅ 99.9% reduction
- Memory: 256 MB ✅ 50% reduction
```

**Verify correctness**:
- [ ] All tests still pass
- [ ] Behavior unchanged
- [ ] No regressions in other areas

---

## Common Performance Issues in Spring Boot

### Issue: Lazy Loading Outside Transaction

**Problem**:
```java
// ❌ LazyInitializationException
public TicketDetailResponse getTicket(Long id) {
    Ticket ticket = ticketRepository.findById(id).orElseThrow();
    // Transaction ended, session closed
    return TicketMapper.toDetailResponse(ticket);  // Tries to load comments lazily
}
```

**Solution**:
```java
// ✅ Option 1: Use @Transactional
@Transactional(readOnly = true)
public TicketDetailResponse getTicket(Long id) {
    Ticket ticket = ticketRepository.findById(id).orElseThrow();
    return TicketMapper.toDetailResponse(ticket);
}

// ✅ Option 2: Use JOIN FETCH
@Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.comments WHERE t.id = :id")
Optional<Ticket> findByIdWithComments(@Param("id") Long id);
```

---

### Issue: Too Many Open Connections

**Problem**: Connection pool exhausted

**Solution**:
```properties
# application.properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

---

### Issue: Large Response Payloads

**Problem**: Returning too much data

**Solution**:
```java
// ❌ Returns everything
public List<TicketDetailResponse> getAllTickets() {
    return ticketRepository.findAll()
        .stream()
        .map(TicketMapper::toDetailResponse)  // Includes comments
        .toList();
}

// ✅ Use summary for list, details for single
public List<TicketSummaryResponse> getAllTickets() {
    return ticketRepository.findAll()
        .stream()
        .map(TicketMapper::toSummaryResponse)  // No comments
        .toList();
}
```

---

## Performance Testing

### Load Testing with JMeter

```xml
<!-- Sample JMeter test plan -->
<ThreadGroup>
  <stringProp name="ThreadGroup.num_threads">100</stringProp>
  <stringProp name="ThreadGroup.ramp_time">10</stringProp>
  <stringProp name="ThreadGroup.duration">60</stringProp>
  
  <HTTPSamplerProxy>
    <stringProp name="HTTPSampler.path">/api/tickets</stringProp>
    <stringProp name="HTTPSampler.method">GET</stringProp>
  </HTTPSamplerProxy>
</ThreadGroup>
```

### Performance Test with @Benchmark

```java
@Test
public void testPerformance() {
    long startTime = System.currentTimeMillis();
    
    ticketService.getAllTickets();
    
    long duration = System.currentTimeMillis() - startTime;
    
    assertThat(duration).isLessThan(100);  // Must complete in 100ms
}
```

---

## Optimization Checklist

### Database Optimizations
- [ ] Added appropriate indexes
- [ ] Fixed N+1 queries (use JOIN FETCH)
- [ ] Added pagination for large result sets
- [ ] Used projections for read-only queries
- [ ] Verified query execution plans
- [ ] Connection pool configured properly

### Code Optimizations
- [ ] Used appropriate data structures (Set vs List)
- [ ] Avoided nested loops where possible
- [ ] Used streams for readability
- [ ] Minimized object creation
- [ ] Lazy initialization where appropriate
- [ ] Efficient string operations (StringBuilder)

### Caching
- [ ] Identified frequently accessed data
- [ ] Added caching with @Cacheable
- [ ] Added cache eviction with @CacheEvict
- [ ] Configured cache size limits
- [ ] Tested cache effectiveness

### Verification
- [ ] Benchmarked before and after
- [ ] All tests pass
- [ ] No behavior changes
- [ ] Documented improvements
- [ ] Measured under realistic load

---

## Performance Goals

**Target response times:**

| Endpoint | Target | Acceptable | Slow |
|----------|--------|------------|------|
| GET (list) | <100ms | <500ms | >1s |
| GET (single) | <50ms | <200ms | >500ms |
| POST | <200ms | <500ms | >1s |
| PATCH | <100ms | <300ms | >500ms |
| DELETE | <100ms | <300ms | >500ms |

**Database queries:**
- Simple queries: <10ms
- Complex queries: <100ms
- Avoid N+1 queries

**Memory:**
- Heap usage <80% of max
- No memory leaks
- GC pause time <100ms

---

## When NOT to Optimize

**Don't optimize if:**

- Performance is already acceptable
- Code would become unreadable
- Optimization violates project principles
- "Premature optimization is the root of all evil"

**Optimize when:**
- Measurable performance problem exists
- User experience is impacted
- Resource costs are high
- Scalability is at risk

---

## Documentation Template

```markdown
## Performance Optimization: [Feature]

**Date**: YYYY-MM-DD
**Issue**: [Description of performance problem]
**Impact**: [User impact, cost impact]

### Baseline Metrics
- [Metric 1]: [Value]
- [Metric 2]: [Value]

### Root Cause
[Analysis of why performance was poor]

### Optimizations Applied
1. [Optimization 1]
2. [Optimization 2]

### Results
- [Metric 1]: [New value] (X% improvement)
- [Metric 2]: [New value] (X% improvement)

### Trade-offs
[Any compromises or limitations]

### Verification
- [x] All tests pass
- [x] Benchmarks show improvement
- [x] No regressions

### Next Steps
[Further optimizations if needed]
```

---

**Skill Version**: 1.0  
**Last Updated**: 2026-09-23  
**Related Documents**: PROJECT_CONSTITUTION.md, .kiro/steering/java-springboot.md
