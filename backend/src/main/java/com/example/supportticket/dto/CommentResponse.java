package com.example.supportticket.dto;

import java.time.Instant;

public class CommentResponse {
    private Long id;
    private String author;
    private String body;
    private Instant createdAt;

    public CommentResponse() {}

    public CommentResponse(Long id, String author, String body, Instant createdAt) {
        this.id = id;
        this.author = author;
        this.body = body;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getAuthor() { return author; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
}
