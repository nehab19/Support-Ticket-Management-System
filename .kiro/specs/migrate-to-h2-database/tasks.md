# Implementation Plan: Migrate to H2 Database

## Overview

This implementation plan migrates the support ticket management system from PostgreSQL to H2 database across all environments. The migration eliminates external database dependencies while maintaining all existing functionality. Changes are limited to Maven dependencies and configuration files - no application code modifications required.

## Tasks

- [x] 1. Update Maven dependencies
  - Remove PostgreSQL and Flyway-PostgreSQL dependencies from pom.xml
  - Change H2 dependency scope from test to runtime
  - Verify build completes successfully without PostgreSQL libraries
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3_

- [ ]* 1.1 Verify dependency changes with build test
  - Run `mvn clean compile` to ensure no compilation errors
  - Confirm PostgreSQL driver not in classpath
  - Confirm H2 driver available at runtime
  - _Requirements: 1.3, 1.4, 2.2_

- [x] 2. Update production configuration files
  - [x] 2.1 Modify application-prod.properties
    - Change spring.datasource.driver-class-name to org.h2.Driver
    - Change spring.jpa.database-platform to org.hibernate.dialect.H2Dialect
    - Update spring.datasource.url with H2 file-based URL and default value
    - Add MODE=PostgreSQL and DB_CLOSE_DELAY=-1 parameters to URL
    - Set default credentials (username: sa, password: empty)
    - _Requirements: 3.1, 3.4, 3.5, 8.1, 8.2, 9.3, 9.4_
  
  - [x] 2.2 Update .env.example template
    - Replace PostgreSQL connection examples with H2 file-based examples
    - Remove PostgreSQL-specific parameters (host, port)
    - Document H2 URL format with relative and absolute path examples
    - Add comment explaining credentials are optional for H2
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 3. Checkpoint - Verify configuration changes
  - Review application-prod.properties for correct H2 driver and dialect
  - Review .env.example for accurate H2 documentation
  - Ensure test configuration (application-test.properties) remains unchanged
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 4.1, 4.2_

- [x] 4. Test Flyway migration compatibility
  - [x] 4.1 Run FlywayMigrationTest against H2
    - Execute existing FlywayMigrationTest
    - Verify V1__create_ticket_table.sql applies successfully
    - Verify V2__create_comment_table.sql applies successfully
    - Confirm schema version table created and tracked correctly
    - _Requirements: 6.1, 6.2, 6.3_
  
  - [ ]* 4.2 Verify PostgreSQL syntax compatibility
    - Check that BIGSERIAL translates correctly with MODE=PostgreSQL
    - Check that now() functions work correctly
    - Check that TIMESTAMP WITH TIME ZONE preserves data correctly
    - _Requirements: 6.1, 6.4_

- [x] 5. Run full test suite for functional equivalence
  - [x] 5.1 Execute all existing unit and integration tests
    - Run `mvn test` to execute entire test suite
    - Verify all 20+ tests pass without modification
    - Confirm no test failures introduced by H2 migration
    - _Requirements: 7.1, 4.3, 4.4_
  
  - [ ]* 5.2 Verify property-based tests pass
    - Confirm all 12 property tests execute successfully against H2
    - Verify business logic correctness maintained (state machine, validation, etc.)
    - _Requirements: 7.1, 7.4, 7.5_
  
  - [ ]* 5.3 Verify CRUD operations and data integrity
    - Run TicketIntegrationTest to verify ticket operations
    - Verify comment creation and retrieval works correctly
    - Verify timestamp precision and timezone handling preserved
    - _Requirements: 7.2, 7.3, 7.6_

- [x] 6. Checkpoint - Ensure all tests pass
  - Confirm zero test failures
  - Review test output for any warnings or deprecations
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 7.1_

- [x] 7. Verify production startup and data persistence
  - [x] 7.1 Test application startup with production profile
    - Start application with production profile locally
    - Verify H2 database files created in configured location (./data/supporttickets.mv.db)
    - Verify Flyway migrations execute on first startup
    - Confirm application starts without errors
    - _Requirements: 3.1, 3.2, 8.2, 8.3, 10.1, 10.2, 10.3_
  
  - [ ]* 7.2 Verify data persistence across restarts
    - Create a test ticket via REST API
    - Add a comment to the ticket
    - Stop application
    - Restart application
    - Verify ticket and comment still exist
    - _Requirements: 3.3, 7.2, 7.3_
  
  - [ ]* 7.3 Test environment variable configuration
    - Start application without DB_URL environment variable (verify default used)
    - Start application with custom DB_URL (verify custom path used)
    - Verify database files created at correct locations
    - _Requirements: 8.1, 8.2, 8.3_

- [x] 8. Final checkpoint - Complete migration verification
  - Review migration verification checklist from design document
  - Confirm all configuration files updated correctly
  - Confirm all tests pass
  - Confirm application runs and data persists
  - Ensure all tests pass, ask the user if questions arise.
  - _Requirements: All requirements satisfied_

## Notes

- Tasks marked with `*` are optional and can be skipped for faster implementation
- All changes are infrastructure-only: no application code modifications required
- Existing test configuration already uses H2 and requires no changes
- MODE=PostgreSQL compatibility mode eliminates need to rewrite Flyway migrations
- For multi-instance deployments, consider PostgreSQL or H2 server mode instead of file-based storage
