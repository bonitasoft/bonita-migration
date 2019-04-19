CREATE TABLE page (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  description TEXT,
  installationDate BIGINT NOT NULL,
  installedBy BIGINT NOT NULL,
  provided BOOLEAN,
  hidden BOOLEAN default false,
  lastModificationDate BIGINT NOT NULL,
  lastUpdatedBy BIGINT NOT NULL,
  contentName VARCHAR(280) NOT NULL,
  content LONGBLOB,
  contentType VARCHAR(50) NOT NULL,
  processDefinitionId BIGINT NOT NULL
) ENGINE = INNODB;
ALTER TABLE page ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id);
ALTER TABLE page ADD CONSTRAINT uk_page UNIQUE (tenantId, name, processDefinitionId);