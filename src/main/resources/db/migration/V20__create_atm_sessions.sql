CREATE TABLE atm_sessions (
                              id BIGSERIAL PRIMARY KEY,
                              card_id BIGINT NOT NULL UNIQUE,
                              created_at TIMESTAMP NOT NULL,
                              active BOOLEAN NOT NULL DEFAULT TRUE,

                              CONSTRAINT fk_atm_sessions_card
                                  FOREIGN KEY (card_id)
                                      REFERENCES bank_cards(id)
);