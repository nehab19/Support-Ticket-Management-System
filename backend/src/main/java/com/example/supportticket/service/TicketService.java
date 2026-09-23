package com.example.supportticket.service;

import com.example.supportticket.dto.*;
import com.example.supportticket.exception.TicketNotFoundException;
import com.example.supportticket.model.Priority;
import com.example.supportticket.model.Ticket;
import com.example.supportticket.model.TicketStatus;
import com.example.supportticket.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketStateMachine stateMachine;

    public TicketService(TicketRepository ticketRepository, TicketStateMachine stateMachine) {
        this.ticketRepository = ticketRepository;
        this.stateMachine = stateMachine;
    }

    public TicketDetailResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        ticket.setStatus(TicketStatus.OPEN);
        Ticket saved = ticketRepository.save(ticket);
        return toDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketSummaryResponse> listTickets(Optional<TicketStatus> status, Optional<String> q) {
        if (q.isPresent()) {
            String keyword = q.get();
            if (keyword.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search keyword must not be blank");
            }
            return ticketRepository
                    .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(keyword, keyword)
                    .stream().map(this::toSummaryResponse).toList();
        }
        if (status.isPresent()) {
            return ticketRepository.findByStatusOrderByCreatedAtDesc(status.get())
                    .stream().map(this::toSummaryResponse).toList();
        }
        return ticketRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toSummaryResponse).toList();
    }

    @Transactional(readOnly = true)
    public TicketDetailResponse getTicket(Long id) {
        return toDetailResponse(findById(id));
    }

    public TicketDetailResponse updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = findById(id);
        if (request.getTitle() != null) ticket.setTitle(request.getTitle());
        if (request.getDescription() != null) ticket.setDescription(request.getDescription());
        if (request.getPriority() != null) ticket.setPriority(request.getPriority());
        if (request.getAssignee() != null) ticket.setAssignee(request.getAssignee());
        return toDetailResponse(ticketRepository.save(ticket));
    }

    public TicketDetailResponse transitionStatus(Long id, StatusTransitionRequest request) {
        Ticket ticket = findById(id);
        stateMachine.validate(ticket.getStatus(), request.getStatus());
        ticket.setStatus(request.getStatus());
        return toDetailResponse(ticketRepository.save(ticket));
    }

    private Ticket findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    private TicketSummaryResponse toSummaryResponse(Ticket t) {
        return new TicketSummaryResponse(t.getId(), t.getTitle(), t.getPriority(),
                t.getStatus(), t.getAssignee(), t.getCreatedAt());
    }

    private TicketDetailResponse toDetailResponse(Ticket t) {
        List<CommentResponse> comments = t.getComments().stream()
                .map(c -> new CommentResponse(c.getId(), c.getAuthor(), c.getBody(), c.getCreatedAt()))
                .toList();
        return new TicketDetailResponse(t.getId(), t.getTitle(), t.getDescription(),
                t.getPriority(), t.getStatus(), t.getAssignee(),
                t.getCreatedAt(), t.getUpdatedAt(), comments);
    }
}
