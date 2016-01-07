CREATE TABLE page (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  description TEXT,
  installationDate BIGINT NOT NULL,
  installedBy BIGINT NOT NULL,
  provided BOOLEAN,
  lastModificationDate BIGINT NOT NULL,
  lastUpdatedBy BIGINT NOT NULL,
  contentName VARCHAR(50) NOT NULL,
  content LONGBLOB,
  contentType VARCHAR(50) NOT NULL,
  processDefinitionId BIGINT
) ENGINE = INNODB
@@
CREATE TABLE profileentry (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  profileId BIGINT NOT NULL,
  name VARCHAR(50),
  description TEXT,
  parentId BIGINT,
  index_ BIGINT,
  type VARCHAR(50),
  page VARCHAR(50),
  custom BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB