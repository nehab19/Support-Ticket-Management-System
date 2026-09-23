package com.example.supportticket;

// Feature: support-ticket-management, Property 13: Error response completeness

import com.example.supportticket.dto.CreateTicketRequest;
import com.example.supportticket.dto.ErrorResponse;
import com.example.supportticket.dto.StatusTransitionRequest;
import com.example.supportticket.dto.TicketDetailResponse;
import com.example.supportticket.model.Priority;
import com.example.supportticket.model.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 13: Error response completeness
 *
 * Validates: Requirements 10.1, 10.2
 *
 * For any request that fails validation (HTTP 400), not-found (HTTP 404), or a business
 * rule violation (HTTP 422), the response body SHALL contain:
 * - a `message` field with a human-readable description
 * - a `status` field matching the HTTP response code
 * - for HTTP 400 responses with field-level violations — an `errors` array with at least
 *   one entry per failing field
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ErrorResponseCompletenessPropertyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String ticketsUrl() {
        return "http://localhost:" + port + "/api/tickets";
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private TicketDetailResponse createTicket(String title) {
        CreateTicketRequest req = new CreateTicketRequest();
        req.setTitle(title);
        req.setDescription("A description for error completeness test");
        req.setPriority(Priority.MEDIUM);
        ResponseEntity<TicketDetailResponse> resp =
                restTemplate.postForEntity(ticketsUrl(), req, TicketDetailResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody();
    }

    private void assertErrorResponseShape(ErrorResponse body, int expectedStatus) {
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(expectedStatus);
        assertThat(body.message()).isNotNull();
        assertThat(body.message()).isNotBlank();
        // errors list must always be non-null (may be empty for non-400 cases)
        assertThat(body.errors()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // Scenario 1 — HTTP 400: missing required fields (empty body)
    // -----------------------------------------------------------------------

    @Test
    void missingRequiredFields_allVariants_return400WithCompleteErrorResponse() {
        // Multiple inputs that should each produce HTTP 400
        Object[] invalidBodies = {
                Map.of(),                           // completely empty → missing title + description
                Map.of("description", "some desc"), // missing title
                Map.of("title", "some title")        // missing description
        };

        for (Object body : invalidBodies) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    ticketsUrl(), HttpMethod.POST, entity, ErrorResponse.class);

            assertThat(response.getStatusCode())
                    .as("Expected HTTP 400 for body: %s", body)
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            ErrorResponse errorResponse = response.getBody();
            assertErrorResponseShape(errorResponse, 400);

            // For missing-field validation errors, the errors list must have ≥ 1 entry
            assertThat(errorResponse.errors())
                    .as("errors array must have at least one entry for body: %s", body)
                    .isNotEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // Scenario 2 — HTTP 400: title exceeds maximum length (256+ chars)
    // -----------------------------------------------------------------------

    @Test
    void titleTooLong_multipleVariants_return400WithTitleFieldError() {
        // Test several over-length titles (property: any title > 255 chars must return 400)
        int[] lengths = {256, 300, 500, 1000};

        for (int length : lengths) {
            String longTitle = "x".repeat(length);
            CreateTicketRequest req = new CreateTicketRequest();
            req.setTitle(longTitle);
            req.setDescription("A valid description");

            ResponseEntity<ErrorResponse> response =
                    restTemplate.postForEntity(ticketsUrl(), req, ErrorResponse.class);

            assertThat(response.getStatusCode())
                    .as("Expected HTTP 400 for title of length %d", length)
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            ErrorResponse errorResponse = response.getBody();
            assertErrorResponseShape(errorResponse, 400);

            // The errors array must contain an entry for the "title" field
            assertThat(errorResponse.errors())
                    .as("errors array must have an entry for 'title' when title is %d chars", length)
                    .isNotEmpty();
            assertThat(errorResponse.errors())
                    .anyMatch(fe -> "title".equals(fe.field()));
        }
    }

    // -----------------------------------------------------------------------
    // Scenario 3 — HTTP 400: invalid priority enum value
    // -----------------------------------------------------------------------

    @Test
    void invalidPriorityEnum_multipleVariants_return400WithCompleteErrorResponse() {
        // These strings are not valid Priority enum values and are not ordinal-resolvable
        // Note: numeric strings like "1" are accepted via ordinal mapping (Jackson default),
        // so we only test truly invalid string values that cannot be resolved to a valid enum.
        String[] invalidPriorities = {"URGENT", "BLOCKER", "EXTREME", "HIGH!"};

        for (String invalidPriority : invalidPriorities) {
            // Send raw JSON so the invalid string reaches the deserialiser
            String json = String.format(
                    "{\"title\":\"Test title\",\"description\":\"Test desc\",\"priority\":\"%s\"}",
                    invalidPriority);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            // Use String response type to avoid deserialization issues with raw response
            ResponseEntity<String> response = restTemplate.exchange(
                    ticketsUrl(), HttpMethod.POST, entity, String.class);

            assertThat(response.getStatusCode())
                    .as("Expected HTTP 400 for invalid priority '%s'", invalidPriority)
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            // Parse the raw JSON string to verify the error shape
            String body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body).contains("\"status\":400");
            assertThat(body).contains("\"message\":");
        }
    }

    // -----------------------------------------------------------------------
    // Scenario 4 — HTTP 404: unknown ticket ID
    // -----------------------------------------------------------------------

    @Test
    void unknownTicketId_multipleVariants_return404WithCompleteErrorResponse() {
        long[] unknownIds = {999999L, 888888L, Long.MAX_VALUE};

        for (long id : unknownIds) {
            ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                    ticketsUrl() + "/" + id, ErrorResponse.class);

            assertThat(response.getStatusCode())
                    .as("Expected HTTP 404 for unknown ticket id %d", id)
                    .isEqualTo(HttpStatus.NOT_FOUND);

            ErrorResponse errorResponse = response.getBody();
            assertErrorResponseShape(errorResponse, 404);
        }
    }

    // -----------------------------------------------------------------------
    // Scenario 5 — HTTP 422: invalid status transition
    // -----------------------------------------------------------------------

    @Test
    void invalidStatusTransition_multipleVariants_return422WithCompleteErrorResponse() {
        // Pairs of (current→disallowed target) — these are all invalid transitions
        TicketStatus[][] disallowedTransitions = {
                // OPEN tickets cannot go directly to CLOSED or RESOLVED
                {null, TicketStatus.CLOSED},
                {null, TicketStatus.RESOLVED},
        };

        for (TicketStatus[] pair : disallowedTransitions) {
            // Create a fresh ticket (starts as OPEN)
            TicketDetailResponse ticket = createTicket("Transition-error test " + System.nanoTime());
            assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);

            TicketStatus targetStatus = pair[1];
            StatusTransitionRequest req = new StatusTransitionRequest();
            req.setStatus(targetStatus);

            HttpEntity<StatusTransitionRequest> entity = new HttpEntity<>(req);
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    ticketsUrl() + "/" + ticket.getId() + "/status",
                    HttpMethod.PATCH, entity, ErrorResponse.class);

            assertThat(response.getStatusCode())
                    .as("Expected HTTP 422 for OPEN → %s", targetStatus)
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

            ErrorResponse errorResponse = response.getBody();
            assertErrorResponseShape(errorResponse, 422);

            // Message must mention both status names
            assertThat(errorResponse.message())
                    .as("Error message should mention 'OPEN'")
                    .contains("OPEN");
            assertThat(errorResponse.message())
                    .as("Error message should mention target status '%s'", targetStatus)
                    .contains(targetStatus.name());
        }
    }

    // -----------------------------------------------------------------------
    // Scenario 6 — HTTP 400: empty body {} produces errors for each missing field
    // -----------------------------------------------------------------------

    @Test
    void emptyBody_returns400_withErrorsForEachMissingField() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                ticketsUrl(), HttpMethod.POST, entity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ErrorResponse errorResponse = response.getBody();
        assertErrorResponseShape(errorResponse, 400);

        // Both title and description are required — errors list must have ≥ 2 entries
        assertThat(errorResponse.errors())
                .as("errors array must have entries for all failing fields (title + description)")
                .hasSizeGreaterThanOrEqualTo(2);

        // Verify both failing fields are represented
        assertThat(errorResponse.errors()).anyMatch(fe -> "title".equals(fe.field()));
        assertThat(errorResponse.errors()).anyMatch(fe -> "description".equals(fe.field()));

        // Each field error must itself have a non-blank message
        errorResponse.errors().forEach(fe -> {
            assertThat(fe.field()).isNotBlank();
            assertThat(fe.message()).isNotBlank();
        });
    }

    // -----------------------------------------------------------------------
    // Scenario 7 — HTTP 400: invalid enum in query parameter
    // -----------------------------------------------------------------------

    @Test
    void invalidStatusQueryParam_return400WithCompleteErrorResponse() {
        String[] invalidStatuses = {"INVALID", "PENDING", "done", "open1"};

        for (String invalidStatus : invalidStatuses) {
            ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                    ticketsUrl() + "?status=" + invalidStatus, ErrorResponse.class);

            assertThat(response.getStatusCode())
                    .as("Expected HTTP 400 for invalid status param '%s'", invalidStatus)
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            ErrorResponse errorResponse = response.getBody();
            assertErrorResponseShape(errorResponse, 400);
        }
    }
}
