CREATE TABLE delegation_rule (
    id                  BIGINT NOT NULL,
    delegator_id        BIGINT NOT NULL,
    delegate_id         BIGINT NOT NULL,
    start_date          BIGINT NOT NULL,
    end_date            BIGINT NOT NULL,
    last_updated_by     BIGINT NOT NULL,
    last_updated_at     BIGINT NOT NULL,
    CONSTRAINT pk_delegation_rule PRIMARY KEY (id),
    CONSTRAINT uk_delegation_rule_delegator_id UNIQUE (delegator_id)
) ENGINE = INNODB
@@
CREATE INDEX idx_delegation_rule_delegate_id ON delegation_rule (delegate_id)
