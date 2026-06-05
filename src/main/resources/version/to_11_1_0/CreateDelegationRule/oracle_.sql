CREATE TABLE delegation_rule (
    id                  NUMBER(19, 0) NOT NULL,
    delegator_id        NUMBER(19, 0) NOT NULL,
    delegate_id         NUMBER(19, 0) NOT NULL,
    start_date          NUMBER(19, 0) NOT NULL,
    end_date            NUMBER(19, 0) NOT NULL,
    last_updated_by     NUMBER(19, 0) NOT NULL,
    last_updated_at     NUMBER(19, 0) NOT NULL,
    CONSTRAINT pk_delegation_rule PRIMARY KEY (id),
    CONSTRAINT uk_delegation_rule_delegator_id UNIQUE (delegator_id)
)
@@
CREATE INDEX idx_delegation_rule_delegate_id ON delegation_rule (delegate_id)
