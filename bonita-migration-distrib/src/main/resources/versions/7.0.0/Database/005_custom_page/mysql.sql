CREATE TABLE page2 (
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

ALTER TABLE page2 ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id)
@@

ALTER TABLE page2 ADD CONSTRAINT uk_page UNIQUE (tenantId, name, processDefinitionId)
@@

ALTER TABLE business_app_page DROP CONSTRAINT fk_page_id
@@

INSERT INTO page2(tenantid, id, name, displayName, description, installationDate, installedBy,
    provided,lastModificationDate,lastUpdatedBy,contentName,content,contentType,processDefinitionId)
SELECT tenantid, id, name, displayName, description, installationDate, installedBy,
           provided,lastModificationDate,lastUpdatedBy,contentName,content,'page',null
FROM page
@@

ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page2 (tenantid, id)
@@

DROP TABLE page
@@

RENAME TABLE  page2 TO page
@@
