CREATE TABLE arch_document_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  sourceObjectId INT8,
  processinstanceid INT8 NOT NULL,
  documentid INT8 DEFAULT 0 NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  version VARCHAR(10) DEFAULT '1' NOT NULL,
  index_ INT NOT NULL,
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, ID)
);

CREATE TABLE document_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processinstanceid INT8 NOT NULL,
  documentid INT8 DEFAULT 0 NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  version VARCHAR(10) DEFAULT '1' NOT NULL,
  index_ INT NOT NULL,
  PRIMARY KEY (tenantid, ID)
);

CREATE TABLE arch_connector_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  containerId INT8 NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  connectorId VARCHAR(255) NOT NULL,
  version VARCHAR(10) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  sourceObjectId INT8,
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE connector_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  containerId INT8 NOT NULL,
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
);