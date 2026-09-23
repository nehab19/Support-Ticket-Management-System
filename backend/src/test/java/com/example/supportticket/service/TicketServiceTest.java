package com.example.supportticket.service;

import com.example.supportticket.dto.*;
import com.example.supportticket.exception.InvalidStatusTransitionException;
import com.example.supportticket.exception.TicketNotFoundException;
import com.example.supportticket.model.Priority;
import com.example.supportticket.model.Ticket;
import com.example.supportticket.model.TicketStatus;
import com.example.supportticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketStateMachine stateMachine;

    @InjectMocks
    private TicketService ticketService;

    private Ticket sampleTicket;

    @BeforeEach
    void setUp() {
        sampleTicket = new Ticket();
        sampleTicket.setId(1L);
        sampleTicket.setTitle("Sample ticket");
        sampleTicket.setDescription("Sample description");
        sampleTicket.setPriority(Priority.MEDIUM);
        sampleTicket.setStatus(TicketStatus.OPEN);
        sampleTicket.setCreatedAt(Instant.now());
        sampleTicket.setUpdatedAt(Instant.now());
    }

    // --- createTicket ---

    @Test
    void createTicket_setsStatusToOpen() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("New ticket");
        request.setDescription("Description");
        request.setPriority(Priority.HIGH);

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(1L);
            t.setCreatedAt(Instant.now());
            t.setUpdatedAt(Instant.now());
            return t;
        });

        TicketDetailResponse result = ticketService.createTicket(request);

        assertThat(result.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void createTicket_defaultsPriorityToMedium_whenNotProvided() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("New ticket");
        request.setDescription("Description");
        // No priority set

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(1L);
            t.setCreatedAt(Instant.now());
            t.setUpdatedAt(Instant.now());
            return t;
        });

        TicketDetailResponse result = ticketService.createTicket(request);

        assertThat(result.getPriority()).isEqualTo(Priority.MEDIUM);
    }

    // --- listTickets ---

    @Test
    void listTickets_blankKeyword_throwsBadRequest() {
        assertThatThrownBy(() -> ticketService.listTickets(Optional.empty(), Optional.of("  ")))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void listTickets_emptyKeyword_throwsBadRequest() {
        assertThatThrownBy(() -> ticketService.listTickets(Optional.empty(), Optional.of("")))
                .isInstanceOf(ResponseStatusException.class);
    }

    // --- updateTicket ---

    @Test
    void updateTicket_partialUpdate_leavesUntouchedFieldsUnchanged() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(sampleTicket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setTitle("Updated title");
        // No description, priority, or assignee

        TicketDetailResponse result = ticketService.updateTicket(1L, request);

        assertThat(result.getTitle()).isEqualTo("Updated title");
        assertThat(result.getDescription()).isEqualTo("Sample description"); // unchanged
        assertThat(result.getPriority()).isEqualTo(Priority.MEDIUM); // unchanged
    }

    @Test
    void updateTicket_unknownId_throwsTicketNotFoundException() {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setTitle("Title");

        assertThatThrownBy(() -> ticketService.updateTicket(999L, request))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining("999");
    }

    // --- transitionStatus ---

    @Test
    void transitionStatus_delegatesToStateMachine() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(sampleTicket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        StatusTransitionRequest request = new StatusTransitionRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);

        ticketService.transitionStatus(1L, request);

        verify(stateMachine).validate(TicketStatus.OPEN, TicketStatus.IN_PROGRESS);
    }

    @Test
    void transitionStatus_propagatesInvalidTransitionException() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(sampleTicket));
        doThrow(new InvalidStatusTransitionException(TicketStatus.OPEN, TicketStatus.CLOSED))
                .when(stateMachine).validate(TicketStatus.OPEN, TicketStatus.CLOSED);

        StatusTransitionRequest request = new StatusTransitionRequest();
        request.setStatus(TicketStatus.CLOSED);

        assertThatThrownBy(() -> ticketService.transitionStatus(1L, request))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }
}
