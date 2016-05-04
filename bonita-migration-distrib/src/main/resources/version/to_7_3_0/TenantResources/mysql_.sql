CREATE TABLE tenant_resource (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(16) NOT NULL,
  content LONGBLOB NOT NULL,
  CONSTRAINT UK_tenant_resource UNIQUE (tenantId, name, type),
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB
@@
CREATE INDEX idx_tenant_resource ON tenant_resource (tenantId, type, name)