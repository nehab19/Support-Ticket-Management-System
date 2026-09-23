package com.example.supportticket;

// Feature: support-ticket-management, Property 11: Blank keyword rejection

import com.example.supportticket.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 11: Blank keyword rejection
 *
 * For any string composed entirely of whitespace (including the empty string)
 * submitted as the search keyword {@code q}, the API SHALL return HTTP 400.
 *
 * Validates: Requirements 7.3
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BlankKeywordPropertyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/tickets";
    }

    /**
     * Sends a GET /api/tickets?q={rawQuery} where the value is already URL-encoded
     * (or a plain string) by building the URI with a literal query string to
     * preserve exact whitespace encoding.
     */
    private ResponseEntity<ErrorResponse> getWithRawQuery(String rawQueryString) {
        // Build the full URI with the raw query string as-is so whitespace
        // encoding (%20, %09, +, empty) is controlled precisely by the caller.
        URI uri = URI.create(baseUrl() + "?" + rawQueryString);
        return restTemplate.getForEntity(uri, ErrorResponse.class);
    }

    // -----------------------------------------------------------------------
    // Test 1: blankKeyword_whitespaceOnly_returns400
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 7.3
     *
     * Submitting whitespace-only strings as the {@code q} parameter must be
     * rejected with HTTP 400 and a non-null error message.
     *
     * Tested inputs (sent via URL-encoding):
     *   " "     → q=%20       (single space)
     *   "  "    → q=%20%20    (two spaces)
     *   "   "   → q=%20%20%20 (three spaces)
     *   "\t"    → q=%09       (tab character)
     *   "  \t " → q=%20%20%09%20 (mixed whitespace)
     */
    @Test
    void blankKeyword_whitespaceOnly_returns400() {
        String[][] inputs = {
            {"q=%20",          "single space (%20)"},
            {"q=%20%20",       "two spaces (%20%20)"},
            {"q=%20%20%20",    "three spaces (%20%20%20)"},
            {"q=%09",          "tab character (%09)"},
            {"q=%20%20%09%20", "mixed whitespace (%20%20%09%20)"},
        };

        for (String[] input : inputs) {
            String rawQuery = input[0];
            String description = input[1];

            ResponseEntity<ErrorResponse> response = getWithRawQuery(rawQuery);

            assertThat(response.getStatusCode())
                    .as("Whitespace keyword [%s] must return HTTP 400", description)
                    .isEqualTo(HttpStatus.BAD_REQUEST);

            assertThat(response.getBody())
                    .as("Response body must not be null for [%s]", description)
                    .isNotNull();

            assertThat(response.getBody().message())
                    .as("Response message must not be null for [%s]", description)
                    .isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // Test 2: emptyKeyword_returns400
    // -----------------------------------------------------------------------

    /**
     * Validates: Requirements 7.3
     *
     * Submitting an empty string as the {@code q} parameter (i.e. {@code ?q=})
     * must be rejected with HTTP 400 and a non-null error message.
     */
    @Test
    void emptyKeyword_returns400() {
        // ?q= sends an empty string value
        ResponseEntity<ErrorResponse> response = getWithRawQuery("q=");

        assertThat(response.getStatusCode())
                .as("Empty keyword (?q=) must return HTTP 400")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .as("Response body must not be null for empty keyword")
                .isNotNull();

        assertThat(response.getBody().message())
                .as("Response message must not be null for empty keyword")
                .isNotNull();
    }
}
