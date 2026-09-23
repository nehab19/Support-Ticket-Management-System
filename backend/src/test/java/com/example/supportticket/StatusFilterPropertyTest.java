package com.example.supportticket;

// Feature: support-ticket-management, Property 12: Status filter correctness

import com.example.supportticket.dto.CreateTicketRequest;
import com.example.supportticket.dto.StatusTransitionRequest;
import com.example.supportticket.dto.TicketDetailResponse;
import com.example.supportticket.dto.TicketSummaryResponse;
import com.example.supportticket.model.Priority;
import com.example.supportticket.model.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 12: Status filter correctness
 *
 * For any valid TicketStatus value and any corpus of tickets with mixed statuses,
 * the filter response SHALL contain only tickets whose status equals the requested
 * value, ordered by createdAt descending, or an empty array when none match.
 *
 * Validates: Requirements 8.1, 8.3
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class StatusFilterPropertyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/tickets";
    }

    /**
     * Helper: create a ticket with the given title and return the full response.
     * Small sleep ensures distinct createdAt timestamps in H2.
     */
    private TicketDetailResponse createTicket(String title) throws InterruptedException {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle(title);
        request.setDescription("Description for status filter property test");
        request.setPriority(Priority.MEDIUM);

        ResponseEntity<TicketDetailResponse> response = restTemplate.postForEntity(
                baseUrl(), request, TicketDetailResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        // Small delay to guarantee distinct createdAt timestamps in H2
        Thread.sleep(5);
        return response.getBody();
    }

    /**
     * Helper: transition a ticket to the given status via PATCH /api/tickets/{id}/status.
     * Apache HttpClient 5 (on classpath) enables PATCH support in TestRestTemplate.
     */
    private TicketDetailResponse transitionStatus(Long ticketId, TicketStatus newStatus) {
        StatusTransitionRequest request = new StatusTransitionRequest();
        request.setStatus(newStatus);

        HttpEntity<StatusTransitionRequest> entity = new HttpEntity<>(request);
        ResponseEntity<TicketDetailResponse> response = restTemplate.exchange(
                baseUrl() + "/" + ticketId + "/status",
                HttpMethod.PATCH,
                entity,
                TicketDetailResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    /**
     * Helper: filter tickets by status and return the list of summary responses.
     */
    private List<TicketSummaryResponse> filterByStatus(TicketStatus status) {
        ResponseEntity<List<TicketSummaryResponse>> response = restTemplate.exchange(
                baseUrl() + "?status=" + status.name(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    /**
     * Helper: assert that a list of TicketSummaryResponse is sorted by createdAt descending.
     * For every adjacent pair, list[i].createdAt >= list[i+1].createdAt.
     */
    private void assertDescendingOrder(List<TicketSummaryResponse> tickets) {
        for (int i = 0; i < tickets.size() - 1; i++) {
            Instant current = tickets.get(i).getCreatedAt();
            Instant next = tickets.get(i + 1).getCreatedAt();
            assertThat(current)
                    .as("tickets[%d].createdAt (%s) should be >= tickets[%d].createdAt (%s)",
                            i, current, i + 1, next)
                    .isAfterOrEqualTo(next);
        }
    }

    // -----------------------------------------------------------------------
    // Test 1: filterByOpen_returnsOnlyOpenTickets
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 8.1, 8.3
     *
     * Create 3 OPEN tickets and 2 tickets that get transitioned to IN_PROGRESS.
     * Filter by OPEN; assert only OPEN tickets are returned and the 3 OPEN IDs
     * are contained in the results.
     */
    @Test
    void filterByOpen_returnsOnlyOpenTickets() throws InterruptedException {
        // 3 tickets that remain OPEN
        TicketDetailResponse open1 = createTicket("OPEN filter test ticket A - " + System.nanoTime());
        TicketDetailResponse open2 = createTicket("OPEN filter test ticket B - " + System.nanoTime());
        TicketDetailResponse open3 = createTicket("OPEN filter test ticket C - " + System.nanoTime());

        // 2 tickets transitioned to IN_PROGRESS
        TicketDetailResponse inProg1 = createTicket("IN_PROGRESS filter test ticket D - " + System.nanoTime());
        TicketDetailResponse inProg2 = createTicket("IN_PROGRESS filter test ticket E - " + System.nanoTime());
        transitionStatus(inProg1.getId(), TicketStatus.IN_PROGRESS);
        transitionStatus(inProg2.getId(), TicketStatus.IN_PROGRESS);

        Set<Long> expectedOpenIds = Set.of(open1.getId(), open2.getId(), open3.getId());
        Set<Long> inProgressIds = Set.of(inProg1.getId(), inProg2.getId());

        List<TicketSummaryResponse> results = filterByStatus(TicketStatus.OPEN);

        // All returned tickets must have OPEN status (no cross-status leakage)
        assertThat(results)
                .as("All tickets returned by status=OPEN filter must have OPEN status")
                .allMatch(t -> t.getStatus() == TicketStatus.OPEN);

        // The 3 OPEN ticket IDs must be contained in the results
        Set<Long> returnedIds = results.stream()
                .map(TicketSummaryResponse::getId)
                .collect(Collectors.toSet());
        assertThat(returnedIds)
                .as("Results must contain all 3 OPEN tickets")
                .containsAll(expectedOpenIds);

        // IN_PROGRESS tickets must NOT be in the results
        assertThat(returnedIds)
                .as("IN_PROGRESS tickets must NOT appear in OPEN filter results")
                .doesNotContainAnyElementsOf(inProgressIds);

        // Results must be ordered by createdAt descending
        assertDescendingOrder(results);
    }

    // -----------------------------------------------------------------------
    // Test 2: filterByInProgress_returnsOnlyInProgressTickets
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 8.1, 8.3
     *
     * Create 3 tickets, transition all 3 to IN_PROGRESS.
     * Create 2 more OPEN tickets.
     * Filter by IN_PROGRESS; assert only the 3 IN_PROGRESS tickets are returned.
     */
    @Test
    void filterByInProgress_returnsOnlyInProgressTickets() throws InterruptedException {
        // 3 tickets transitioned to IN_PROGRESS
        TicketDetailResponse ip1 = createTicket("IN_PROGRESS filter test 1 - " + System.nanoTime());
        TicketDetailResponse ip2 = createTicket("IN_PROGRESS filter test 2 - " + System.nanoTime());
        TicketDetailResponse ip3 = createTicket("IN_PROGRESS filter test 3 - " + System.nanoTime());
        transitionStatus(ip1.getId(), TicketStatus.IN_PROGRESS);
        transitionStatus(ip2.getId(), TicketStatus.IN_PROGRESS);
        transitionStatus(ip3.getId(), TicketStatus.IN_PROGRESS);

        // 2 more tickets that remain OPEN
        TicketDetailResponse openX = createTicket("OPEN filter test X - " + System.nanoTime());
        TicketDetailResponse openY = createTicket("OPEN filter test Y - " + System.nanoTime());

        Set<Long> expectedInProgressIds = Set.of(ip1.getId(), ip2.getId(), ip3.getId());
        Set<Long> openIds = Set.of(openX.getId(), openY.getId());

        List<TicketSummaryResponse> results = filterByStatus(TicketStatus.IN_PROGRESS);

        // All returned tickets must have IN_PROGRESS status
        assertThat(results)
                .as("All tickets returned by status=IN_PROGRESS filter must have IN_PROGRESS status")
                .allMatch(t -> t.getStatus() == TicketStatus.IN_PROGRESS);

        // The 3 IN_PROGRESS IDs must be contained in the results
        Set<Long> returnedIds = results.stream()
                .map(TicketSummaryResponse::getId)
                .collect(Collectors.toSet());
        assertThat(returnedIds)
                .as("Results must contain all 3 IN_PROGRESS tickets")
                .containsAll(expectedInProgressIds);

        // OPEN tickets must NOT be in the results
        assertThat(returnedIds)
                .as("OPEN tickets must NOT appear in IN_PROGRESS filter results")
                .doesNotContainAnyElementsOf(openIds);

        // Results must be ordered by createdAt descending
        assertDescendingOrder(results);
    }

    // -----------------------------------------------------------------------
    // Test 3: filterByCancelled_returnsOnlyCancelledTickets
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 8.1, 8.3
     *
     * Create 2 tickets and cancel them (OPEN → CANCELLED).
     * Filter by CANCELLED; assert only cancelled tickets are in the results.
     */
    @Test
    void filterByCancelled_returnsOnlyCancelledTickets() throws InterruptedException {
        // 2 tickets transitioned to CANCELLED (OPEN → CANCELLED is allowed)
        TicketDetailResponse cancel1 = createTicket("CANCELLED filter test 1 - " + System.nanoTime());
        TicketDetailResponse cancel2 = createTicket("CANCELLED filter test 2 - " + System.nanoTime());
        transitionStatus(cancel1.getId(), TicketStatus.CANCELLED);
        transitionStatus(cancel2.getId(), TicketStatus.CANCELLED);

        Set<Long> expectedCancelledIds = Set.of(cancel1.getId(), cancel2.getId());

        List<TicketSummaryResponse> results = filterByStatus(TicketStatus.CANCELLED);

        // All returned tickets must have CANCELLED status
        assertThat(results)
                .as("All tickets returned by status=CANCELLED filter must have CANCELLED status")
                .allMatch(t -> t.getStatus() == TicketStatus.CANCELLED);

        // The 2 cancelled ticket IDs must be contained in the results
        Set<Long> returnedIds = results.stream()
                .map(TicketSummaryResponse::getId)
                .collect(Collectors.toSet());
        assertThat(returnedIds)
                .as("Results must contain both CANCELLED tickets")
                .containsAll(expectedCancelledIds);

        // Results must be ordered by createdAt descending
        if (results.size() > 1) {
            assertDescendingOrder(results);
        }
    }

    // -----------------------------------------------------------------------
    // Test 4: filterByStatus_noMatchingTickets_returnsEmptyArray
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 8.1, 8.3
     *
     * Filter by CLOSED when no CLOSED tickets exist in this fresh context.
     * Assert HTTP 200 with an empty array.
     *
     * NOTE: @DirtiesContext(BEFORE_CLASS) starts this test class with a clean DB,
     * and no other test in this class transitions anything to CLOSED, so filtering
     * by CLOSED at the start should yield zero results.
     */
    @Test
    void filterByStatus_noMatchingTickets_returnsEmptyArray() {
        // RESOLVED tickets would need OPEN→IN_PROGRESS→RESOLVED transitions.
        // Use CLOSED: requires OPEN→IN_PROGRESS→RESOLVED→CLOSED; no test does that.
        // However, since tests may run in any order, we query first and assert zero
        // CLOSED tickets by virtue of the fresh context (no test creates CLOSED tickets).
        ResponseEntity<List<TicketSummaryResponse>> response = restTemplate.exchange(
                baseUrl() + "?status=CLOSED",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode())
                .as("Filter by status with no matching tickets must return HTTP 200")
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .as("Filter by status with no matching tickets must return an empty array")
                .isNotNull()
                .isEmpty();
    }

    // -----------------------------------------------------------------------
    // Test 5: filterResults_areOrderedByCreatedAtDescending
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 8.1
     *
     * Create 3 tickets with delays between creations, keep them OPEN.
     * Filter by OPEN and assert descending createdAt order for the full list.
     */
    @Test
    void filterResults_areOrderedByCreatedAtDescending() throws InterruptedException {
        // Create 3 tickets with small delays to guarantee distinct createdAt timestamps
        TicketDetailResponse t1 = createTicket("Ordering test order-1 - " + System.nanoTime());
        TicketDetailResponse t2 = createTicket("Ordering test order-2 - " + System.nanoTime());
        TicketDetailResponse t3 = createTicket("Ordering test order-3 - " + System.nanoTime());

        List<Long> createdIds = List.of(t1.getId(), t2.getId(), t3.getId());

        List<TicketSummaryResponse> results = filterByStatus(TicketStatus.OPEN);

        // All returned tickets must have OPEN status
        assertThat(results)
                .as("All tickets returned by status=OPEN filter must have OPEN status")
                .allMatch(t -> t.getStatus() == TicketStatus.OPEN);

        // All 3 created OPEN tickets must be in the results
        Set<Long> returnedIds = results.stream()
                .map(TicketSummaryResponse::getId)
                .collect(Collectors.toSet());
        assertThat(returnedIds)
                .as("Results must contain all 3 newly created OPEN tickets")
                .containsAll(createdIds);

        // The full filtered list must be ordered descending by createdAt
        assertThat(results)
                .as("Filter results must not be empty")
                .isNotEmpty();
        assertDescendingOrder(results);
    }
}
