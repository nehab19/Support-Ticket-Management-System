package com.example.supportticket.repository;

import com.example.supportticket.model.Priority;
import com.example.supportticket.model.Ticket;
import com.example.supportticket.model.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TicketRepositoryTest {

    @Autowired
    TicketRepository ticketRepository;

    private Ticket createTicket(String title, String description, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setStatus(status);
        ticket.setPriority(Priority.MEDIUM);
        return ticketRepository.save(ticket);
    }

    @Test
    void findAllByOrderByCreatedAtDesc_returnsMostRecentFirst() throws InterruptedException {
        Ticket first = createTicket("First ticket", "desc1", TicketStatus.OPEN);
        Thread.sleep(10); // ensure distinct timestamps
        Ticket second = createTicket("Second ticket", "desc2", TicketStatus.OPEN);

        List<Ticket> results = ticketRepository.findAllByOrderByCreatedAtDesc();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(second.getId());
        assertThat(results.get(1).getId()).isEqualTo(first.getId());
    }

    @Test
    void findByStatusOrderByCreatedAtDesc_returnsOnlyMatchingStatus() {
        createTicket("Open ticket", "desc", TicketStatus.OPEN);
        createTicket("Closed ticket", "desc", TicketStatus.CLOSED);

        List<Ticket> openTickets = ticketRepository.findByStatusOrderByCreatedAtDesc(TicketStatus.OPEN);

        assertThat(openTickets).hasSize(1);
        assertThat(openTickets.get(0).getStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void findByStatusOrderByCreatedAtDesc_returnsEmptyList_whenNoMatch() {
        createTicket("Open ticket", "desc", TicketStatus.OPEN);

        List<Ticket> resolved = ticketRepository.findByStatusOrderByCreatedAtDesc(TicketStatus.RESOLVED);

        assertThat(resolved).isEmpty();
    }

    @Test
    void keywordSearch_matchesTitleCaseInsensitive() {
        createTicket("Login page error", "Something went wrong", TicketStatus.OPEN);
        createTicket("Unrelated ticket", "Nothing here", TicketStatus.OPEN);

        List<Ticket> results = ticketRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc("LOGIN", "LOGIN");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).containsIgnoringCase("login");
    }

    @Test
    void keywordSearch_matchesDescriptionCaseInsensitive() {
        createTicket("Some ticket", "Critical database issue", TicketStatus.OPEN);
        createTicket("Other ticket", "Nothing relevant", TicketStatus.OPEN);

        List<Ticket> results = ticketRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc("DATABASE", "DATABASE");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDescription()).containsIgnoringCase("database");
    }

    @Test
    void keywordSearch_returnsEmpty_whenNoMatch() {
        createTicket("Ticket one", "desc one", TicketStatus.OPEN);

        List<Ticket> results = ticketRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc("ZZZNOMATCH", "ZZZNOMATCH");

        assertThat(results).isEmpty();
    }

    @Test
    void save_assignsId_andTimestamps() {
        Ticket ticket = createTicket("New ticket", "description", TicketStatus.OPEN);

        assertThat(ticket.getId()).isNotNull();
        assertThat(ticket.getCreatedAt()).isNotNull();
        assertThat(ticket.getUpdatedAt()).isNotNull();
    }
}
