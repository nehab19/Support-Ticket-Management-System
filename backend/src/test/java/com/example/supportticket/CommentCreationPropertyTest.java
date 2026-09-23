package com.example.supportticket;

// Feature: support-ticket-management, Property 9: Comment creation round-trip

import com.example.supportticket.dto.CommentResponse;
import com.example.supportticket.dto.CreateCommentRequest;
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
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 9: Comment creation round-trip
 *
 * For any existing ticket and any valid CreateCommentRequest (non-empty author,
 * non-empty body), the created comment SHALL have a non-null id, a non-null createdAt,
 * and author/body values that exactly match the request.
 *
 * Validates: Requirements 6.1
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CommentCreationPropertyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/tickets";
    }

    /**
     * Helper: create a ticket to attach comments to.
     */
    private TicketDetailResponse createTicket() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Comment round-trip test ticket - " + System.nanoTime());
        request.setDescription("A ticket for property 9 comment creation tests");
        request.setPriority(Priority.MEDIUM);

        ResponseEntity<TicketDetailResponse> response = restTemplate.postForEntity(
                baseUrl(), request, TicketDetailResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    /**
     * Helper: post a comment and return the response.
     */
    private ResponseEntity<CommentResponse> postComment(Long ticketId, String author, String body) {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setAuthor(author);
        request.setBody(body);
        return restTemplate.postForEntity(
                baseUrl() + "/" + ticketId + "/comments",
                request,
                CommentResponse.class);
    }

    // -----------------------------------------------------------------------
    // Test 1: addComment_withValidRequest_returnsCommentWithMatchingAuthorAndBody
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 6.1
     *
     * For a variety of (author, body) combinations (short, long, special chars),
     * each created comment must have a non-null id, non-null createdAt, and
     * author/body that exactly match the request.
     */
    @Test
    void addComment_withValidRequest_returnsCommentWithMatchingAuthorAndBody() {
        TicketDetailResponse ticket = createTicket();
        Long ticketId = ticket.getId();

        // --- Input variants covering the property's input space ---

        // Short author name, short body
        assertCommentRoundTrip(ticketId, "alice", "Quick note.");

        // Long author name (50 chars)
        String longAuthor = "a".repeat(50);
        assertCommentRoundTrip(ticketId, longAuthor, "Comment from a user with a long name.");

        // Author with special characters
        assertCommentRoundTrip(ticketId, "bob_o'brien@example.com", "Special chars in author name.");

        // Short body
        assertCommentRoundTrip(ticketId, "charlie", "Ok.");

        // Long body (500 chars) — build by repeating a phrase until we have >= 500 chars, then trim
        String phrase = "This is a detailed investigation note. ";
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 500) {
            sb.append(phrase);
        }
        String longBody = sb.substring(0, 500);
        assertCommentRoundTrip(ticketId, "diana", longBody);

        // Numbers and punctuation in both author and body
        assertCommentRoundTrip(ticketId, "user123", "Issue #42: confirmed reproducible on v1.2.3.");

        // Unicode content
        assertCommentRoundTrip(ticketId, "élodie", "Résolu après investigation approfondie.");
    }

    /**
     * Helper: post a single comment and assert the round-trip invariants hold.
     *
     * Asserts:
     *   - HTTP 201 response
     *   - response.id != null
     *   - response.createdAt != null
     *   - response.author == request.author
     *   - response.body == request.body
     */
    private void assertCommentRoundTrip(Long ticketId, String author, String body) {
        ResponseEntity<CommentResponse> response = postComment(ticketId, author, body);

        assertThat(response.getStatusCode())
                .as("Adding comment (author='%s') should return HTTP 201", author)
                .isEqualTo(HttpStatus.CREATED);

        CommentResponse comment = response.getBody();
        assertThat(comment)
                .as("Response body must not be null for author='%s'", author)
                .isNotNull();

        assertThat(comment.getId())
                .as("Comment id must be non-null (author='%s')", author)
                .isNotNull();

        assertThat(comment.getCreatedAt())
                .as("Comment createdAt must be non-null (author='%s')", author)
                .isNotNull();

        assertThat(comment.getAuthor())
                .as("Comment author must exactly match request author")
                .isEqualTo(author);

        assertThat(comment.getBody())
                .as("Comment body must exactly match request body")
                .isEqualTo(body);
    }

    // -----------------------------------------------------------------------
    // Test 2: addComment_toNonExistentTicket_returns404
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 6.4
     *
     * POSTing a comment to a non-existent ticket id must return HTTP 404.
     */
    @Test
    void addComment_toNonExistentTicket_returns404() {
        ResponseEntity<Object> response = restTemplate.postForEntity(
                baseUrl() + "/999999/comments",
                buildRequest("alice", "This ticket does not exist"),
                Object.class);

        assertThat(response.getStatusCode())
                .as("Adding comment to non-existent ticket must return HTTP 404")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * Helper: build a CreateCommentRequest without using the full postComment helper.
     */
    private CreateCommentRequest buildRequest(String author, String body) {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setAuthor(author);
        request.setBody(body);
        return request;
    }
}
