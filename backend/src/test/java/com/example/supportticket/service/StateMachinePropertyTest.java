package com.example.supportticket.service;

// Feature: support-ticket-management, Property 8: State machine — allowed transitions succeed; disallowed transitions are rejected with HTTP 422

import com.example.supportticket.exception.InvalidStatusTransitionException;
import com.example.supportticket.model.TicketStatus;
import net.jqwik.api.*;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class StateMachinePropertyTest {

    private static final Set<String> ALLOWED_PAIRS = Set.of(
            "OPEN->IN_PROGRESS",
            "OPEN->CANCELLED",
            "IN_PROGRESS->RESOLVED",
            "IN_PROGRESS->CANCELLED",
            "RESOLVED->CLOSED"
    );

    private final TicketStateMachine stateMachine = new TicketStateMachine();

    @Provide
    Arbitrary<TicketStatus> anyStatus() {
        return Arbitraries.of(TicketStatus.values());
    }

    /**
     * **Validates: Requirements 5.1, 5.2, 5.3**
     *
     * For any (from, to) pair: allowed transitions must NOT throw;
     * disallowed transitions MUST throw InvalidStatusTransitionException
     * whose message contains both status names.
     */
    @Property(tries = 100)
    void stateMachineTransitions(
            @ForAll("anyStatus") TicketStatus from,
            @ForAll("anyStatus") TicketStatus to) {

        String pair = from.name() + "->" + to.name();

        if (ALLOWED_PAIRS.contains(pair)) {
            // Allowed — must not throw
            assertThatCode(() -> stateMachine.validate(from, to))
                    .doesNotThrowAnyException();
        } else {
            // Disallowed — must throw with both names in message
            assertThatThrownBy(() -> stateMachine.validate(from, to))
                    .isInstanceOf(InvalidStatusTransitionException.class)
                    .hasMessageContaining(from.name())
                    .hasMessageContaining(to.name());
        }
    }

    /**
     * Exhaustive coverage of all 25 (from, to) pairs so that every
     * allowed transition is guaranteed to be exercised at least once,
     * independent of jqwik's random sampling.
     *
     * **Validates: Requirements 5.1, 5.2, 5.3**
     */
    @Example
    void allPairsExhaustive() {
        for (TicketStatus from : TicketStatus.values()) {
            for (TicketStatus to : TicketStatus.values()) {
                String pair = from.name() + "->" + to.name();
                if (ALLOWED_PAIRS.contains(pair)) {
                    assertThatCode(() -> stateMachine.validate(from, to))
                            .as("Expected allowed transition %s -> %s to succeed", from, to)
                            .doesNotThrowAnyException();
                } else {
                    assertThatThrownBy(() -> stateMachine.validate(from, to))
                            .as("Expected disallowed transition %s -> %s to throw", from, to)
                            .isInstanceOf(InvalidStatusTransitionException.class)
                            .hasMessageContaining(from.name())
                            .hasMessageContaining(to.name());
                }
            }
        }
    }
}
