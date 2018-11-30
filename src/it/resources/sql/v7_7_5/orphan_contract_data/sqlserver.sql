CREATE TABLE arch_contract_data (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(20) NOT NULL,
  scopeId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  val NVARCHAR(MAX),
  archiveDate NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0) NOT NULL
)
GO
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId)
GO
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid)
GO
CREATE INDEX idx_acd_scope_name ON arch_contract_data (kind, scopeId, name, tenantid)
GO
CREATE TABLE arch_process_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(75) NOT NULL,
  sourceObjectId NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
CREATE TABLE arch_flownode_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0),
  name NVARCHAR(255) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO