CREATE TABLE processed_notifications (
                                         id BIGSERIAL PRIMARY KEY,
                                         transfer_id BIGINT NOT NULL,
                                         processed_at TIMESTAMP NOT NULL,
                                         CONSTRAINT uk_processed_notifications_transfer_id UNIQUE (transfer_id)
);