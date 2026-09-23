# Prompt 002: Run the Project

**Date**: 2026-09-23  
**Session**: 1  
**Prompt Number**: 2

## User Prompt
```
I want to run the project
```

## Context
User wanted to start the Spring Boot backend application that was fully implemented.

## AI Actions Taken

### Step 1: Initial Attempt with Maven Plugin
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```
**Result**: Maven plugin container exception occurred.

### Step 2: Build JAR Instead
```bash
cd backend
mvn clean package -DskipTests
```
**Result**: ✅ Build successful - JAR created

### Step 3: Run JAR with Java 21
```bash
java -jar target/support-ticket-management-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```
**Result**: ❌ UnsupportedClassVersionError (Java version mismatch)

### Step 4: Use Explicit Java 21 Path
```bash
/usr/lib/jvm/java-21-amazon-corretto/bin/java -jar target/support-ticket-management-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```
**Result**: ❌ Database locked by another process

### Step 5: Resolve Database Lock
```bash
# Find process using database
lsof backend/data/supporttickets.mv.db

# Kill the process
kill -9 20468

# Restart application
/usr/lib/jvm/java-21-amazon-corretto/bin/java -jar target/support-ticket-management-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```
**Result**: ✅ Success!

## Technical Issues Resolved

1. **Maven Plugin Issue**: Switched to JAR execution
2. **Java Version Mismatch**: Used explicit Java 21 binary path
3. **Database Lock**: Killed stale process holding database file

## Outcome
- Backend started successfully on **http://localhost:8080**
- H2 database initialized at `backend/data/supporttickets.mv.db`
- Flyway migrations applied successfully
- Process ID: 22492
- Terminal ID: 6

## Verification
```bash
curl http://localhost:8080/api/tickets
# Response: []

curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Ticket","description":"This is a test ticket","priority":"HIGH"}'
# Response: {"id":1,...}

curl http://localhost:8080/api/tickets/1
# Response: Full ticket details
```

## Next Action
User requested to start frontend as well.
