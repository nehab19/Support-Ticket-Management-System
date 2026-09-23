package com.example.supportticket;

import com.example.supportticket.dto.*;
import com.example.supportticket.model.Priority;
import com.example.supportticket.model.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TicketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/tickets";
    }

    private TicketDetailResponse createSampleTicket(String title) {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle(title);
        request.setDescription("A sample description");
        request.setPriority(Priority.HIGH);

        ResponseEntity<TicketDetailResponse> response = restTemplate.postForEntity(
                baseUrl(), request, TicketDetailResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    // --- Create ---

    @Test
    void createTicket_validRequest_returns201_withOpenStatus() {
        TicketDetailResponse ticket = createSampleTicket("Integration test ticket");

        assertThat(ticket).isNotNull();
        assertThat(ticket.getId()).isNotNull();
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(ticket.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(ticket.getCreatedAt()).isNotNull();
    }

    @Test
    void createTicket_missingTitle_returns400() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setDescription("desc");

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                baseUrl(), request, ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    // --- List ---

    @Test
    void listTickets_returnsCreatedTickets() {
        createSampleTicket("Ticket for list test " + System.nanoTime());

        ResponseEntity<List<TicketSummaryResponse>> response = restTemplate.exchange(
                baseUrl(), HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    // --- Get detail ---

    @Test
    void getTicket_existingId_returnsFullDetail() {
        TicketDetailResponse created = createSampleTicket("Detail test " + System.nanoTime());

        ResponseEntity<TicketDetailResponse> response = restTemplate.getForEntity(
                baseUrl() + "/" + created.getId(), TicketDetailResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(created.getId());
        assertThat(response.getBody().getDescription()).isEqualTo("A sample description");
        assertThat(response.getBody().getComments()).isNotNull();
    }

    @Test
    void getTicket_unknownId_returns404() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                baseUrl() + "/999999", ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- Update ---

    @Test
    void updateTicket_partialUpdate_changesOnlySpecifiedFields() {
        TicketDetailResponse created = createSampleTicket("Update test " + System.nanoTime());

        UpdateTicketRequest update = new UpdateTicketRequest();
        update.setTitle("Updated title");
        update.setAssignee("alice");

        HttpEntity<UpdateTicketRequest> entity = new HttpEntity<>(update);
        ResponseEntity<TicketDetailResponse> response = restTemplate.exchange(
                baseUrl() + "/" + created.getId(), HttpMethod.PATCH, entity, TicketDetailResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTitle()).isEqualTo("Updated title");
        assertThat(response.getBody().getAssignee()).isEqualTo("alice");
        assertThat(response.getBody().getDescription()).isEqualTo("A sample description"); // unchanged
    }

    // --- Status transitions ---

    @Test
    void statusTransition_validTransition_updatesStatus() {
        TicketDetailResponse created = createSampleTicket("Transition test " + System.nanoTime());

        StatusTransitionRequest request = new StatusTransitionRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);

        HttpEntity<StatusTransitionRequest> entity = new HttpEntity<>(request);
        ResponseEntity<TicketDetailResponse> response = restTemplate.exchange(
                baseUrl() + "/" + created.getId() + "/status", HttpMethod.PATCH, entity, TicketDetailResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void statusTransition_invalidTransition_returns422() {
        TicketDetailResponse created = createSampleTicket("Bad transition " + System.nanoTime());

        StatusTransitionRequest request = new StatusTransitionRequest();
        request.setStatus(TicketStatus.CLOSED); // OPEN → CLOSED is not allowed

        HttpEntity<StatusTransitionRequest> entity = new HttpEntity<>(request);
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                baseUrl() + "/" + created.getId() + "/status", HttpMethod.PATCH, entity, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().status()).isEqualTo(422);
        assertThat(response.getBody().message()).contains("OPEN");
        assertThat(response.getBody().message()).contains("CLOSED");
    }

    // --- Comments ---

    @Test
    void addComment_validRequest_returns201() {
        TicketDetailResponse ticket = createSampleTicket("Comment test " + System.nanoTime());

        CreateCommentRequest comment = new CreateCommentRequest();
        comment.setAuthor("bob");
        comment.setBody("This is a comment");

        ResponseEntity<CommentResponse> response = restTemplate.postForEntity(
                baseUrl() + "/" + ticket.getId() + "/comments", comment, CommentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getAuthor()).isEqualTo("bob");
        assertThat(response.getBody().getBody()).isEqualTo("This is a comment");
    }

    @Test
    void addComment_missingBody_returns400() {
        TicketDetailResponse ticket = createSampleTicket("Comment validation test " + System.nanoTime());

        CreateCommentRequest comment = new CreateCommentRequest();
        comment.setAuthor("alice");
        // Missing body

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                baseUrl() + "/" + ticket.getId() + "/comments", comment, ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // --- Search ---

    @Test
    void searchByKeyword_returnsMatchingTickets() {
        String uniqueTitle = "UniquePhraseXYZ" + System.nanoTime();
        createSampleTicket(uniqueTitle);

        ResponseEntity<List<TicketSummaryResponse>> response = restTemplate.exchange(
                baseUrl() + "?q=" + uniqueTitle, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).anyMatch(t -> t.getTitle().contains(uniqueTitle));
    }

    @Test
    void searchByKeyword_blank_returns400() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                baseUrl() + "?q=   ", ErrorResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // --- Status filter ---

    @Test
    void filterByStatus_returnsOnlyMatchingTickets() {
        createSampleTicket("Filter test " + System.nanoTime());

        ResponseEntity<List<TicketSummaryResponse>> response = restTemplate.exchange(
                baseUrl() + "?status=OPEN", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).allMatch(t -> t.getStatus() == TicketStatus.OPEN);
    }
}
