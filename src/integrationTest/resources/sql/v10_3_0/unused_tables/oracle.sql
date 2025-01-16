CREATE TABLE sequence (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  nextid NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
INSERT INTO sequence VALUES(1, 31, 1);
INSERT INTO sequence VALUES(1, 10070, 1);

CREATE TABLE queriable_log (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  log_timestamp NUMBER(19, 0) NOT NULL,
  whatYear SMALLINT NOT NULL,
  whatMonth SMALLINT NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear SMALLINT NOT NULL,
  userId VARCHAR2(255 CHAR) NOT NULL,
  threadNumber NUMBER(19, 0) NOT NULL,
  clusterNode VARCHAR2(50 CHAR),
  productVersion VARCHAR2(50 CHAR) NOT NULL,
  severity VARCHAR2(50 CHAR) NOT NULL,
  actionType VARCHAR2(50 CHAR) NOT NULL,
  actionScope VARCHAR2(100 CHAR),
  actionStatus SMALLINT NOT NULL,
  rawMessage VARCHAR2(255 CHAR) NOT NULL,
  callerClassName VARCHAR2(200 CHAR),
  callerMethodName VARCHAR2(80 CHAR),
  numericIndex1 NUMBER(19, 0),
  numericIndex2 NUMBER(19, 0),
  numericIndex3 NUMBER(19, 0),
  numericIndex4 NUMBER(19, 0),
  numericIndex5 NUMBER(19, 0),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE queriablelog_p (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  queriableLogId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  stringValue VARCHAR2(255 CHAR),
  blobId NUMBER(19, 0),
  valueType VARCHAR2(30 CHAR),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_queriablelog ON queriablelog_p (queriableLogId);
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriable_log(tenantid, id);

CREATE TABLE blob_ (
    tenantId NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	blobValue BLOB,
	PRIMARY KEY (tenantid, id)
);

CREATE TABLE external_identity_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25 CHAR) NOT NULL,
  externalId VARCHAR2(50 CHAR) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  groupId NUMBER(19, 0) NOT NULL,
  roleId NUMBER(19, 0) NOT NULL,
  CONSTRAINT UK_External_Identity_Mapping UNIQUE (tenantid, kind, externalId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
