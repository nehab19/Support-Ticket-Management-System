package com.example.supportticket.dto;

import com.example.supportticket.model.Priority;
import com.example.supportticket.model.TicketStatus;
import java.time.Instant;

public class TicketSummaryResponse {
    private Long id;
    private String title;
    private Priority priority;
    private TicketStatus status;
    private String assignee;
    private Instant createdAt;

    public TicketSummaryResponse() {}

    public TicketSummaryResponse(Long id, String title, Priority priority, TicketStatus status, String assignee, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.assignee = assignee;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Priority getPriority() { return priority; }
    public TicketStatus getStatus() { return status; }
    public String getAssignee() { return assignee; }
    public Instant getCreatedAt() { return createdAt; }
}
