-- drop unnecessary constraints:
alter table arch_contract_data drop PRIMARY KEY;
alter table arch_contract_data drop INDEX uc_acd_scope_name;
alter table arch_contract_data drop index idx_acd_scope_name;
-- rename table:
alter table arch_contract_data rename to arch_contract_data_backup;
-- recreate empty table...
CREATE TABLE arch_contract_data (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  kind VARCHAR(20) NOT NULL,
  scopeId BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  val LONGTEXT,
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL
) ENGINE = INNODB;
-- with standard constraints:
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid);
CREATE INDEX idx_acd_scope_name ON arch_contract_data (kind, scopeId, name, tenantid);
