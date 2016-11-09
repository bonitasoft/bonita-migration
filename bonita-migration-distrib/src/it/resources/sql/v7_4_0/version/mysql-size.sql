CREATE TABLE arch_document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  sourceObjectId BIGINT,
  processinstanceid BIGINT NOT NULL,
  documentid BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  version VARCHAR(10) NOT NULL,
  index_ INT NOT NULL,
  archiveDate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, ID)
) ENGINE = INNODB;

CREATE TABLE document_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processinstanceid BIGINT NOT NULL,
  documentid BIGINT NOT NULL,
  name VARCHAR(10) NOT NULL,
  description TEXT,
  version VARCHAR(10) NOT NULL,
  index_ INT NOT NULL,
  PRIMARY KEY (tenantid, ID)
) ENGINE = INNODB;

CREATE TABLE arch_connector_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  containerId BIGINT NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  connectorId VARCHAR(255) NOT NULL,
  version VARCHAR(10) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  sourceObjectId BIGINT,
  archiveDate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE connector_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  containerId BIGINT NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  connectorId VARCHAR(255) NOT NULL,
  version VARCHAR(10) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  executionOrder INT,
  exceptionMessage VARCHAR(255),
  stackTrace TEXT,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;