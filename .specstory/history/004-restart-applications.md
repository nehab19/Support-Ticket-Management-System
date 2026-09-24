# Prompt 004: Restart Applications

**Date**: 2026-09-23  
**Session**: 1  
**Tool**: Kiro AI

---

## User Prompt

```
can you stop and restart the application
```

---

## Context

User wanted a clean restart of both applications, possibly to test data persistence or resolve issues.

---

## AI Response

### Step 1: List All Running Processes

**Command**: `listProcesses`

**Found**:
- Terminal ID 2: Backend (old process)
- Terminal ID 6: Backend (current)
- Terminal ID 7: Frontend (current)

**Issue**: Multiple backend instances running

### Step 2: Stop All Processes

**Stopped**:
- Terminal ID 2 ✅
- Terminal ID 6 ✅
- Terminal ID 7 ✅

### Step 3: Restart Backend

**Command**:
```bash
cd backend
/usr/lib/jvm/java-21-amazon-corretto/bin/java \
  -jar target/support-ticket-management-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

**Result**: ✅ Started (Terminal ID 8)

### Step 4: Restart Frontend

**Command**:
```bash
cd frontend
npm run dev
```

**Result**: ✅ Started (Terminal ID 9)

### Step 5: Verify Data Persistence

**Checked**: Database still contained 2 tickets from previous session

**Result**: ✅ Data persisted across restarts (H2 file-based database working correctly)

---

## Deliverables

**Clean Restart**:
- Backend: Terminal ID 8, port 8080
- Frontend: Terminal ID 9, port 3000
- Data: Preserved (2 existing tickets)

---

## Impact Assessment

**Immediate Impact**:
- ✅ Clean application state
- ✅ Verified data persistence
- ✅ Removed old processes

**Verification**:
- H2 file-based database works correctly
- No data loss on restart
- Application stable

---

**Prompt Number**: 004  
**Status**: Complete  
**Related Prompts**: 
- Prompt 003: Start FE and BE
- Prompt 005: GitHub setup (next)
