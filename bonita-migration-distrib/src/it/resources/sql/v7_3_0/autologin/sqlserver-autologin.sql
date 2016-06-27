CREATE TABLE process_definition (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(150) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  description NVARCHAR(255),
  deploymentDate NUMERIC(19, 0) NOT NULL,
  deployedBy NUMERIC(19, 0) NOT NULL,
  activationState NVARCHAR(30) NOT NULL,
  configurationState NVARCHAR(30) NOT NULL,
  displayName NVARCHAR(75),
  displayDescription NVARCHAR(255),
  lastUpdateDate NUMERIC(19, 0),
  categoryId NUMERIC(19, 0),
  iconPath NVARCHAR(255),
  content_tenantid NUMERIC(19, 0) NOT NULL,
  content_id NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name, version)
)
GO
CREATE TABLE bar_resource (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  process_id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  type NVARCHAR(16) NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  UNIQUE (tenantId, process_id, name, type),
  PRIMARY KEY (tenantId, id)
)
GO
CREATE TABLE configuration (
  tenant_id NUMERIC(19, 0) NOT NULL,
  content_type  NVARCHAR(50) NOT NULL,
  resource_name  NVARCHAR(120) NOT NULL,
  resource_content  VARBINARY(MAX) NOT NULL
)
GO
ALTER TABLE configuration ADD CONSTRAINT pk_configuration PRIMARY KEY (tenant_id, content_type, resource_name)
GO
CREATE INDEX idx_configuration ON configuration (tenant_id, content_type)
GO
CREATE TABLE tenant (
  id NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (id)
)
GO