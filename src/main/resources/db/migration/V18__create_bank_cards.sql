CREATE TABLE bank_cards (
                            id BIGSERIAL PRIMARY KEY,
                            card_number VARCHAR(19) NOT NULL UNIQUE,
                            pin_hash VARCHAR(255) NOT NULL,
                            expiry_date DATE NOT NULL,
                            cvv_hash VARCHAR(255) NOT NULL,
                            account_id BIGINT NOT NULL UNIQUE,
                            CONSTRAINT fk_bank_cards_account
                                FOREIGN KEY (account_id)
                                    REFERENCES accounts(id)
);