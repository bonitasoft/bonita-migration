CREATE TABLE tenant (
  id INT8 NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE sequence (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  nextid INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE page (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  description TEXT,
  installationDate INT8 NOT NULL,
  installedBy INT8 NOT NULL,
  provided BOOLEAN,
  lastModificationDate INT8 NOT NULL,
  lastUpdatedBy INT8 NOT NULL,
  contentName VARCHAR(50) NOT NULL,
  content BYTEA,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);

