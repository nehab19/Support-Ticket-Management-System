package com.example.supportticket.service;

import com.example.supportticket.exception.InvalidStatusTransitionException;
import com.example.supportticket.model.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class TicketStateMachineTest {

    private TicketStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new TicketStateMachine();
    }

    // --- Allowed transitions ---

    @ParameterizedTest(name = "{0} -> {1} should be allowed")
    @CsvSource({
        "OPEN, IN_PROGRESS",
        "OPEN, CANCELLED",
        "IN_PROGRESS, RESOLVED",
        "IN_PROGRESS, CANCELLED",
        "RESOLVED, CLOSED"
    })
    void allowedTransitions_doNotThrow(TicketStatus from, TicketStatus to) {
        assertThatCode(() -> stateMachine.validate(from, to)).doesNotThrowAnyException();
    }

    // --- Disallowed transitions ---

    @ParameterizedTest(name = "{0} -> {1} should be rejected")
    @CsvSource({
        "CLOSED, OPEN",
        "RESOLVED, OPEN",
        "CANCELLED, OPEN",
        "CLOSED, IN_PROGRESS",
        "RESOLVED, IN_PROGRESS",
        "CANCELLED, IN_PROGRESS",
        "CLOSED, RESOLVED",
        "CANCELLED, RESOLVED",
        "OPEN, RESOLVED",
        "OPEN, CLOSED"
    })
    void disallowedTransitions_throwInvalidStatusTransitionException(TicketStatus from, TicketStatus to) {
        assertThatThrownBy(() -> stateMachine.validate(from, to))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining(from.name())
                .hasMessageContaining(to.name());
    }

    @Test
    void exceptionMessage_containsBothStatusNames() {
        assertThatThrownBy(() -> stateMachine.validate(TicketStatus.CLOSED, TicketStatus.OPEN))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("CLOSED")
                .hasMessageContaining("OPEN");
    }
}
