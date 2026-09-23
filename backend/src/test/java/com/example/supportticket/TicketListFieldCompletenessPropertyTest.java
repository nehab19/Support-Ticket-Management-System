package com.example.supportticket;

// Feature: support-ticket-management, Property 6: Ticket list field completeness

import com.example.supportticket.dto.CreateTicketRequest;
import com.example.supportticket.dto.TicketDetailResponse;
import com.example.supportticket.dto.TicketSummaryResponse;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 6: Ticket list field completeness
 *
 * Validates: Requirements 2.2, 7.4
 *
 * For any ticket in the system, its representation in the list response
 * (including search and filter results) SHALL include all of id, title,
 * priority, status, assignee, and createdAt.
 *
 * Note: assignee is nullable (NULL allowed), but id, title, priority,
 * status, and createdAt must be non-null.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class TicketListFieldCompletenessPropertyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/tickets";
    }

    /**
     * Helper: create a ticket and return the response body.
     * Newly created tickets have no assignee (assignee will be null).
     */
    private TicketDetailResponse createTicket(String title) {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle(title);
        request.setDescription("Description for field completeness test");
        request.setPriority(Priority.MEDIUM);

        ResponseEntity<TicketDetailResponse> response = restTemplate.postForEntity(
                baseUrl(), request, TicketDetailResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    /**
     * Helper: assert that a single TicketSummaryResponse has all required non-null fields.
     * assignee is nullable (may be null), but all other fields must be non-null.
     */
    private void assertRequiredFields(TicketSummaryResponse ticket) {
        assertThat(ticket.getId())
                .as("id must not be null")
                .isNotNull();

        assertThat(ticket.getTitle())
                .as("title must not be null or blank")
                .isNotNull()
                .isNotBlank();

        assertThat(ticket.getPriority())
                .as("priority must not be null (must be a valid enum value)")
                .isNotNull();

        assertThat(ticket.getStatus())
                .as("status must not be null (must be a valid enum value)")
                .isNotNull();

        assertThat(ticket.getCreatedAt())
                .as("createdAt must not be null")
                .isNotNull();

        // assignee is explicitly allowed to be null — no assertion needed
    }

    // -----------------------------------------------------------------------
    // Test 1: listTickets_allTicketsHaveRequiredFields
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 2.2
     *
     * Create 3 tickets (some with assignee, some without), then GET /api/tickets
     * and assert each item in the response has all required non-null fields.
     */
    @Test
    void listTickets_allTicketsHaveRequiredFields() {
        // Create 3 tickets (all without assignee, which is a nullable field)
        createTicket("Field completeness list test ticket 1 - " + System.nanoTime());
        createTicket("Field completeness list test ticket 2 - " + System.nanoTime());
        createTicket("Field completeness list test ticket 3 - " + System.nanoTime());

        // GET /api/tickets
        ResponseEntity<TicketSummaryResponse[]> response = restTemplate.getForEntity(
                baseUrl(), TicketSummaryResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<TicketSummaryResponse> tickets = List.of(response.getBody());
        assertThat(tickets).isNotEmpty();

        // Assert every ticket in the response has all required fields
        for (TicketSummaryResponse ticket : tickets) {
            assertRequiredFields(ticket);
        }
    }

    // -----------------------------------------------------------------------
    // Test 2: searchResults_allTicketsHaveRequiredFields
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 7.4
     *
     * Create 2 tickets with a unique keyword in title, then GET /api/tickets?q={keyword}
     * and assert each result has all required non-null fields.
     */
    @Test
    void searchResults_allTicketsHaveRequiredFields() {
        // Use a unique keyword unlikely to collide with other test data
        String uniqueKeyword = "FIELDCOMP" + System.nanoTime();

        // Create 2 tickets containing the keyword in their title
        createTicket(uniqueKeyword + " search test ticket A");
        createTicket(uniqueKeyword + " search test ticket B");

        // GET /api/tickets?q={keyword}
        ResponseEntity<TicketSummaryResponse[]> response = restTemplate.getForEntity(
                baseUrl() + "?q=" + uniqueKeyword, TicketSummaryResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<TicketSummaryResponse> results = List.of(response.getBody());
        assertThat(results)
                .as("Search results must contain the 2 tickets with keyword '%s'", uniqueKeyword)
                .hasSize(2);

        // Assert every ticket in the search results has all required fields
        for (TicketSummaryResponse ticket : results) {
            assertRequiredFields(ticket);
        }
    }

    // -----------------------------------------------------------------------
    // Test 3: filteredResults_allTicketsHaveRequiredFields
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 2.2, 7.4
     *
     * GET /api/tickets?status=OPEN and assert each result has all required non-null fields.
     * Newly created tickets are always OPEN, so there will be results from the other tests.
     */
    @Test
    void filteredResults_allTicketsHaveRequiredFields() {
        // Create a couple of OPEN tickets to ensure the filter has results
        createTicket("Field completeness filter test ticket X - " + System.nanoTime());
        createTicket("Field completeness filter test ticket Y - " + System.nanoTime());

        // GET /api/tickets?status=OPEN
        ResponseEntity<TicketSummaryResponse[]> response = restTemplate.getForEntity(
                baseUrl() + "?status=OPEN", TicketSummaryResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<TicketSummaryResponse> results = List.of(response.getBody());
        assertThat(results).isNotEmpty();

        // Assert every ticket in the filtered results has all required fields
        for (TicketSummaryResponse ticket : results) {
            assertRequiredFields(ticket);
        }
    }
}
