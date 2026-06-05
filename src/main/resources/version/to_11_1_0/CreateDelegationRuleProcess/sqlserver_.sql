CREATE TABLE delegation_rule_process (
    id                  NUMERIC(19, 0) NOT NULL,
    delegation_rule_id  NUMERIC(19, 0) NOT NULL,
    process_name        NVARCHAR(255) NOT NULL,
    CONSTRAINT pk_delegation_rule_process PRIMARY KEY (id),
    CONSTRAINT uk_delegation_rule_process_delegation_rule_id_process_name UNIQUE (delegation_rule_id, process_name)
)
@@
ALTER TABLE delegation_rule_process ADD CONSTRAINT fk_delegation_rule_process_delegation_rule_id
    FOREIGN KEY (delegation_rule_id) REFERENCES delegation_rule(id) ON DELETE CASCADE
