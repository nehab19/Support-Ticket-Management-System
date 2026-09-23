package com.example.supportticket.service;

// Feature: support-ticket-management, Property 7: Partial update preserves untouched fields

import com.example.supportticket.dto.TicketDetailResponse;
import com.example.supportticket.dto.UpdateTicketRequest;
import com.example.supportticket.model.Priority;
import com.example.supportticket.model.Ticket;
import com.example.supportticket.model.TicketStatus;
import com.example.supportticket.repository.TicketRepository;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Property 7: Partial update preserves untouched fields
 *
 * For any existing ticket and any partial update request containing a non-empty strict subset
 * of {title, description, priority, assignee}, the API SHALL update only the specified fields
 * and leave all other fields unchanged.
 *
 * Validates: Requirements 4.5
 */
class PartialUpdatePropertyTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketStateMachine stateMachine;

    private TicketService ticketService;

    @BeforeProperty
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ticketService = new TicketService(ticketRepository, stateMachine);
    }

    @Property(tries = 100)
    void partialUpdatePreservesUntouchedFields(
            @ForAll("existingTickets") Ticket existing,
            @ForAll("partialUpdateRequests") UpdateTicketRequest request
    ) {
        // Capture the original field values before update
        String originalTitle = existing.getTitle();
        String originalDescription = existing.getDescription();
        Priority originalPriority = existing.getPriority();
        String originalAssignee = existing.getAssignee();

        // Mock repository to return the existing ticket and save it as-is (service mutates it)
        when(ticketRepository.findById(any())).thenReturn(Optional.of(existing));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TicketDetailResponse result = ticketService.updateTicket(existing.getId(), request);

        // Assert: fields set in request WERE changed; null fields in request were NOT changed
        if (request.getTitle() != null) {
            assertThat(result.getTitle())
                    .as("Title should be updated when request.getTitle() is non-null")
                    .isEqualTo(request.getTitle());
        } else {
            assertThat(result.getTitle())
                    .as("Title should be unchanged when request.getTitle() is null")
                    .isEqualTo(originalTitle);
        }

        if (request.getDescription() != null) {
            assertThat(result.getDescription())
                    .as("Description should be updated when request.getDescription() is non-null")
                    .isEqualTo(request.getDescription());
        } else {
            assertThat(result.getDescription())
                    .as("Description should be unchanged when request.getDescription() is null")
                    .isEqualTo(originalDescription);
        }

        if (request.getPriority() != null) {
            assertThat(result.getPriority())
                    .as("Priority should be updated when request.getPriority() is non-null")
                    .isEqualTo(request.getPriority());
        } else {
            assertThat(result.getPriority())
                    .as("Priority should be unchanged when request.getPriority() is null")
                    .isEqualTo(originalPriority);
        }

        if (request.getAssignee() != null) {
            assertThat(result.getAssignee())
                    .as("Assignee should be updated when request.getAssignee() is non-null")
                    .isEqualTo(request.getAssignee());
        } else {
            assertThat(result.getAssignee())
                    .as("Assignee should be unchanged when request.getAssignee() is null")
                    .isEqualTo(originalAssignee);
        }
    }

    /**
     * Generates Ticket objects with non-null title, description, priority, status, and assignee.
     */
    @Provide
    Arbitrary<Ticket> existingTickets() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 10_000L);
        Arbitrary<String> titles = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(100)
                .filter(s -> !s.isBlank());
        Arbitrary<String> descriptions = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(200)
                .filter(s -> !s.isBlank());
        Arbitrary<Priority> priorities = Arbitraries.of(Priority.class);
        Arbitrary<TicketStatus> statuses = Arbitraries.of(TicketStatus.class);
        Arbitrary<String> assignees = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(50)
                .filter(s -> !s.isBlank());

        return Combinators.combine(ids, titles, descriptions, priorities, statuses, assignees)
                .as((id, title, description, priority, status, assignee) -> {
                    Ticket ticket = new Ticket();
                    ticket.setId(id);
                    ticket.setTitle(title);
                    ticket.setDescription(description);
                    ticket.setPriority(priority);
                    ticket.setStatus(status);
                    ticket.setAssignee(assignee);
                    ticket.setCreatedAt(Instant.now());
                    ticket.setUpdatedAt(Instant.now());
                    return ticket;
                });
    }

    /**
     * Generates UpdateTicketRequest where at least one field is non-null and at least one
     * field is null (strict subset of {title, description, priority, assignee}).
     * This ensures the update is partial — not all fields are set and not none are set.
     */
    @Provide
    Arbitrary<UpdateTicketRequest> partialUpdateRequests() {
        Arbitrary<String> titles = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(100)
                .filter(s -> !s.isBlank())
                .injectNull(0.5);
        Arbitrary<String> descriptions = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(200)
                .filter(s -> !s.isBlank())
                .injectNull(0.5);
        Arbitrary<Priority> priorities = Arbitraries.of(Priority.class)
                .injectNull(0.5);
        Arbitrary<String> assignees = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(50)
                .filter(s -> !s.isBlank())
                .injectNull(0.5);

        return Combinators.combine(titles, descriptions, priorities, assignees)
                .as((title, description, priority, assignee) -> {
                    UpdateTicketRequest req = new UpdateTicketRequest();
                    req.setTitle(title);
                    req.setDescription(description);
                    req.setPriority(priority);
                    req.setAssignee(assignee);
                    return req;
                })
                // Strict subset: at least one non-null (not empty update) and at least one null (not full update)
                .filter(req ->
                        (req.getTitle() != null || req.getDescription() != null
                                || req.getPriority() != null || req.getAssignee() != null)
                        &&
                        (req.getTitle() == null || req.getDescription() == null
                                || req.getPriority() == null || req.getAssignee() == null)
                );
    }
}
