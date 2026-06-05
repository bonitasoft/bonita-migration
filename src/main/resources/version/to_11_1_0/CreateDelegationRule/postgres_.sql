CREATE TABLE delegation_rule (
    id                  INT8 NOT NULL,
    delegator_id        INT8 NOT NULL,
    delegate_id         INT8 NOT NULL,
    start_date          INT8 NOT NULL,
    end_date            INT8 NOT NULL,
    last_updated_by     INT8 NOT NULL,
    last_updated_at     INT8 NOT NULL,
    CONSTRAINT pk_delegation_rule PRIMARY KEY (id),
    CONSTRAINT uk_delegation_rule_delegator_id UNIQUE (delegator_id)
)
@@
CREATE INDEX idx_delegation_rule_delegate_id ON delegation_rule (delegate_id)
