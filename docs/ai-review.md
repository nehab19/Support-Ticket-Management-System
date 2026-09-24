# AI Code Review Log

**Purpose**: Document meaningful mistakes made by AI assistants during development.

**Scope**: Record significant errors, pattern failures, architecture violations, and security issues. Exclude minor syntax errors and formatting issues.

---

## Review Entry Template

```markdown
## YYYY-MM-DD: [Brief Description]

**AI Tool**: [e.g., Cursor, GitHub Copilot, ChatGPT, Claude]
**Task**: [What was the AI asked to do?]
**Issue**: [What went wrong? What mistake did the AI make?]
**Impact**: [What would have happened if this wasn't caught?]
**Resolution**: [How was it fixed?]
**Lesson**: [What can we learn? How to prevent in future?]
```

---

## Log Entries

*(No entries yet. This section will be populated as AI mistakes are discovered and documented.)*

---

## Common AI Mistake Patterns

As mistakes are documented, patterns will emerge. This section summarizes recurring issues:

### Pattern: [Pattern Name]

**Frequency**: [How often this occurs]
**Description**: [What the pattern is]
**Prevention**: [How to avoid this in the future]
**Related Entries**: [Links to specific log entries]

---

## Review Statistics

**Total Reviews**: 0  
**Issues Found**: 0  
**Critical Issues**: 0  
**Major Issues**: 0  
**Minor Issues**: 0

*(Updated as reviews are conducted)*

---

## Guidelines for Reviewers

**What to Document**:
- ✅ Logic errors that would cause incorrect behavior
- ✅ Security vulnerabilities (SQL injection, XSS, exposed secrets)
- ✅ Architecture violations (business logic in wrong layer)
- ✅ Violations of project constitution
- ✅ Missing validation or error handling
- ✅ Test gaps or incorrect test logic
- ✅ Performance issues (N+1 queries, memory leaks)
- ✅ Repeated mistakes indicating AI misunderstanding

**What NOT to Document**:
- ❌ Minor syntax errors (missing semicolon, typo)
- ❌ Formatting issues (indentation, whitespace)
- ❌ Preference differences (variable naming style)
- ❌ Issues caught immediately by compiler/linter
- ❌ Non-functional suggestions that work correctly

**Review Process**:
1. Review all AI-generated code line-by-line
2. Run automated tests (unit, integration, property-based)
3. Manually test functionality
4. Check against project constitution
5. Document any meaningful issues found
6. Approve or request regeneration

---

## Future Improvements

As this log grows, consider:
- Adding AI tool comparison metrics
- Creating automated checks for common mistakes
- Developing prompts that avoid known patterns
- Training materials based on documented issues

---

**Last Updated**: 2026-09-23  
**Next Review**: Ongoing (update as issues are found)
