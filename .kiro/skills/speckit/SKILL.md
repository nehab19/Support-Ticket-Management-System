---
inclusion: manual
---

# SpecKit: Complete Spec-Driven Development Workflow

**Skill Name**: SpecKit  
**Version**: 1.0  
**Last Updated**: 2026-09-23

---

## Purpose

SpecKit is the master workflow for spec-driven development in the Support Ticket Management System. It provides a structured, phase-by-phase approach from initial idea to production-ready implementation, ensuring compliance with project constitution and quality standards.

**Scope**: Complete feature development lifecycle  
**Authority**: Supreme workflow implementing PROJECT_CONSTITUTION.md principles

---

## When to Use This Skill

Invoke this skill (`#speckit`) when:

- Starting a new feature from scratch
- Need end-to-end guidance for feature development
- Want to follow the complete spec-driven methodology
- Ensuring constitutional compliance throughout development
- Need a systematic approach to complex features

---

## SpecKit Workflow Overview

```
Constitution ↓
Specify ↓
Clarify ↓
Plan ↓
Checklist ↓
Tasks ↓
Analyze ↓
Implement ↓
Testing ↓
Converge ↓
Human Review ↓
Fix
```

**11 Phases** from concept to completion

---

## Phase 1: Constitution

**Goal**: Ensure constitutional alignment and establish governance

**Duration**: 15-30 minutes

### Activities

1. **Review Constitutional Requirements**:
   - [ ] Read PROJECT_CONSTITUTION.md relevant sections
   - [ ] Identify applicable principles
   - [ ] Check for conflicts with existing decisions
   - [ ] Review related ADRs

2. **Constitutional Compliance Check**:
   - [ ] Does feature align with project mission?
   - [ ] Are architectural principles upheld?
   - [ ] Does it follow layered architecture?
   - [ ] Security requirements understood?
   - [ ] Testing requirements clear?

3. **Governance Setup**:
   - [ ] Identify stakeholders
   - [ ] Determine approval requirements
   - [ ] Plan documentation needs
   - [ ] Schedule reviews

### Outputs

- Constitutional compliance statement
- List of applicable principles
- Governance plan

### Example

```markdown
## Constitutional Review: User Authentication Feature

**Alignment**: ✅ Aligned with project mission (secure ticket management)

**Applicable Principles**:
- Layered architecture (auth in service layer)
- Security (no secrets in code, JWT tokens)
- Testing (unit + integration + property tests)

**Governance**:
- Stakeholder: Security team
- Review: Required before production
- Documentation: Update security section
```

### Proceed to Phase 2

Once constitutional alignment is confirmed.

---

## Phase 2: Specify

**Goal**: Define requirements with acceptance criteria

**Duration**: 1-2 hours

### Activities

1. **Gather Requirements**:
   - [ ] Interview stakeholders
   - [ ] Review user needs
   - [ ] Identify constraints
   - [ ] List dependencies

2. **Write User Stories**:
   ```markdown
   As a [role],
   I want [capability],
   So that [benefit]
   ```

3. **Define Acceptance Criteria (EARS format)**:
   - Ubiquitous: "The system shall..."
   - Event-driven: "When [event], the system shall..."
   - State-driven: "While [state], the system shall..."
   - Unwanted event: "If [error], the system shall..."
   - Optional: "Where [condition], the system shall..."
   - Complex: Combination of above

4. **Identify Correctness Properties**:
   - [ ] What invariants must hold?
   - [ ] What safety properties needed?
   - [ ] What liveness properties needed?

### Outputs

- User stories (3-10 stories)
- Acceptance criteria (EARS format)
- Correctness properties (for property-based testing)
- Requirements document: `spec/requirements-{feature}.md`

### Example

```markdown
## REQ-1: User Login

**User Story**:
As a support agent,
I want to log in with email and password,
So that I can access the ticket system securely.

**Acceptance Criteria**:
- When user submits valid credentials, the system shall authenticate and return JWT token
- If credentials are invalid, the system shall return 401 Unauthorized
- The system shall rate-limit login attempts (max 5 per minute per IP)
- While user is authenticated, the system shall include user context in all requests

**Correctness Properties**:
- Property 1: Invalid credentials never authenticate
- Property 2: Rate limiting always enforced
- Property 3: JWT tokens expire after configured duration
```

### Proceed to Phase 3

Once requirements are documented and reviewed.

---

## Phase 3: Clarify

**Goal**: Resolve ambiguities and document assumptions

**Duration**: 30 minutes - 1 hour

### Activities

1. **Identify Ambiguities**:
   - [ ] Unclear requirements
   - [ ] Missing edge cases
   - [ ] Undefined behaviors
   - [ ] Unstated assumptions

2. **Ask Clarifying Questions**:
   ```markdown
   - What happens when [edge case]?
   - How should system behave if [error condition]?
   - What are the limits for [resource]?
   - Who can perform [action]?
   - What validation rules apply to [input]?
   ```

3. **Document Decisions**:
   - [ ] Edge case handling
   - [ ] Error scenarios
   - [ ] Boundary conditions
   - [ ] Performance expectations
   - [ ] Security considerations

4. **Update Requirements**:
   - [ ] Add clarifications
   - [ ] Define edge cases
   - [ ] Specify error handling
   - [ ] Document assumptions

### Outputs

- Clarifications document
- Updated requirements
- Documented assumptions
- Resolved ambiguities

### Example

```markdown
## Clarifications: User Login

**Q**: What happens if user tries to login while already logged in?
**A**: Issue new JWT token, invalidate old one

**Q**: How long should JWT tokens be valid?
**A**: 8 hours for regular sessions, 24 hours for "remember me"

**Q**: Should we lock accounts after failed attempts?
**A**: Yes, after 10 failed attempts in 1 hour, lock for 30 minutes

**Assumptions**:
- Email addresses are case-insensitive
- Passwords must be at least 8 characters
- JWT secret stored in environment variable
- Database stores password hashes (bcrypt)
```

### Proceed to Phase 4

Once all ambiguities are resolved.

---

## Phase 4: Plan

**Goal**: Design the technical solution

**Duration**: 2-4 hours

### Activities

1. **Architectural Design**:
   - [ ] Identify affected layers (Controller, Service, Repository)
   - [ ] Define new components
   - [ ] Map component interactions
   - [ ] Identify design patterns

2. **Data Model Design**:
   - [ ] Define entities
   - [ ] Specify relationships
   - [ ] Plan database schema
   - [ ] Design indexes
   - [ ] Create migration scripts

3. **API Contract Design**:
   - [ ] Define endpoints (method, path)
   - [ ] Specify request DTOs
   - [ ] Specify response DTOs
   - [ ] Map HTTP status codes
   - [ ] Design error responses

4. **State Machine Design** (if applicable):
   - [ ] Define states
   - [ ] Define allowed transitions
   - [ ] Define forbidden transitions
   - [ ] Specify validation rules

5. **Test Strategy**:
   - [ ] Plan unit tests
   - [ ] Plan integration tests
   - [ ] Plan property-based tests
   - [ ] Identify test scenarios

### Outputs

- Architecture document: `spec/architecture-{feature}.md`
- Data model: `spec/data-model-{feature}.md`
- API contract: `spec/api-contract-{feature}.md`
- State machine: `spec/state-machine-{feature}.md` (if applicable)
- Test strategy: `spec/test-strategy-{feature}.md`

### Example

```markdown
## Architecture: User Authentication

**Components**:
- AuthController: Handle login/logout requests
- AuthService: Business logic, token generation
- UserRepository: User data access
- JwtTokenProvider: JWT creation/validation

**Flow**:
1. Client → POST /api/auth/login (email, password)
2. AuthController → AuthService.authenticate()
3. AuthService → UserRepository.findByEmail()
4. AuthService → validatePassword() + generateToken()
5. AuthController → Return JWT token (200) or error (401)

**Data Model**:
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    failed_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**API Contract**:
POST /api/auth/login
Request: { "email": "user@example.com", "password": "secret" }
Response: { "token": "eyJ...", "expiresAt": "2026-09-24T10:00:00Z" }
```

### Proceed to Phase 5

Once design is complete and reviewed.

---

## Phase 5: Checklist

**Goal**: Create implementation checklist

**Duration**: 30 minutes

### Activities

1. **Break Down Implementation**:
   - [ ] List all files to create
   - [ ] List all files to modify
   - [ ] Identify dependencies
   - [ ] Order tasks logically

2. **Create Checklist**:
   ```markdown
   ## Implementation Checklist
   
   ### Database
   - [ ] Create migration V{N}__create_users_table.sql
   - [ ] Create User entity
   - [ ] Create UserRepository interface
   
   ### DTOs
   - [ ] Create LoginRequest DTO
   - [ ] Create LoginResponse DTO
   - [ ] Create AuthErrorResponse DTO
   
   ### Business Logic
   - [ ] Create JwtTokenProvider
   - [ ] Create AuthService
   - [ ] Implement authenticate() method
   - [ ] Implement rate limiting
   
   ### API Layer
   - [ ] Create AuthController
   - [ ] Add POST /api/auth/login endpoint
   - [ ] Add exception handling
   
   ### Testing
   - [ ] Unit tests for AuthService
   - [ ] Unit tests for JwtTokenProvider
   - [ ] Integration tests for /api/auth/login
   - [ ] Property tests for token validation
   
   ### Documentation
   - [ ] Update API documentation
   - [ ] Add JavaDoc
   - [ ] Update README if needed
   ```

3. **Identify Risks**:
   - [ ] Technical risks
   - [ ] Timeline risks
   - [ ] Dependency risks

### Outputs

- Implementation checklist
- Risk assessment
- Dependency list

### Proceed to Phase 6

Once checklist is complete.

---

## Phase 6: Tasks

**Goal**: Create detailed task breakdown

**Duration**: 30-60 minutes

### Activities

1. **Create Task File**: `.kiro/specs/{feature-name}/tasks.md`

2. **Define Tasks with Sub-tasks**:
   ```markdown
   ## Tasks
   
   ### 1. Database Setup
   - [ ] 1.1 Create migration script
   - [ ] 1.2 Define User entity with JPA annotations
   - [ ] 1.3 Create UserRepository interface
   - [ ] 1.4 Test repository with H2
   
   ### 2. Authentication Logic
   - [ ] 2.1 Create JwtTokenProvider class
   - [ ] 2.2 Implement token generation
   - [ ] 2.3 Implement token validation
   - [ ] 2.4 Add rate limiting logic
   - [ ] 2.5 Create AuthService
   - [ ] 2.6 Implement authenticate() method
   
   ### 3. API Layer
   - [ ] 3.1 Create DTOs (LoginRequest, LoginResponse)
   - [ ] 3.2 Create AuthController
   - [ ] 3.3 Implement POST /api/auth/login
   - [ ] 3.4 Add exception handling
   
   ### 4. Testing
   - [ ] 4.1 Unit tests for JwtTokenProvider
   - [ ] 4.2 Unit tests for AuthService
   - [ ] 4.3 Integration tests for AuthController
   - [ ] 4.4 Property tests for token expiration
   
   ### 5. Documentation
   - [ ] 5.1 JavaDoc for public APIs
   - [ ] 5.2 Update spec/api-contract.md
   - [ ] 5.3 Create ADR for JWT choice
   ```

3. **Estimate Effort**:
   - [ ] Assign time estimates to each task
   - [ ] Identify critical path
   - [ ] Plan iteration milestones

### Outputs

- Detailed tasks file: `.kiro/specs/{feature}/tasks.md`
- Effort estimates
- Implementation order

### Proceed to Phase 7

Once tasks are defined.

---

## Phase 7: Analyze

**Goal**: Review and validate before implementation

**Duration**: 30 minutes

### Activities

1. **Design Review**:
   - [ ] Review architecture against constitution
   - [ ] Verify layered architecture compliance
   - [ ] Check security considerations
   - [ ] Validate error handling approach

2. **Completeness Check**:
   - [ ] All requirements have design
   - [ ] All edge cases covered
   - [ ] All error scenarios planned
   - [ ] Test strategy covers all properties

3. **Dependency Analysis**:
   - [ ] External dependencies identified
   - [ ] Integration points clear
   - [ ] Database changes planned
   - [ ] Migration strategy defined

4. **Risk Assessment**:
   - [ ] Technical risks documented
   - [ ] Mitigation strategies planned
   - [ ] Rollback plan exists

### Outputs

- Design review report
- Completeness checklist (verified)
- Risk mitigation plan

### Decision Point

**Go/No-Go Decision**:
- ✅ GO: Proceed to implementation
- ❌ NO-GO: Return to earlier phase to address gaps

### Proceed to Phase 8

Once analysis confirms readiness.

---

## Phase 8: Implement

**Goal**: Write production code following specifications

**Duration**: Varies by feature complexity

### Activities

1. **Follow Task Order**:
   - [ ] Implement tasks sequentially
   - [ ] Mark tasks as complete in tasks.md
   - [ ] Commit frequently

2. **Implementation Standards**:
   - [ ] Follow .kiro/steering/java-springboot.md
   - [ ] Use constructor injection
   - [ ] DTOs for all API contracts
   - [ ] @Transactional where needed
   - [ ] EnumType.STRING for enums
   - [ ] Proper exception handling

3. **Code as You Go**:
   - [ ] JavaDoc for public methods
   - [ ] Inline comments for complex logic
   - [ ] Meaningful variable names
   - [ ] Extract methods when >30 lines

4. **Commit Strategy**:
   ```bash
   # Commit after each major task
   git add .
   git commit -m "feat: implement JWT token provider"
   
   # Push regularly
   git push origin feature/user-authentication
   ```

### Implementation Checklist

**Database Layer**:
- [ ] Migration script follows V{N}__{description}.sql format
- [ ] Entity has proper JPA annotations
- [ ] Repository extends JpaRepository
- [ ] Custom queries use @Query

**Service Layer**:
- [ ] Business logic in service, not controller
- [ ] Input validation present
- [ ] @Transactional on write operations
- [ ] Proper exception handling

**Controller Layer**:
- [ ] Only HTTP concerns
- [ ] Uses @Valid for validation
- [ ] Returns DTOs, not entities
- [ ] Proper HTTP status codes

### Outputs

- Implemented code (all layers)
- Migration scripts
- DTOs
- Frequent commits

### Proceed to Phase 9

Once implementation is complete.

---

## Phase 9: Testing

**Goal**: Comprehensive test coverage

**Duration**: Equal to or greater than implementation time

### Activities

1. **Unit Tests**:
   ```java
   @Test
   void authenticate_validCredentials_returnsToken() {
       // Given
       LoginRequest request = new LoginRequest("user@example.com", "password");
       User user = createTestUser();
       when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
       
       // When
       LoginResponse response = authService.authenticate(request);
       
       // Then
       assertThat(response.token()).isNotBlank();
       assertThat(response.expiresAt()).isAfter(Instant.now());
   }
   
   @Test
   void authenticate_invalidPassword_throwsAuthenticationException() {
       // Given
       LoginRequest request = new LoginRequest("user@example.com", "wrong");
       User user = createTestUser();
       when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
       
       // When/Then
       assertThatThrownBy(() -> authService.authenticate(request))
           .isInstanceOf(AuthenticationException.class)
           .hasMessageContaining("Invalid credentials");
   }
   ```

2. **Integration Tests**:
   ```java
   @SpringBootTest
   @AutoConfigureMockMvc
   class AuthControllerIntegrationTest {
       
       @Autowired
       private MockMvc mockMvc;
       
       @Test
       void login_validCredentials_returns200WithToken() throws Exception {
           // Given
           String requestBody = """
               {
                   "email": "user@example.com",
                   "password": "password123"
               }
               """;
           
           // When/Then
           mockMvc.perform(post("/api/auth/login")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(requestBody))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.token").exists())
               .andExpect(jsonPath("$.expiresAt").exists());
       }
   }
   ```

3. **Property-Based Tests**:
   ```java
   // Feature: user-authentication, Property 1: Token expiration invariant
   @Property(tries = 100)
   void tokenExpiration_alwaysHappensAfterConfiguredDuration(
       @ForAll @Email String email,
       @ForAll @AlphaNumeric @StringLength(min = 8, max = 50) String password
   ) {
       // Arrange
       Instant before = Instant.now();
       
       // Act
       String token = jwtTokenProvider.generateToken(email);
       Instant expiresAt = jwtTokenProvider.getExpirationTime(token);
       
       // Assert
       Duration duration = Duration.between(before, expiresAt);
       assertThat(duration.toHours()).isGreaterThanOrEqualTo(8);
       assertThat(duration.toHours()).isLessThanOrEqualTo(8 + 1);  // Allow 1 hour buffer
   }
   ```

4. **Test Coverage**:
   ```bash
   # Run tests with coverage
   ./mvnw clean test jacoco:report
   
   # Check coverage report
   open target/site/jacoco/index.html
   ```

### Test Quality Checklist

- [ ] All service methods have unit tests
- [ ] All endpoints have integration tests
- [ ] Property tests for business invariants
- [ ] Edge cases covered
- [ ] Error scenarios tested
- [ ] Tests use AssertJ assertions
- [ ] Test names are descriptive
- [ ] Given-When-Then structure
- [ ] No flaky tests
- [ ] Coverage >80%

### Outputs

- Comprehensive test suite
- Test coverage report (>80%)
- Property tests (100+ iterations each)

### Proceed to Phase 10

Once all tests pass and coverage is adequate.

---

## Phase 10: Converge

**Goal**: Integrate, validate, and prepare for review

**Duration**: 1-2 hours

### Activities

1. **Integration Validation**:
   - [ ] Run full test suite
   - [ ] Run integration tests
   - [ ] Test with realistic data
   - [ ] Verify all tasks complete

2. **Code Quality Check**:
   - [ ] Run linter/formatter
   - [ ] Check for TODOs or FIXMEs
   - [ ] Verify no commented code
   - [ ] Check for proper error handling

3. **Documentation Review**:
   - [ ] All public APIs have JavaDoc
   - [ ] README updated (if needed)
   - [ ] API documentation updated
   - [ ] ADRs created for key decisions

4. **Constitutional Compliance**:
   - [ ] Layered architecture followed
   - [ ] DTOs used (never entities)
   - [ ] Business rules in service layer
   - [ ] No secrets in code
   - [ ] Tests exist and pass
   - [ ] Error handling consistent

5. **Prepare for Review**:
   - [ ] Create pull request
   - [ ] Write PR description
   - [ ] Link related issues
   - [ ] Request reviewers

### Convergence Checklist

**Code Quality**:
- [ ] No compilation errors
- [ ] No warnings
- [ ] All tests pass
- [ ] Coverage >80%
- [ ] No code smells

**Documentation**:
- [ ] JavaDoc complete
- [ ] API documentation updated
- [ ] Specs updated
- [ ] ADRs created

**Constitutional Compliance**:
- [ ] Architecture principles followed
- [ ] Security requirements met
- [ ] Testing requirements met
- [ ] Documentation requirements met

### Outputs

- Pull request
- Complete, tested, documented code
- Updated specifications
- Compliance verification

### Proceed to Phase 11

Once convergence is complete.

---

## Phase 11: Human Review

**Goal**: Get human validation and feedback

**Duration**: Varies (wait for reviewer)

### Activities

1. **Self-Review First**:
   - [ ] Use `#code-review` skill
   - [ ] Check all compliance items
   - [ ] Fix any obvious issues

2. **Submit for Review**:
   - [ ] Assign reviewers
   - [ ] Provide context in PR description
   - [ ] Link to specifications
   - [ ] Highlight key decisions

3. **PR Description Template**:
   ```markdown
   ## Feature: [Feature Name]
   
   ### Summary
   [Brief description of what was implemented]
   
   ### Specifications
   - Requirements: `spec/requirements-{feature}.md`
   - Design: `spec/architecture-{feature}.md`
   - Tasks: `.kiro/specs/{feature}/tasks.md`
   
   ### Key Changes
   - [Change 1]
   - [Change 2]
   - [Change 3]
   
   ### Testing
   - Unit tests: X tests, Y% coverage
   - Integration tests: Z tests
   - Property tests: N properties, 100 iterations each
   
   ### Constitutional Compliance
   - [x] Layered architecture
   - [x] DTOs for API
   - [x] Business rules in service
   - [x] No secrets in code
   - [x] Tests pass
   - [x] Documentation complete
   
   ### ADRs
   - ADR-XXX: [Decision title]
   
   ### Screenshots (if applicable)
   [Add screenshots]
   
   ### Checklist
   - [x] All tasks complete
   - [x] Tests pass
   - [x] Documentation updated
   - [x] Self-reviewed with #code-review
   ```

4. **Address Feedback**:
   - [ ] Respond to comments
   - [ ] Make requested changes
   - [ ] Re-request review
   - [ ] Document significant changes

### Review Outcomes

**Approved**:
- ✅ Merge to main/development branch
- Update project tracking
- Close related issues
- Celebrate! 🎉

**Changes Requested**:
- 🔄 Proceed to Phase 12 (Fix)

### Proceed to Phase 12

If changes are requested.

---

## Phase 12: Fix

**Goal**: Address review feedback and iterate

**Duration**: Varies by feedback

### Activities

1. **Categorize Feedback**:
   - **Critical**: Security, architecture violations → Fix immediately
   - **Important**: Code quality, missing tests → Fix before merge
   - **Nice-to-have**: Refactoring suggestions → Consider

2. **Address Each Item**:
   - [ ] Understand the feedback
   - [ ] Ask clarifying questions if needed
   - [ ] Implement fixes
   - [ ] Add tests if needed
   - [ ] Update documentation

3. **Iterative Fixes**:
   ```bash
   # Fix issue 1
   git add .
   git commit -m "fix: address review feedback - add null check"
   
   # Fix issue 2
   git add .
   git commit -m "fix: extract validation method as suggested"
   
   # Push fixes
   git push origin feature/user-authentication
   ```

4. **Re-validate**:
   - [ ] Run all tests
   - [ ] Self-review changes
   - [ ] Verify feedback addressed
   - [ ] Update PR description

5. **Respond to Reviewers**:
   ```markdown
   > Reviewer: Should add null check for email parameter
   
   Fixed in commit abc123. Added null check and test case.
   
   > Reviewer: Consider extracting validation logic
   
   Agreed. Extracted to `validateLoginRequest()` method. See commit def456.
   ```

### Fix Iteration

**If more feedback**:
- 🔄 Repeat Phase 12

**If approved**:
- ✅ Merge and complete!

---

## SpecKit Completion

### Post-Merge Activities

1. **Document the Journey**:
   - [ ] Use `#prompt-history` for significant AI interactions
   - [ ] Update `docs/ai-review.md` for AI mistakes (if any)
   - [ ] Update `docs/prompt-history.md` summary

2. **Lessons Learned**:
   - [ ] What went well?
   - [ ] What could be improved?
   - [ ] Update steering files if needed
   - [ ] Share knowledge with team

3. **Close the Loop**:
   - [ ] Update project tracking
   - [ ] Close related issues
   - [ ] Notify stakeholders
   - [ ] Plan next iteration (if needed)

---

## SpecKit Quick Reference

### Phase Checklist

- [ ] Phase 1: Constitution - Alignment verified
- [ ] Phase 2: Specify - Requirements documented
- [ ] Phase 3: Clarify - Ambiguities resolved
- [ ] Phase 4: Plan - Design complete
- [ ] Phase 5: Checklist - Implementation plan ready
- [ ] Phase 6: Tasks - Detailed tasks defined
- [ ] Phase 7: Analyze - Design validated
- [ ] Phase 8: Implement - Code written
- [ ] Phase 9: Testing - Tests pass, coverage >80%
- [ ] Phase 10: Converge - PR ready
- [ ] Phase 11: Human Review - Feedback received
- [ ] Phase 12: Fix - Feedback addressed (if needed)

### Key Artifacts by Phase

| Phase | Artifact | Location |
|-------|----------|----------|
| 1 | Constitutional review | Discussion notes |
| 2 | Requirements | `spec/requirements-{feature}.md` |
| 3 | Clarifications | Added to requirements |
| 4 | Design docs | `spec/architecture-{feature}.md`, etc. |
| 5 | Checklist | Implementation notes |
| 6 | Tasks | `.kiro/specs/{feature}/tasks.md` |
| 7 | Analysis report | Review document |
| 8 | Code | `backend/src/main/java/...` |
| 9 | Tests | `backend/src/test/java/...` |
| 10 | PR | GitHub/GitLab |
| 11 | Review | PR comments |
| 12 | Fixes | Updated code + commits |

---

## Tips for Success

### Do's ✅

- **Start with constitution** - Always align with principles
- **Document thoroughly** - Specs are your contract
- **Test comprehensively** - Tests are proof of correctness
- **Commit frequently** - Small, focused commits
- **Seek feedback early** - Don't wait until the end
- **Use other skills** - Combine with #code-review, #adr, etc.

### Don'ts ❌

- **Skip phases** - Each phase builds on previous
- **Ignore constitution** - It's the supreme authority
- **Rush testing** - Tests are as important as code
- **Large commits** - Hard to review and revert
- **Work in isolation** - Collaborate and communicate
- **Skip documentation** - Future you will thank you

---

## Combining with Other Skills

SpecKit works well with other skills at specific phases:

**Phase 4 (Plan)**: Use `#adr` for architectural decisions
**Phase 8 (Implement)**: Use `#refactor` for code quality
**Phase 9 (Testing)**: Follow `.kiro/steering/testing.md`
**Phase 10 (Converge)**: Use `#code-review` before submitting
**Phase 11 (Human Review)**: Use `#bug-investigation` if issues found
**Phase 12 (Fix)**: Use `#performance-review` if performance issues

---

## Example: Complete SpecKit Journey

### Feature: User Authentication

**Phase 1**: Verified alignment with security requirements ✅  
**Phase 2**: Wrote 3 user stories with EARS criteria ✅  
**Phase 3**: Clarified JWT duration, rate limiting, account locking ✅  
**Phase 4**: Designed 4 components, User entity, 2 endpoints ✅  
**Phase 5**: Created 25-item implementation checklist ✅  
**Phase 6**: Broke down into 5 tasks, 23 sub-tasks ✅  
**Phase 7**: Reviewed design, verified completeness ✅  
**Phase 8**: Implemented over 3 days, 18 commits ✅  
**Phase 9**: Wrote 32 tests, achieved 87% coverage ✅  
**Phase 10**: Created PR, ran full validation ✅  
**Phase 11**: Received feedback from 2 reviewers ✅  
**Phase 12**: Fixed 5 issues, re-submitted ✅

**Result**: Merged to main, feature complete! 🎉

**Timeline**: 2 weeks from idea to production

---

## Troubleshooting

### "I'm stuck in Phase X"

**Solution**: Review phase goals and outputs. If unclear, go back one phase.

### "Requirements keep changing"

**Solution**: Update specs, re-analyze in Phase 7, communicate impact.

### "Tests are failing"

**Solution**: Don't proceed to Phase 10. Fix in Phase 9.

### "Review taking too long"

**Solution**: Ping reviewers, offer to explain in meeting, check PR description.

### "Feedback conflicts with specs"

**Solution**: Discuss with reviewer, update specs if needed, document in ADR.

---

## SpecKit Metrics

Track these metrics to improve:

- Time spent per phase
- Number of iterations in Phase 12
- Test coverage achieved
- Number of bugs found in review
- Time from start to merge

---

**Skill Version**: 1.0  
**Last Updated**: 2026-09-23  
**Related Documents**: PROJECT_CONSTITUTION.md, All other skills, All steering files

**Master Workflow**: This is the supreme skill that orchestrates all others.

