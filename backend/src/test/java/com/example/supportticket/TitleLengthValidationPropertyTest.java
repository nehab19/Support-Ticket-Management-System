package com.example.supportticket;

// Feature: support-ticket-management, Property 3: Title length validation (create and update)

import com.example.supportticket.dto.*;
import com.example.supportticket.model.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates: Requirements 1.5, 4.3
 *
 * Property 3: For any string whose length exceeds 255 characters submitted as title
 * in either a creation or an update request, the API SHALL reject the request with HTTP 400.
 *
 * NOTE: jqwik + Spring context don't integrate well.
 * Using @SpringBootTest + JUnit 5 @Test methods with manually generated inputs.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TitleLengthValidationPropertyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/tickets";
    }

    private String titleOfLength(int length) {
        return "a".repeat(length);
    }

    private TicketDetailResponse createValidTicket() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Valid title for update test " + System.nanoTime());
        request.setDescription("A valid description for property test");
        request.setPriority(Priority.MEDIUM);

        ResponseEntity<TicketDetailResponse> response = restTemplate.postForEntity(
                baseUrl(), request, TicketDetailResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    /**
     * Validates: Requirements 1.5
     *
     * For titles of lengths [256, 300, 500, 1000], POST /api/tickets should return HTTP 400
     * with an errors list containing a "title" field error.
     */
    @Test
    void createTicket_withTitleLongerThan255Chars_returns400() {
        int[] lengths = {256, 300, 500, 1000};

        for (int length : lengths) {
            String longTitle = titleOfLength(length);

            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle(longTitle);
            request.setDescription("A valid description");
            request.setPriority(Priority.MEDIUM);

            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                    baseUrl(), request, ErrorResponse.class);

            assertThat(response.getStatusCode())
                    .as("POST with title of length %d should return 400", length)
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            ErrorResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.status())
                    .as("Error response status should be 400 for title length %d", length)
                    .isEqualTo(400);
            assertThat(body.message())
                    .as("Error message should be non-null and non-blank for title length %d", length)
                    .isNotNull()
                    .isNotBlank();
            assertThat(body.errors())
                    .as("Errors list should contain a 'title' field error for title length %d", length)
                    .anyMatch(fe -> "title".equals(fe.field()));
        }
    }

    /**
     * Validates: Requirements 4.3
     *
     * For titles of lengths [256, 300, 500, 1000], PATCH /api/tickets/{id} should return HTTP 400
     * with an errors list containing a "title" field error.
     */
    @Test
    void updateTicket_withTitleLongerThan255Chars_returns400() {
        // First create a valid ticket to update
        TicketDetailResponse created = createValidTicket();
        Long ticketId = created.getId();

        int[] lengths = {256, 300, 500, 1000};

        for (int length : lengths) {
            String longTitle = titleOfLength(length);

            UpdateTicketRequest updateRequest = new UpdateTicketRequest();
            updateRequest.setTitle(longTitle);

            HttpEntity<UpdateTicketRequest> entity = new HttpEntity<>(updateRequest);
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    baseUrl() + "/" + ticketId, HttpMethod.PATCH, entity, ErrorResponse.class);

            assertThat(response.getStatusCode())
                    .as("PATCH with title of length %d should return 400", length)
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            ErrorResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.status())
                    .as("Error response status should be 400 for title length %d", length)
                    .isEqualTo(400);
            assertThat(body.message())
                    .as("Error message should be non-null and non-blank for title length %d", length)
                    .isNotNull()
                    .isNotBlank();
            assertThat(body.errors())
                    .as("Errors list should contain a 'title' field error for title length %d", length)
                    .anyMatch(fe -> "title".equals(fe.field()));
        }
    }
}
