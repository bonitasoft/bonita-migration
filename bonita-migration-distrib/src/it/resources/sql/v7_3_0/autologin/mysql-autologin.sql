CREATE TABLE process_definition (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processId BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  version VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  deploymentDate BIGINT NOT NULL,
  deployedBy BIGINT NOT NULL,
  activationState VARCHAR(30) NOT NULL,
  configurationState VARCHAR(30) NOT NULL,
  displayName VARCHAR(75),
  displayDescription VARCHAR(255),
  lastUpdateDate BIGINT,
  categoryId BIGINT,
  iconPath VARCHAR(255),
  content_tenantid BIGINT NOT NULL,
  content_id BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name, version)
) ENGINE = INNODB;

CREATE TABLE bar_resource (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  process_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(16) NOT NULL,
  content LONGBLOB NOT NULL,
  UNIQUE (tenantId, process_id, name, type),
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;
CREATE INDEX idx_bar_resource ON bar_resource (tenantId, process_id, type, name);

CREATE TABLE configuration (
  tenant_id BIGINT NOT NULL,
  content_type VARCHAR(50) NOT NULL,
  resource_name VARCHAR(120) NOT NULL,
  resource_content BLOB
) ENGINE = INNODB;
ALTER TABLE configuration ADD CONSTRAINT pk_configuration PRIMARY KEY (tenant_id, content_type, resource_name);
CREATE INDEX idx_configuration ON configuration (tenant_id, content_type);
CREATE TABLE tenant (
  id BIGINT NOT NULL,
  PRIMARY KEY (id)
) ENGINE = INNODB;
