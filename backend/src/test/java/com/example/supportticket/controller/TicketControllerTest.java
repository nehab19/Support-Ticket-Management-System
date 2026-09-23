package com.example.supportticket.controller;

import com.example.supportticket.dto.*;
import com.example.supportticket.model.Priority;
import com.example.supportticket.model.TicketStatus;
import com.example.supportticket.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    private TicketDetailResponse sampleDetail() {
        return new TicketDetailResponse(1L, "Test ticket", "A description",
                Priority.MEDIUM, TicketStatus.OPEN, null,
                Instant.now(), Instant.now(), List.of());
    }

    private TicketSummaryResponse sampleSummary() {
        return new TicketSummaryResponse(1L, "Test ticket",
                Priority.MEDIUM, TicketStatus.OPEN, null, Instant.now());
    }

    // --- POST /api/tickets ---

    @Test
    void createTicket_validRequest_returns201() throws Exception {
        when(ticketService.createTicket(any())).thenReturn(sampleDetail());

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Test ticket\", \"description\": \"A description\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void createTicket_missingTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"A description\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createTicket_missingDescription_returns400() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Test ticket\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createTicket_titleTooLong_returns400() throws Exception {
        String longTitle = "x".repeat(256);
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"" + longTitle + "\", \"description\": \"desc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // --- GET /api/tickets ---

    @Test
    void listTickets_noParams_returns200() throws Exception {
        when(ticketService.listTickets(any(), any())).thenReturn(List.of(sampleSummary()));

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listTickets_withValidStatus_returns200() throws Exception {
        when(ticketService.listTickets(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/tickets").param("status", "OPEN"))
                .andExpect(status().isOk());
    }

    @Test
    void listTickets_withInvalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/api/tickets").param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    // --- GET /api/tickets/{id} ---

    @Test
    void getTicket_existingId_returns200() throws Exception {
        when(ticketService.getTicket(1L)).thenReturn(sampleDetail());

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test ticket"))
                .andExpect(jsonPath("$.comments").isArray());
    }

    // --- PATCH /api/tickets/{id} ---

    @Test
    void updateTicket_validRequest_returns200() throws Exception {
        when(ticketService.updateTicket(anyLong(), any())).thenReturn(sampleDetail());

        mockMvc.perform(patch("/api/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Updated\"}"))
                .andExpect(status().isOk());
    }

    // --- PATCH /api/tickets/{id}/status ---

    @Test
    void transitionStatus_validRequest_returns200() throws Exception {
        when(ticketService.transitionStatus(anyLong(), any())).thenReturn(sampleDetail());

        mockMvc.perform(patch("/api/tickets/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void transitionStatus_missingStatus_returns400() throws Exception {
        mockMvc.perform(patch("/api/tickets/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
