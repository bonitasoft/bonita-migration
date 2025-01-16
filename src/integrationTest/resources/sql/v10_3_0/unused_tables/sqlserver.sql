CREATE TABLE sequence (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  nextid NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
INSERT INTO sequence VALUES(1, 31, 1);
INSERT INTO sequence VALUES(1, 10070, 1);

CREATE TABLE queriable_log (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  log_timestamp NUMERIC(19, 0) NOT NULL,
  whatYear SMALLINT NOT NULL,
  whatMonth TINYINT NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear TINYINT NOT NULL,
  userId NVARCHAR(255) NOT NULL,
  threadNumber NUMERIC(19, 0) NOT NULL,
  clusterNode NVARCHAR(50),
  productVersion NVARCHAR(50) NOT NULL,
  severity NVARCHAR(50) NOT NULL,
  actionType NVARCHAR(50) NOT NULL,
  actionScope NVARCHAR(100),
  actionStatus TINYINT NOT NULL,
  rawMessage NVARCHAR(255) NOT NULL,
  callerClassName NVARCHAR(200),
  callerMethodName NVARCHAR(80),
  numericIndex1 NUMERIC(19, 0),
  numericIndex2 NUMERIC(19, 0),
  numericIndex3 NUMERIC(19, 0),
  numericIndex4 NUMERIC(19, 0),
  numericIndex5 NUMERIC(19, 0),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE queriablelog_p (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  queriableLogId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  stringValue NVARCHAR(255),
  blobId NUMERIC(19, 0),
  valueType NVARCHAR(30),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_queriablelog ON queriablelog_p (queriableLogId, id);
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriable_log(tenantid, id);

CREATE TABLE blob_ (
    tenantId NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	blobValue VARBINARY(MAX),
	PRIMARY KEY (tenantid, id)
);

CREATE TABLE external_identity_mapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(25) NOT NULL,
  externalId NVARCHAR(50) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  groupId NUMERIC(19, 0) NOT NULL,
  roleId NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantid, kind, externalId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
