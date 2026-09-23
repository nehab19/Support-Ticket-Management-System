package com.example.supportticket.service;

import com.example.supportticket.dto.CommentResponse;
import com.example.supportticket.dto.CreateCommentRequest;
import com.example.supportticket.exception.TicketNotFoundException;
import com.example.supportticket.model.Comment;
import com.example.supportticket.model.Ticket;
import com.example.supportticket.repository.CommentRepository;
import com.example.supportticket.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;

    public CommentService(TicketRepository ticketRepository, CommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
    }

    public CommentResponse addComment(Long ticketId, CreateCommentRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setAuthor(request.getAuthor());
        comment.setBody(request.getBody());
        Comment saved = commentRepository.save(comment);
        return new CommentResponse(saved.getId(), saved.getAuthor(), saved.getBody(), saved.getCreatedAt());
    }
}
