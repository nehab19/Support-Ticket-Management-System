package com.example.supportticket.service;

import com.example.supportticket.exception.InvalidStatusTransitionException;
import com.example.supportticket.model.TicketStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class TicketStateMachine {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS;

    static {
        ALLOWED_TRANSITIONS = new EnumMap<>(TicketStatus.class);
        ALLOWED_TRANSITIONS.put(TicketStatus.OPEN,        EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TicketStatus.IN_PROGRESS, EnumSet.of(TicketStatus.RESOLVED,    TicketStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(TicketStatus.RESOLVED,    EnumSet.of(TicketStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(TicketStatus.CLOSED,      EnumSet.noneOf(TicketStatus.class));
        ALLOWED_TRANSITIONS.put(TicketStatus.CANCELLED,   EnumSet.noneOf(TicketStatus.class));
    }

    /**
     * Validates that the transition from {@code from} to {@code to} is permitted.
     *
     * @throws InvalidStatusTransitionException if the transition is not allowed
     */
    public void validate(TicketStatus from, TicketStatus to) {
        Set<TicketStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(TicketStatus.class));
        if (!allowed.contains(to)) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }
}
