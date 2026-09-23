package com.example.supportticket.dto;

import com.example.supportticket.model.TicketStatus;
import jakarta.validation.constraints.NotNull;

public class StatusTransitionRequest {

    @NotNull(message = "status must not be null")
    private TicketStatus status;

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
}
