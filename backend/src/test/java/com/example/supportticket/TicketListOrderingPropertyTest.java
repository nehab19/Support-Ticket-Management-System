package com.example.supportticket;

// Feature: support-ticket-management, Property 5: Ticket list ordering

import com.example.supportticket.dto.CreateTicketRequest;
import com.example.supportticket.dto.TicketDetailResponse;
import com.example.supportticket.dto.TicketSummaryResponse;
import com.example.supportticket.model.Priority;
import com.example.supportticket.model.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 2.1, 8.1
 *
 * Property 5: For any set of N tickets with distinct createdAt timestamps, the list
 * returned by GET /api/tickets SHALL be ordered such that for every adjacent pair,
 * list[i].createdAt >= list[i+1].createdAt (descending).
 *
 * NOTE: jqwik + Spring context don't integrate well.
 * Using @SpringBootTest + JUnit 5 @Test methods with explicit N-ticket scenarios.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class TicketListOrderingPropertyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/tickets";
    }

    /**
     * Helper: create a ticket and return the response body.
     * Uses a small sleep between creations to ensure distinct createdAt timestamps.
     */
    private TicketDetailResponse createTicket(String title) throws InterruptedException {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle(title);
        request.setDescription("Description for ordering property test");
        request.setPriority(Priority.MEDIUM);

        ResponseEntity<TicketDetailResponse> response = restTemplate.postForEntity(
                baseUrl(), request, TicketDetailResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        // Small delay to guarantee distinct createdAt timestamps in the H2 DB
        Thread.sleep(5);
        return response.getBody();
    }

    /**
     * Helper: assert that a list of TicketSummaryResponse is sorted by createdAt descending.
     * For every adjacent pair, list[i].createdAt >= list[i+1].createdAt.
     */
    private void assertDescendingOrder(List<TicketSummaryResponse> tickets) {
        assertThat(tickets).isNotEmpty();
        for (int i = 0; i < tickets.size() - 1; i++) {
            Instant current = tickets.get(i).getCreatedAt();
            Instant next = tickets.get(i + 1).getCreatedAt();
            assertThat(current)
                    .as("tickets[%d].createdAt (%s) should be >= tickets[%d].createdAt (%s)",
                            i, current, i + 1, next)
                    .isAfterOrEqualTo(next);
        }
    }

    /**
     * Validates: Requirements 2.1
     *
     * Create 3 tickets sequentially, then GET /api/tickets and assert
     * the full response is ordered descending by createdAt.
     */
    @Test
    void ticketList_isOrderedByCreatedAtDescending_forSmallN() throws InterruptedException {
        // Create 3 tickets with distinct timestamps (small N)
        List<Long> createdIds = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            TicketDetailResponse created = createTicket("Ordering test small-N ticket " + i + " - " + System.nanoTime());
            createdIds.add(created.getId());
        }

        // Fetch the full list
        ResponseEntity<TicketSummaryResponse[]> response = restTemplate.getForEntity(
                baseUrl(), TicketSummaryResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<TicketSummaryResponse> allTickets = List.of(response.getBody());
        assertThat(allTickets).isNotEmpty();

        // The full list must be ordered descending by createdAt (Property 5)
        assertDescendingOrder(allTickets);

        // Also verify all 3 created tickets are present in the response
        List<Long> returnedIds = allTickets.stream()
                .map(TicketSummaryResponse::getId)
                .toList();
        assertThat(returnedIds).containsAll(createdIds);
    }

    /**
     * Validates: Requirements 2.1
     *
     * Create 7 tickets sequentially, then GET /api/tickets and assert
     * the full response is ordered descending by createdAt (larger N).
     */
    @Test
    void ticketList_isOrderedByCreatedAtDescending_forLargerN() throws InterruptedException {
        // Create 7 tickets with distinct timestamps (larger N)
        List<Long> createdIds = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            TicketDetailResponse created = createTicket("Ordering test large-N ticket " + i + " - " + System.nanoTime());
            createdIds.add(created.getId());
        }

        // Fetch the full list
        ResponseEntity<TicketSummaryResponse[]> response = restTemplate.getForEntity(
                baseUrl(), TicketSummaryResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<TicketSummaryResponse> allTickets = List.of(response.getBody());
        assertThat(allTickets).isNotEmpty();

        // The full list must be ordered descending by createdAt (Property 5)
        assertDescendingOrder(allTickets);

        // Also verify all 7 created tickets are present in the response
        List<Long> returnedIds = allTickets.stream()
                .map(TicketSummaryResponse::getId)
                .toList();
        assertThat(returnedIds).containsAll(createdIds);
    }

    /**
     * Validates: Requirements 8.1
     *
     * Create 3 OPEN tickets, then GET /api/tickets?status=OPEN and assert
     * the filtered response is ordered descending by createdAt.
     */
    @Test
    void statusFilteredList_isOrderedByCreatedAtDescending() throws InterruptedException {
        // Create 3 OPEN tickets (fresh tickets are always OPEN)
        List<Long> createdIds = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            TicketDetailResponse created = createTicket("Ordering test status-filter ticket " + i + " - " + System.nanoTime());
            createdIds.add(created.getId());
        }

        // Fetch filtered list by status=OPEN
        ResponseEntity<TicketSummaryResponse[]> response = restTemplate.getForEntity(
                baseUrl() + "?status=OPEN", TicketSummaryResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<TicketSummaryResponse> openTickets = List.of(response.getBody());
        assertThat(openTickets).isNotEmpty();

        // All returned tickets must have OPEN status
        assertThat(openTickets)
                .allMatch(t -> t.getStatus() == TicketStatus.OPEN,
                        "All tickets in status=OPEN filter must have OPEN status");

        // The filtered list must be ordered descending by createdAt (Property 5 / Requirement 8.1)
        assertDescendingOrder(openTickets);

        // Also verify all 3 created tickets are present
        List<Long> returnedIds = openTickets.stream()
                .map(TicketSummaryResponse::getId)
                .toList();
        assertThat(returnedIds).containsAll(createdIds);
    }
}
