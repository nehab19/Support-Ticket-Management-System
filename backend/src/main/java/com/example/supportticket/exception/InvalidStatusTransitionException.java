package com.example.supportticket.exception;

import com.example.supportticket.model.TicketStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(TicketStatus from, TicketStatus to) {
        super("Transition from " + from + " to " + to + " is not allowed");
    }
}
