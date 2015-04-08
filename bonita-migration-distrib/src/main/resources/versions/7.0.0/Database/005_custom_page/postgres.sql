CREATE TABLE page2 (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(512) NOT NULL,
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

ALTER TABLE page2 ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id);
@@

ALTER TABLE page2 ADD CONSTRAINT uk_page UNIQUE (tenantId, name, processDefinitionId);
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

ALTER TABLE page2 RENAME TO page
@@
