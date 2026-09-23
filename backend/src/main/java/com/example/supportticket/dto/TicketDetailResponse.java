package com.example.supportticket.dto;

import com.example.supportticket.model.Priority;
import com.example.supportticket.model.TicketStatus;
import java.time.Instant;
import java.util.List;

public class TicketDetailResponse {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private TicketStatus status;
    private String assignee;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CommentResponse> comments;

    public TicketDetailResponse() {}

    public TicketDetailResponse(Long id, String title, String description, Priority priority,
                                 TicketStatus status, String assignee, Instant createdAt,
                                 Instant updatedAt, List<CommentResponse> comments) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.assignee = assignee;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.comments = comments;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public TicketStatus getStatus() { return status; }
    public String getAssignee() { return assignee; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<CommentResponse> getComments() { return comments; }
}
