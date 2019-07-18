CREATE TABLE arch_contract_data_backup (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  kind VARCHAR(20) NOT NULL,
  scopeId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  val BYTEA,
  archiveDate INT8 NOT NULL,
  sourceObjectId INT8 NOT NULL
);

CREATE TABLE arch_contract_data (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  kind VARCHAR(20) NOT NULL,
  scopeId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  val TEXT,
  archiveDate INT8 NOT NULL,
  sourceObjectId INT8 NOT NULL
);
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid);
CREATE INDEX idx_acd_scope_name ON arch_contract_data (kind, scopeId, name, tenantid);