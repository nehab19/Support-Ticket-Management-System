package com.example.supportticket.dto;

import java.util.List;

public record ErrorResponse(int status, String message, List<FieldError> errors) {}
