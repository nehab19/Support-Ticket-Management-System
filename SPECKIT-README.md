# SpecKit: Complete Spec-Driven Development

**Created**: 2026-09-23  
**Version**: 1.0  
**Status**: ✅ Production Ready

---

## What is SpecKit?

**SpecKit** is the master workflow for spec-driven development in this project. It provides a structured, 12-phase approach from initial idea to production-ready implementation.

```
Constitution → Specify → Clarify → Plan → Checklist → Tasks → 
Analyze → Implement → Testing → Converge → Human Review → Fix
```

---

## Quick Start

### Invoke SpecKit

```
#speckit

I want to build [feature description]. 
Guide me through the complete process.
```

### Example

```
#speckit

I want to add user authentication with JWT tokens. 
The system should support login, logout, and token refresh.
```

---

## The 12 Phases

### Phase 1: Constitution (15-30 min)
**Goal**: Ensure alignment with project principles
- Review PROJECT_CONSTITUTION.md
- Check architectural compliance
- Identify stakeholders

### Phase 2: Specify (1-2 hours)
**Goal**: Define requirements with acceptance criteria
- Write user stories
- Define EARS acceptance criteria
- Identify correctness properties

### Phase 3: Clarify (30-60 min)
**Goal**: Resolve ambiguities
- Ask clarifying questions
- Document edge cases
- Define error handling

### Phase 4: Plan (2-4 hours)
**Goal**: Design the technical solution
- Architecture design
- Data model design
- API contract design
- State machine design (if applicable)
- Test strategy

### Phase 5: Checklist (30 min)
**Goal**: Create implementation checklist
- List all files to create/modify
- Identify dependencies
- Order tasks logically

### Phase 6: Tasks (30-60 min)
**Goal**: Detailed task breakdown
- Create `.kiro/specs/{feature}/tasks.md`
- Break into tasks with sub-tasks
- Estimate effort

### Phase 7: Analyze (30 min)
**Goal**: Validate before implementation
- Design review
- Completeness check
- Risk assessment
- Go/No-Go decision

### Phase 8: Implement (Varies)
**Goal**: Write production code
- Follow task order
- Commit frequently
- Follow coding standards
- Document as you go

### Phase 9: Testing (Equal to implementation)
**Goal**: Comprehensive test coverage
- Unit tests
- Integration tests
- Property-based tests (100+ iterations)
- Achieve >80% coverage

### Phase 10: Converge (1-2 hours)
**Goal**: Integrate and prepare for review
- Run full test suite
- Code quality check
- Documentation review
- Create pull request

### Phase 11: Human Review (Varies)
**Goal**: Get human validation
- Submit PR with complete description
- Address reviewer questions
- Make requested changes

### Phase 12: Fix (Varies)
**Goal**: Address feedback and iterate
- Categorize feedback
- Implement fixes
- Re-validate
- Re-submit

---

## Key Artifacts by Phase

| Phase | Artifact | Location |
|-------|----------|----------|
| 1 | Constitutional review | Notes |
| 2 | Requirements | `spec/requirements-{feature}.md` |
| 3 | Clarifications | In requirements doc |
| 4 | Design specs | `spec/*.md` files |
| 5 | Checklist | Implementation notes |
| 6 | Tasks | `.kiro/specs/{feature}/tasks.md` |
| 7 | Analysis | Review document |
| 8 | Code | `backend/src/main/java/...` |
| 9 | Tests | `backend/src/test/java/...` |
| 10 | PR | GitHub/GitLab |
| 11 | Review | PR comments |
| 12 | Fixes | Updated code |

---

## Integration with Other Skills

SpecKit orchestrates all other skills at appropriate phases:

```
Phase 4 (Plan)         → Use #adr for architectural decisions
Phase 8 (Implement)    → Use #refactor for code quality
Phase 9 (Testing)      → Follow testing standards
Phase 10 (Converge)    → Use #code-review before PR
Phase 11 (Review)      → Use #bug-investigation if issues
Phase 12 (Fix)         → Use #performance-review if needed
```

---

## Constitutional Compliance

SpecKit ensures compliance with PROJECT_CONSTITUTION.md at every phase:

### Architecture (Phases 1, 4, 7, 10)
- ✅ Layered architecture (Controller → Service → Repository)
- ✅ DTOs for all API contracts
- ✅ Business rules in service layer only
- ✅ No layer skipping

### Security (Phases 1, 8, 10)
- ✅ No secrets in code
- ✅ Environment variables for config
- ✅ Input validation
- ✅ Proper error handling

### Testing (Phases 6, 9, 10)
- ✅ Unit tests for services
- ✅ Integration tests for endpoints
- ✅ Property tests (100+ iterations)
- ✅ Coverage >80%

### Documentation (Phases 2, 4, 8, 10)
- ✅ Requirements documented
- ✅ Design documented
- ✅ JavaDoc for public APIs
- ✅ API contracts documented

---

## Success Metrics

Track these to measure effectiveness:

**Time Metrics**:
- Time per phase
- Total time to completion
- Time spent in Phase 12 (fixes)

**Quality Metrics**:
- Test coverage achieved
- Number of bugs in review
- Number of review iterations

**Completeness Metrics**:
- All phases completed
- All artifacts created
- All checklists verified

---

## Example: User Authentication Feature

### Timeline
- **Week 1**: Phases 1-7 (Constitution through Analyze)
- **Week 2**: Phase 8-9 (Implement and Testing)
- **Week 3**: Phase 10-12 (Converge, Review, Fix)

### Artifacts Created
- `spec/requirements-user-auth.md` (8 requirements)
- `spec/architecture-user-auth.md` (4 components)
- `spec/data-model-user-auth.md` (User entity)
- `spec/api-contract-user-auth.md` (2 endpoints)
- `.kiro/specs/user-authentication/tasks.md` (5 tasks, 23 sub-tasks)
- 18 Java files (entities, services, controllers, DTOs)
- 32 test files
- Pull request with complete documentation

### Results
- ✅ 87% test coverage
- ✅ All constitutional requirements met
- ✅ Approved in 2 review iterations
- ✅ Merged to main after 3 weeks

---

## Tips for Success

### Do's ✅

1. **Don't skip phases** - Each builds on the previous
2. **Document thoroughly** - Specs are your contract
3. **Test comprehensively** - Tests prove correctness
4. **Commit frequently** - Small, focused commits
5. **Seek feedback early** - Don't wait until the end
6. **Use constitutional checklist** - At Phases 1, 7, 10

### Don'ts ❌

1. **Don't skip Constitution phase** - Alignment is critical
2. **Don't rush through Clarify** - Ambiguities cause rework
3. **Don't skip Analysis** - Catch design issues early
4. **Don't skimp on testing** - Tests are as important as code
5. **Don't work in isolation** - Communicate and collaborate
6. **Don't ignore review feedback** - Learn and improve

---

## Troubleshooting

### "I'm stuck in a phase"
**Solution**: Review phase goals. If unclear, go back one phase.

### "Requirements keep changing"
**Solution**: Update specs, re-analyze (Phase 7), communicate impact.

### "Tests are failing"
**Solution**: Don't proceed to Phase 10. Stay in Phase 9 until green.

### "Review taking too long"
**Solution**: Ping reviewers, provide more context, offer to discuss.

### "Too much rework in Phase 12"
**Solution**: Use `#code-review` in Phase 10 before submitting.

---

## Comparison: Traditional vs SpecKit

| Aspect | Traditional | SpecKit |
|--------|-------------|---------|
| **Planning** | Ad-hoc | Structured (Phases 1-7) |
| **Constitutional Check** | Maybe never | Every phase |
| **Requirements** | Verbal or incomplete | Documented with EARS |
| **Design** | In someone's head | Complete specs |
| **Testing** | After the fact | Planned from start |
| **Review** | Code only | Code + specs + tests |
| **Quality** | Varies | Consistently high |
| **Rework** | High | Low (caught in Phase 7) |

---

## SpecKit Philosophy

### Principles

1. **Specification Before Implementation**
   - Know what to build before building
   - Document the contract
   - Validate design early

2. **Constitutional Compliance**
   - Architecture principles are non-negotiable
   - Security is built-in, not added later
   - Testing is integral, not optional

3. **Property-Based Correctness**
   - Define invariants explicitly
   - Test them comprehensively (100+ iterations)
   - Provide formal evidence of correctness

4. **Iterative Refinement**
   - Requirements → Design → Tasks → Code
   - Review → Fix → Converge
   - Continuous improvement

5. **Human in the Loop**
   - AI assists, human decides
   - Review is mandatory
   - Feedback drives improvement

---

## Team Adoption

### For New Team Members

**Week 1**: Shadow experienced developer using SpecKit
**Week 2**: Try small feature with mentorship
**Week 3**: Lead a feature with SpecKit
**Week 4**: Independent with SpecKit

### For Experienced Developers

- Use SpecKit for complex features (always)
- Use abbreviated SpecKit for simple features (Phases 2, 6, 8, 9, 10, 11)
- Mentor others on SpecKit usage
- Contribute improvements to SpecKit

---

## SpecKit Evolution

### Version 1.0 (Current)
- 12 phases
- Constitutional compliance
- Property-based testing
- Complete documentation

### Future Enhancements (Planned)
- Phase time estimates by feature complexity
- Automated phase validation
- SpecKit metrics dashboard
- Integration with project management tools
- AI-assisted phase transitions

---

## Resources

### Documentation
- **SpecKit Full Workflow**: `.kiro/skills/speckit/SKILL.md`
- **Project Constitution**: `PROJECT_CONSTITUTION.md`
- **Coding Standards**: `.kiro/steering/*.md`
- **Other Skills**: `.kiro/skills/*/SKILL.md`

### Templates
- Requirements template: See Phase 2
- Design template: See Phase 4
- Tasks template: See Phase 6
- PR template: See Phase 11

### Tools
- **Invoke SpecKit**: `#speckit`
- **Other Skills**: `#code-review`, `#adr`, etc.
- **Testing**: `./mvnw test`, `./mvnw jacoco:report`

---

## FAQ

### Q: Do I have to use SpecKit for every feature?

**A**: For complex features (>1 week), yes. For simple fixes (<1 day), use abbreviated workflow (Phases 2, 6, 8, 9, 10, 11).

### Q: What if requirements change mid-implementation?

**A**: Return to Phase 2 (Specify), update specs, re-analyze in Phase 7, continue from current implementation phase.

### Q: Can I skip the Testing phase?

**A**: No. Testing (Phase 9) is mandatory per PROJECT_CONSTITUTION.md. No tests = no merge.

### Q: How do I know if I'm ready to move to the next phase?

**A**: Check the phase outputs and checklist. All items should be complete before proceeding.

### Q: What if Phase 7 (Analyze) reveals major issues?

**A**: Go back to Phase 4 (Plan) and redesign. Better to catch issues early than late.

### Q: Can I use SpecKit for bug fixes?

**A**: For complex bugs, yes. For simple bugs, use `#bug-investigation` skill instead.

---

## Success Stories

### Feature: Ticket Status State Machine
- **Complexity**: High (strict business rules)
- **SpecKit Phases**: All 12
- **Duration**: 2 weeks
- **Outcome**: Zero bugs in production, 90% test coverage

### Feature: Comment System
- **Complexity**: Medium (CRUD with relationships)
- **SpecKit Phases**: 2, 4, 6, 8, 9, 10, 11, 12
- **Duration**: 1 week
- **Outcome**: Approved in 1 review iteration

### Feature: Search by Keyword
- **Complexity**: Low (single query enhancement)
- **SpecKit Phases**: 2, 6, 8, 9, 10, 11
- **Duration**: 2 days
- **Outcome**: Merged same day

---

## Get Started Today

### Step 1: Read This Document ✅

You're here! You've completed step 1.

### Step 2: Review SpecKit Workflow

Open `.kiro/skills/speckit/SKILL.md` and read through all 12 phases.

### Step 3: Try It

Pick a small feature (1-3 days) and invoke:

```
#speckit

I want to [describe your feature].
Guide me through the process.
```

### Step 4: Reflect and Improve

After completing your first SpecKit journey:
- What worked well?
- What was challenging?
- How can SpecKit be improved?

---

## Conclusion

**SpecKit is your guide from idea to production.**

It ensures:
- ✅ Constitutional compliance
- ✅ High-quality implementation
- ✅ Comprehensive testing
- ✅ Complete documentation
- ✅ Successful reviews

**Start using SpecKit today and elevate your development process!**

---

**Document Version**: 1.0  
**Last Updated**: 2026-09-23  
**Maintained By**: Development Team

**Invoke SpecKit**: `#speckit`

