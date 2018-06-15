CREATE TABLE arch_document_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0),
  processinstanceid NUMBER(19, 0) NOT NULL,
  documentid NUMBER(19, 0) DEFAULT 0 NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  version VARCHAR2(10 CHAR) DEFAULT '1' NOT NULL,
  index_ INT NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE document_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processinstanceid NUMBER(19, 0) NOT NULL,
  documentid NUMBER(19, 0) DEFAULT 0 NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  version VARCHAR2(10 CHAR) DEFAULT '1' NOT NULL,
  index_ INT NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE arch_connector_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0) NOT NULL,
  containerType VARCHAR2(10 CHAR) NOT NULL,
  connectorId VARCHAR2(255 CHAR) NOT NULL,
  version VARCHAR2(10 CHAR) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  activationEvent VARCHAR2(30 CHAR),
  state VARCHAR2(50 CHAR),
  sourceObjectId NUMBER(19, 0),
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE connector_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0) NOT NULL,
  containerType VARCHAR2(10 CHAR) NOT NULL,
  connectorId VARCHAR2(255 CHAR) NOT NULL,
  version VARCHAR2(10 CHAR) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  activationEvent VARCHAR2(30 CHAR),
  state VARCHAR2(50 CHAR),
  executionOrder INT,
  exceptionMessage VARCHAR2(255 CHAR),
  stackTrace CLOB,
  PRIMARY KEY (tenantid, id)
);