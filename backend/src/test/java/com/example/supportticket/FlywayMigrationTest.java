package com.example.supportticket;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest {

    @Autowired
    private Flyway flyway;

    @Test
    void allMigrations_areApplied_andNonePending() {
        MigrationInfo[] infos = flyway.info().all();

        assertThat(infos).isNotEmpty();

        long pendingCount = java.util.Arrays.stream(infos)
                .filter(m -> m.getState() == MigrationState.PENDING)
                .count();

        assertThat(pendingCount)
                .as("There should be zero pending Flyway migrations on startup")
                .isEqualTo(0);
    }

    @Test
    void migrations_V1andV2_areBothSuccessful() {
        MigrationInfo[] infos = flyway.info().applied();

        assertThat(infos).hasSizeGreaterThanOrEqualTo(2);

        boolean v1Applied = java.util.Arrays.stream(infos)
                .anyMatch(m -> m.getVersion() != null
                        && m.getVersion().getVersion().equals("1")
                        && m.getState() == MigrationState.SUCCESS);
        boolean v2Applied = java.util.Arrays.stream(infos)
                .anyMatch(m -> m.getVersion() != null
                        && m.getVersion().getVersion().equals("2")
                        && m.getState() == MigrationState.SUCCESS);

        assertThat(v1Applied).as("V1 migration (ticket table) should be applied").isTrue();
        assertThat(v2Applied).as("V2 migration (comment table) should be applied").isTrue();
    }
}
