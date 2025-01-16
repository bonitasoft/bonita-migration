CREATE TABLE sequence (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  nextid INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);
INSERT INTO sequence VALUES(1, 31, 1);
INSERT INTO sequence VALUES(1, 10070, 1);

CREATE TABLE queriable_log (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  log_timestamp INT8 NOT NULL,
  whatYear SMALLINT NOT NULL,
  whatMonth SMALLINT NOT NULL,
  dayOfYear SMALLINT NOT NULL,
  weekOfYear SMALLINT NOT NULL,
  userId VARCHAR(255) NOT NULL,
  threadNumber INT8 NOT NULL,
  clusterNode VARCHAR(50),
  productVersion VARCHAR(50) NOT NULL,
  severity VARCHAR(50) NOT NULL,
  actionType VARCHAR(50) NOT NULL,
  actionScope VARCHAR(100),
  actionStatus SMALLINT NOT NULL,
  rawMessage VARCHAR(255) NOT NULL,
  callerClassName VARCHAR(200),
  callerMethodName VARCHAR(80),
  numericIndex1 INT8,
  numericIndex2 INT8,
  numericIndex3 INT8,
  numericIndex4 INT8,
  numericIndex5 INT8,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE queriablelog_p (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  queriableLogId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  stringValue VARCHAR(255),
  blobId INT8,
  valueType VARCHAR(30),
  PRIMARY KEY (tenantid, id)
);

CREATE INDEX idx_queriablelog ON queriablelog_p (queriableLogId);
ALTER TABLE queriablelog_p ADD CONSTRAINT fk_queriableLogId FOREIGN KEY (tenantid, queriableLogId) REFERENCES queriable_log(tenantid, id);

CREATE TABLE blob_ (
    tenantId INT8 NOT NULL,
	id INT8 NOT NULL,
	blobValue BYTEA,
	PRIMARY KEY (tenantid, id)
);

CREATE TABLE external_identity_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  externalId VARCHAR(50) NOT NULL,
  userId INT8 NOT NULL,
  groupId INT8 NOT NULL,
  roleId INT8 NOT NULL,
  UNIQUE (tenantid, kind, externalId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
