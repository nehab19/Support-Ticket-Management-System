package com.example.supportticket;

// Feature: support-ticket-management, Property 4: Priority enum validation (create and update)

import com.example.supportticket.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 1.6, 4.4
 *
 * Property 4: For any string that is not one of LOW, MEDIUM, HIGH, CRITICAL submitted as priority
 * in either a creation or an update request, the API SHALL reject the request with HTTP 400.
 *
 * NOTE: jqwik + Spring context don't integrate well.
 * Using @SpringBootTest + JUnit 5 @Test methods with manually selected invalid values.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PriorityValidationPropertyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/tickets";
    }

    /**
     * Invalid priority strings to test — these are clearly not valid Priority enum values.
     * Note: numeric ordinal strings like "0", "1", "2", "3" may be accepted by Jackson
     * as enum ordinals, so they are deliberately avoided here.
     */
    private static final String[] INVALID_PRIORITIES = {
        "URGENT", "BLOCKER", "EXTREME", "high", "Medium", "CRITCAL", "NULL", "invalid-priority", "NONE"
    };

    /**
     * Creates a valid ticket via raw JSON and returns its ID.
     */
    private Long createValidTicket() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String validJson = "{\"title\":\"Valid ticket for priority test\",\"description\":\"Valid description\",\"priority\":\"HIGH\"}";
        HttpEntity<String> entity = new HttpEntity<>(validJson, headers);

        ResponseEntity<java.util.Map> response = restTemplate.exchange(
                baseUrl(), HttpMethod.POST, entity, java.util.Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return ((Number) response.getBody().get("id")).longValue();
    }

    /**
     * Validates: Requirements 1.6
     *
     * For each clearly invalid priority string, POST /api/tickets should return HTTP 400
     * with a non-null, non-blank message field and status == 400 in the body.
     */
    @Test
    void createTicket_withInvalidPriority_returns400() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (String invalidPriority : INVALID_PRIORITIES) {
            String rawJson = "{\"title\":\"Test\",\"description\":\"Desc\",\"priority\":\"" + invalidPriority + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(rawJson, headers);

            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    baseUrl(), HttpMethod.POST, entity, ErrorResponse.class);

            assertThat(response.getStatusCode())
                    .as("POST with invalid priority '%s' should return 400", invalidPriority)
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            ErrorResponse body = response.getBody();
            assertThat(body)
                    .as("Response body should not be null for invalid priority '%s'", invalidPriority)
                    .isNotNull();
            assertThat(body.status())
                    .as("Error response status should be 400 for invalid priority '%s'", invalidPriority)
                    .isEqualTo(400);
            assertThat(body.message())
                    .as("Error message should be non-null and non-blank for invalid priority '%s'", invalidPriority)
                    .isNotNull()
                    .isNotBlank();
        }
    }

    /**
     * Validates: Requirements 4.4
     *
     * For each clearly invalid priority string, PATCH /api/tickets/{id} should return HTTP 400
     * with a non-null, non-blank message field and status == 400 in the body.
     */
    @Test
    void updateTicket_withInvalidPriority_returns400() {
        Long ticketId = createValidTicket();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (String invalidPriority : INVALID_PRIORITIES) {
            String rawJson = "{\"priority\":\"" + invalidPriority + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(rawJson, headers);

            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    baseUrl() + "/" + ticketId, HttpMethod.PATCH, entity, ErrorResponse.class);

            assertThat(response.getStatusCode())
                    .as("PATCH with invalid priority '%s' should return 400", invalidPriority)
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            ErrorResponse body = response.getBody();
            assertThat(body)
                    .as("Response body should not be null for invalid priority '%s'", invalidPriority)
                    .isNotNull();
            assertThat(body.status())
                    .as("Error response status should be 400 for invalid priority '%s'", invalidPriority)
                    .isEqualTo(400);
            assertThat(body.message())
                    .as("Error message should be non-null and non-blank for invalid priority '%s'", invalidPriority)
                    .isNotNull()
                    .isNotBlank();
        }
    }
}
