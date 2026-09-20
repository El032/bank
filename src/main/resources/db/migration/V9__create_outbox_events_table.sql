CREATE TABLE outbox_events (
                               id BIGSERIAL PRIMARY KEY,
                               event_type VARCHAR(255) NOT NULL,
                               aggregate_type VARCHAR(255) NOT NULL,
                               aggregate_id BIGINT NOT NULL,
                               payload TEXT NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               published BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_outbox_events_unpublished
    ON outbox_events (published, created_at);