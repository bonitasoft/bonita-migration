CREATE TABLE tenant_resource (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  type NVARCHAR(16) NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  CONSTRAINT UK_tenant_resource UNIQUE (tenantId, name, type),
  PRIMARY KEY (tenantId, id)
)
@@
CREATE INDEX idx_tenant_resource ON tenant_resource (tenantId, type, name)