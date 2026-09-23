package com.example.supportticket;

// Feature: support-ticket-management, Property 10: Keyword search correctness

import com.example.supportticket.dto.CreateTicketRequest;
import com.example.supportticket.dto.TicketDetailResponse;
import com.example.supportticket.dto.TicketSummaryResponse;
import com.example.supportticket.model.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 10: Keyword search correctness
 *
 * For any keyword and any corpus of tickets, the search response SHALL contain
 * exactly the set of tickets whose title or description contains the keyword
 * using a case-insensitive match — no more, no fewer — or an empty array when
 * no tickets match.
 *
 * Validates: Requirements 7.1, 7.2
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class KeywordSearchPropertyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/tickets";
    }

    /**
     * Helper: create a ticket with the given title and description, returning its ID.
     */
    private Long createTicket(String title, String description) {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setPriority(Priority.MEDIUM);

        ResponseEntity<TicketDetailResponse> response = restTemplate.postForEntity(
                baseUrl(), request, TicketDetailResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().getId();
    }

    /**
     * Helper: perform a keyword search and return the list of matching summary responses.
     */
    private List<TicketSummaryResponse> search(String keyword) {
        ResponseEntity<List<TicketSummaryResponse>> response = restTemplate.exchange(
                baseUrl() + "?q=" + keyword,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    // -----------------------------------------------------------------------
    // Test 1: search_byTitleKeyword_returnsExactlyMatchingTickets
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 7.1, 7.2
     *
     * Create 5 tickets where 3 have a unique keyword in title and 2 don't.
     * Search by keyword; assert exactly 3 results, all containing the keyword,
     * and the non-matching tickets are NOT included.
     */
    @Test
    void search_byTitleKeyword_returnsExactlyMatchingTickets() {
        String keyword = "TITLEKW" + System.nanoTime();

        // 3 tickets that SHOULD match (keyword in title)
        Long id1 = createTicket("Ticket with " + keyword + " in title", "Some unrelated description A");
        Long id2 = createTicket(keyword + " prefix ticket", "Some unrelated description B");
        Long id3 = createTicket("Suffix ticket " + keyword, "Some unrelated description C");

        // 2 tickets that should NOT match (no keyword anywhere)
        Long noMatchId1 = createTicket("Completely different title Alpha " + System.nanoTime(), "Alpha description text");
        Long noMatchId2 = createTicket("Another unrelated title Beta " + System.nanoTime(), "Beta description text");

        Set<Long> expectedIds = Set.of(id1, id2, id3);
        Set<Long> nonMatchingIds = Set.of(noMatchId1, noMatchId2);

        List<TicketSummaryResponse> results = search(keyword);

        // Assert exactly 3 results
        assertThat(results)
                .as("Search for keyword '%s' must return exactly 3 matching tickets", keyword)
                .hasSize(3);

        Set<Long> returnedIds = results.stream()
                .map(TicketSummaryResponse::getId)
                .collect(Collectors.toSet());

        // Assert returned IDs are exactly the expected matching ones
        assertThat(returnedIds)
                .as("Returned IDs must match exactly the 3 tickets with the keyword in title")
                .containsExactlyInAnyOrderElementsOf(expectedIds);

        // Assert the non-matching tickets are NOT in the results
        assertThat(returnedIds)
                .as("Non-matching tickets must NOT appear in search results")
                .doesNotContainAnyElementsOf(nonMatchingIds);
    }

    // -----------------------------------------------------------------------
    // Test 2: search_byDescriptionKeyword_returnsExactlyMatchingTickets
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 7.1, 7.2
     *
     * Create tickets where 2 have keyword in description (not title).
     * Assert exactly 2 results are returned.
     */
    @Test
    void search_byDescriptionKeyword_returnsExactlyMatchingTickets() {
        String keyword = "DESCKW" + System.nanoTime();

        // 2 tickets that SHOULD match (keyword in description only)
        Long id1 = createTicket("Regular title one " + System.nanoTime(), "Description contains " + keyword + " here");
        Long id2 = createTicket("Regular title two " + System.nanoTime(), keyword + " at start of description");

        // 2 tickets that should NOT match (keyword not in title or description)
        Long noMatchId1 = createTicket("Non-matching title X " + System.nanoTime(), "Non-matching description X");
        Long noMatchId2 = createTicket("Non-matching title Y " + System.nanoTime(), "Non-matching description Y");

        Set<Long> expectedIds = Set.of(id1, id2);
        Set<Long> nonMatchingIds = Set.of(noMatchId1, noMatchId2);

        List<TicketSummaryResponse> results = search(keyword);

        // Assert exactly 2 results
        assertThat(results)
                .as("Search for keyword '%s' must return exactly 2 tickets with keyword in description", keyword)
                .hasSize(2);

        Set<Long> returnedIds = results.stream()
                .map(TicketSummaryResponse::getId)
                .collect(Collectors.toSet());

        // Assert returned IDs are exactly the expected matching ones
        assertThat(returnedIds)
                .as("Returned IDs must match exactly the 2 tickets with keyword in description")
                .containsExactlyInAnyOrderElementsOf(expectedIds);

        // Assert the non-matching tickets are NOT in the results
        assertThat(returnedIds)
                .as("Non-matching tickets must NOT appear in search results")
                .doesNotContainAnyElementsOf(nonMatchingIds);
    }

    // -----------------------------------------------------------------------
    // Test 3: search_caseInsensitive_matchesRegardlessOfCase
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 7.1
     *
     * Create a ticket with title containing "UPPERCASE". Assert that searching
     * for "uppercase", "UPPERCASE", and "UpperCase" all find the ticket.
     */
    @Test
    void search_caseInsensitive_matchesRegardlessOfCase() {
        String uniquePart = "CASETEST" + System.nanoTime();
        // Store the word in uppercase in the title
        String titleKeyword = "UPPERCASE" + uniquePart;
        Long matchingId = createTicket("Ticket with " + titleKeyword + " in title", "Plain description without the word");

        // Search lowercase — must find the ticket
        List<TicketSummaryResponse> resultsLower = search("uppercase" + uniquePart);
        Set<Long> idsLower = resultsLower.stream()
                .map(TicketSummaryResponse::getId)
                .collect(Collectors.toSet());
        assertThat(idsLower)
                .as("Lowercase search must find ticket with UPPERCASE title keyword")
                .contains(matchingId);

        // Search uppercase — must find the ticket
        List<TicketSummaryResponse> resultsUpper = search("UPPERCASE" + uniquePart);
        Set<Long> idsUpper = resultsUpper.stream()
                .map(TicketSummaryResponse::getId)
                .collect(Collectors.toSet());
        assertThat(idsUpper)
                .as("Uppercase search must find ticket with UPPERCASE title keyword")
                .contains(matchingId);

        // Search mixed-case — must find the ticket
        List<TicketSummaryResponse> resultsMixed = search("UpperCase" + uniquePart);
        Set<Long> idsMixed = resultsMixed.stream()
                .map(TicketSummaryResponse::getId)
                .collect(Collectors.toSet());
        assertThat(idsMixed)
                .as("Mixed-case search must find ticket with UPPERCASE title keyword")
                .contains(matchingId);
    }

    // -----------------------------------------------------------------------
    // Test 4: search_noMatchingKeyword_returnsEmptyArray
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 7.2
     *
     * Search for a random unique string that no ticket has; assert empty array
     * returned with HTTP 200.
     */
    @Test
    void search_noMatchingKeyword_returnsEmptyArray() {
        // Use a highly unique keyword that no ticket in the DB can possibly have
        String uniqueKeyword = "NOMATCH_XYZZY_" + System.nanoTime() + "_UNIQUE";

        ResponseEntity<List<TicketSummaryResponse>> response = restTemplate.exchange(
                baseUrl() + "?q=" + uniqueKeyword,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode())
                .as("Search with no matching keyword must return HTTP 200")
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .as("Search with no matching keyword must return an empty array")
                .isNotNull()
                .isEmpty();
    }
}
