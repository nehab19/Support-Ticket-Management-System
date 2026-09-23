package com.example.supportticket.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateCommentRequest {

    @NotBlank(message = "author must not be blank")
    private String author;

    @NotBlank(message = "body must not be blank")
    private String body;

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
