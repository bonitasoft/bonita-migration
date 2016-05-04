CREATE TABLE tenant_resource (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(16) NOT NULL,
  content BYTEA NOT NULL,
  CONSTRAINT UK_tenant_resource UNIQUE (tenantId, name, type),
  PRIMARY KEY (tenantId, id)
)
@@
CREATE INDEX idx_tenant_resource ON tenant_resource (tenantId, type, name)