package com.example.supportticket;

// Feature: support-ticket-management, Property 2: Unique ticket identifiers

import com.example.supportticket.dto.CreateTicketRequest;
import com.example.supportticket.dto.TicketDetailResponse;
import com.example.supportticket.model.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 1.2
 *
 * Property 2: For any N valid ticket creation requests submitted sequentially,
 * the resulting N tickets SHALL each have a distinct id.
 *
 * NOTE: jqwik + Spring context don't integrate well.
 * Using @SpringBootTest + JUnit 5 @Test methods with explicit N-ticket scenarios.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class TicketIdUniquenessPropertyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/tickets";
    }

    /**
     * Helper: create a ticket and return the response body.
     */
    private TicketDetailResponse createTicket(String title) {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle(title);
        request.setDescription("Description for uniqueness property test");
        request.setPriority(Priority.MEDIUM);

        ResponseEntity<TicketDetailResponse> response = restTemplate.postForEntity(
                baseUrl(), request, TicketDetailResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    /**
     * Validates: Requirements 1.2
     *
     * Create 5 tickets sequentially; collect all IDs; assert they are all distinct (no duplicates).
     */
    @Test
    void createNTickets_allHaveDistinctIds_smallN() {
        int n = 5;
        List<Long> ids = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            TicketDetailResponse created = createTicket("Uniqueness test small-N ticket " + i + " - " + System.nanoTime());
            ids.add(created.getId());
        }

        // Assert all N creations succeeded
        assertThat(ids).hasSize(n);

        // Assert all IDs are non-null
        assertThat(ids).doesNotContainNull();

        // Assert all IDs are distinct (no duplicates)
        assertThat(ids).doesNotHaveDuplicates();
    }

    /**
     * Validates: Requirements 1.2
     *
     * Create 20 tickets sequentially; collect all IDs; assert they are all distinct (no duplicates).
     */
    @Test
    void createNTickets_allHaveDistinctIds_largerN() {
        int n = 20;
        List<Long> ids = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            TicketDetailResponse created = createTicket("Uniqueness test larger-N ticket " + i + " - " + System.nanoTime());
            ids.add(created.getId());
        }

        // Assert all N creations succeeded
        assertThat(ids).hasSize(n);

        // Assert all IDs are non-null
        assertThat(ids).doesNotContainNull();

        // Assert all IDs are distinct (no duplicates)
        assertThat(ids).doesNotHaveDuplicates();
    }

    /**
     * Validates: Requirements 1.2
     *
     * Create 50 tickets sequentially; collect all IDs; assert they are all distinct (no duplicates).
     */
    @Test
    void createNTickets_allHaveDistinctIds_veryLargeN() {
        int n = 50;
        List<Long> ids = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            TicketDetailResponse created = createTicket("Uniqueness test very-large-N ticket " + i + " - " + System.nanoTime());
            ids.add(created.getId());
        }

        // Assert all N creations succeeded
        assertThat(ids).hasSize(n);

        // Assert all IDs are non-null
        assertThat(ids).doesNotContainNull();

        // Assert all IDs are distinct (no duplicates)
        assertThat(ids).doesNotHaveDuplicates();
    }
}
