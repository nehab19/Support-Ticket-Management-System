CREATE TABLE comment (
    id         BIGSERIAL    PRIMARY KEY,
    ticket_id  BIGINT       NOT NULL REFERENCES ticket(id) ON DELETE CASCADE,
    author     VARCHAR(255) NOT NULL,
    body       TEXT         NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT now()
);

CREATE INDEX idx_comment_ticket_id ON comment (ticket_id);
