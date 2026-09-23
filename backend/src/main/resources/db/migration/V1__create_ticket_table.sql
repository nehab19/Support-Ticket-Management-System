CREATE TABLE ticket (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    priority    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    assignee    VARCHAR(255),
    created_at  TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT now()
);

CREATE INDEX idx_ticket_status     ON ticket (status);
CREATE INDEX idx_ticket_created_at ON ticket (created_at DESC);
