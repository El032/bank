ALTER TABLE atm_sessions
DROP CONSTRAINT atm_sessions_card_id_key;

CREATE UNIQUE INDEX ux_atm_sessions_active_card
    ON atm_sessions (card_id)
    WHERE active = TRUE;