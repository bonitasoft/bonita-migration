CREATE TABLE page2 (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  displayName VARCHAR2(255 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  installationDate NUMBER(19, 0) NOT NULL,
  installedBy NUMBER(19, 0) NOT NULL,
  provided NUMBER(1),
  lastModificationDate NUMBER(19, 0) NOT NULL,
  lastUpdatedBy NUMBER(19, 0) NOT NULL,
  contentName VARCHAR2(50 CHAR) NOT NULL,
  content BLOB,
  contentType VARCHAR2(50 CHAR),
  processDefinitionId NUMBER(19, 0)
)
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

RENAME page2 TO page
@@

