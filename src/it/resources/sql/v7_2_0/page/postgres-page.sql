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
  contentType VARCHAR(50) NOT NULL,
  processDefinitionId INT8
)
@@
CREATE TABLE profileentry (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  profileId INT8 NOT NULL,
  name VARCHAR(50),
  description TEXT,
  parentId INT8,
  index_ INT8,
  type VARCHAR(50),
  page VARCHAR(50),
  custom BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (tenantId, id)
)