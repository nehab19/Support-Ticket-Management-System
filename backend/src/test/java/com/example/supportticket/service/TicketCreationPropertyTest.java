package com.example.supportticket.service;

// Feature: support-ticket-management, Property 1: Ticket creation invariants

import com.example.supportticket.dto.CreateTicketRequest;
import com.example.supportticket.dto.TicketDetailResponse;
import com.example.supportticket.model.Priority;
import com.example.supportticket.model.Ticket;
import com.example.supportticket.model.TicketStatus;
import com.example.supportticket.repository.TicketRepository;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Property 1: Ticket creation invariants
 *
 * For any valid CreateTicketRequest (non-empty title ≤ 255 chars, non-empty description,
 * valid or absent priority), calling createTicket() SHALL produce a ticket where
 * status == OPEN, id is non-null, and createdAt is non-null.
 *
 * Validates: Requirements 1.1
 */
class TicketCreationPropertyTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketStateMachine stateMachine;

    private TicketService ticketService;

    private final AtomicLong idSequence = new AtomicLong(1L);

    @BeforeProperty
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ticketService = new TicketService(ticketRepository, stateMachine);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(idSequence.getAndIncrement());
            t.setCreatedAt(Instant.now());
            t.setUpdatedAt(Instant.now());
            return t;
        });
    }

    @Property(tries = 100)
    void ticketCreationInvariants(
            @ForAll("validTitles") String title,
            @ForAll("nonBlankStrings") String description,
            @ForAll("priorityOrNull") Priority priority
    ) {
        // Arrange
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setPriority(priority);

        // Act
        TicketDetailResponse result = ticketService.createTicket(request);

        // Assert — Property 1: status == OPEN, id non-null, createdAt non-null
        assertThat(result.getStatus())
                .as("Ticket status must be OPEN after creation")
                .isEqualTo(TicketStatus.OPEN);
        assertThat(result.getId())
                .as("Ticket id must be non-null after creation")
                .isNotNull();
        assertThat(result.getCreatedAt())
                .as("Ticket createdAt must be non-null after creation")
                .isNotNull();
    }

    /**
     * Generates non-blank strings of length 1–255 (valid title range).
     */
    @Provide
    Arbitrary<String> validTitles() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(255)
                .filter(s -> !s.isBlank());
    }

    /**
     * Generates non-blank strings for description.
     */
    @Provide
    Arbitrary<String> nonBlankStrings() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(500)
                .filter(s -> !s.isBlank());
    }

    /**
     * Generates a valid Priority enum value or null (absent priority).
     */
    @Provide
    Arbitrary<Priority> priorityOrNull() {
        return Arbitraries.of(Priority.class).injectNull(0.25);
    }
}
