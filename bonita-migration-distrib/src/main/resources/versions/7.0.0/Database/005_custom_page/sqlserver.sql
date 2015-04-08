CREATE TABLE page2 (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(512) NOT NULL,
  displayName NVARCHAR(255) NOT NULL,
  description NVARCHAR(MAX),
  installationDate NUMERIC(19, 0) NOT NULL,
  installedBy NUMERIC(19, 0) NOT NULL,
  provided BIT,
  lastModificationDate NUMERIC(19, 0) NOT NULL,
  lastUpdatedBy NUMERIC(19, 0) NOT NULL,
  contentName NVARCHAR(50) NOT NULL,
  content VARBINARY(MAX),
  contentType NVARCHAR(50) NOT NULL,
  processDefinitionId NUMERIC(19,0)
)
@@

ALTER TABLE page2 ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id)
@@

ALTER TABLE page2 ADD CONSTRAINT  uk_page UNIQUE (tenantId, name, processDefinitionId)
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

sp_rename page2 , page
@@
