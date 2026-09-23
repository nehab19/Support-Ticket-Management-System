package com.example.supportticket.exception;

import com.example.supportticket.controller.TicketController;
import com.example.supportticket.dto.CreateTicketRequest;
import com.example.supportticket.model.TicketStatus;
import com.example.supportticket.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @Test
    void missingRequiredFields_returns400WithFieldErrors() throws Exception {
        // Empty body — title and description both missing
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(2)); // title + description
    }

    @Test
    void ticketNotFound_returns404() throws Exception {
        when(ticketService.getTicket(anyLong()))
                .thenThrow(new TicketNotFoundException(99L));

        mockMvc.perform(get("/api/tickets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Ticket 99 not found"));
    }

    @Test
    void invalidStatusTransition_returns422() throws Exception {
        when(ticketService.transitionStatus(anyLong(), any()))
                .thenThrow(new InvalidStatusTransitionException(TicketStatus.CLOSED, TicketStatus.OPEN));

        mockMvc.perform(patch("/api/tickets/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"OPEN\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Transition from CLOSED to OPEN is not allowed"));
    }

    @Test
    void invalidEnumValue_returns400() throws Exception {
        mockMvc.perform(get("/api/tickets")
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unexpectedError_returns500WithGenericMessage() throws Exception {
        when(ticketService.getTicket(anyLong()))
                .thenThrow(new RuntimeException("Internal details"));

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                // stack trace must NOT be leaked
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
