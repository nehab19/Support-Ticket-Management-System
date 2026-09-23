package com.example.supportticket.dto;

import com.example.supportticket.model.Priority;
import jakarta.validation.constraints.Size;

public class UpdateTicketRequest {

    @Size(max = 255, message = "title must not exceed 255 characters")
    private String title;

    private String description;

    private Priority priority;

    private String assignee;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
}
