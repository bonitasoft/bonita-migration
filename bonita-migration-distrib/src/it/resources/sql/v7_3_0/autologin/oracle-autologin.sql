CREATE TABLE process_definition (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(150 CHAR) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(255 CHAR),
  deploymentDate NUMBER(19, 0) NOT NULL,
  deployedBy NUMBER(19, 0) NOT NULL,
  activationState VARCHAR2(30 CHAR) NOT NULL,
  configurationState VARCHAR2(30 CHAR) NOT NULL,
  displayName VARCHAR2(75 CHAR),
  displayDescription VARCHAR2(255 CHAR),
  lastUpdateDate NUMBER(19, 0),
  categoryId NUMBER(19, 0),
  iconPath VARCHAR2(255 CHAR),
  content_tenantid NUMBER(19, 0) NOT NULL,
  content_id NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantId, id),
  CONSTRAINT UK_Process_Definition UNIQUE (tenantId, name, version)
);
CREATE TABLE bar_resource (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  process_id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255) NOT NULL,
  type VARCHAR2(16) NOT NULL,
  content BLOB NOT NULL,
  UNIQUE (tenantId, process_id, name, type),
  PRIMARY KEY (tenantId, id)
);
CREATE INDEX idx_bar_resource ON bar_resource (tenantId, process_id, type, name);

CREATE TABLE configuration (
  tenant_id NUMBER(19, 0) NOT NULL,
  content_type VARCHAR2(50 CHAR) NOT NULL,
  resource_name VARCHAR2(120 CHAR) NOT NULL,
  resource_content BLOB NOT NULL
);
ALTER TABLE configuration ADD CONSTRAINT pk_configuration PRIMARY KEY (tenant_id, content_type, resource_name);
CREATE INDEX idx_configuration ON configuration (tenant_id, content_type);

CREATE TABLE tenant (
  id NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (id)
);