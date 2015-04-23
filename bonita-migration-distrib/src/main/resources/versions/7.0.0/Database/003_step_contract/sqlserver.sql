CREATE TABLE contract_data (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(20) NOT NULL,
  scopeId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  val VARBINARY(MAX)
)
@@
ALTER TABLE contract_data ADD CONSTRAINT pk_contract_data PRIMARY KEY (tenantid, id, scopeId)
@@
ALTER TABLE contract_data ADD CONSTRAINT uc_cd_scope_name UNIQUE (kind, scopeId, name, tenantid)
@@
CREATE INDEX idx_cd_scope_name ON contract_data (kind, scopeId, name, tenantid)
@@

CREATE TABLE arch_contract_data (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(20) NOT NULL,
  scopeId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  val VARBINARY(MAX),
  archiveDate NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0) NOT NULL
)
@@
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId)
@@
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid)
@@
CREATE INDEX idx_acd_scope_name ON arch_contract_data (kind, scopeId, name, tenantid)
@@
