package com.example.supportticket.controller;

import com.example.supportticket.dto.*;
import com.example.supportticket.model.TicketStatus;
import com.example.supportticket.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketDetailResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(request));
    }

    @GetMapping
    public ResponseEntity<List<TicketSummaryResponse>> listTickets(
            @RequestParam Optional<TicketStatus> status,
            @RequestParam Optional<String> q) {
        return ResponseEntity.ok(ticketService.listTickets(status, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailResponse> getTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicket(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TicketDetailResponse> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketDetailResponse> transitionStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusTransitionRequest request) {
        return ResponseEntity.ok(ticketService.transitionStatus(id, request));
    }
}
