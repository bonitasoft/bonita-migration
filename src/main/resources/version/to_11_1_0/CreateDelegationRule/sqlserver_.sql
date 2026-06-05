CREATE TABLE delegation_rule (
    id                  NUMERIC(19, 0) NOT NULL,
    delegator_id        NUMERIC(19, 0) NOT NULL,
    delegate_id         NUMERIC(19, 0) NOT NULL,
    start_date          NUMERIC(19, 0) NOT NULL,
    end_date            NUMERIC(19, 0) NOT NULL,
    last_updated_by     NUMERIC(19, 0) NOT NULL,
    last_updated_at     NUMERIC(19, 0) NOT NULL,
    CONSTRAINT pk_delegation_rule PRIMARY KEY (id),
    CONSTRAINT uk_delegation_rule_delegator_id UNIQUE (delegator_id)
)
@@
CREATE INDEX idx_delegation_rule_delegate_id ON delegation_rule (delegate_id)
