CREATE TABLE page (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  displayName VARCHAR2(255 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  installationDate NUMBER(19, 0) NOT NULL,
  installedBy NUMBER(19, 0) NOT NULL,
  provided NUMBER(1),
  lastModificationDate NUMBER(19, 0) NOT NULL,
  lastUpdatedBy NUMBER(19, 0) NOT NULL,
  contentName VARCHAR2(280 CHAR) NOT NULL,
  content BLOB,
  contentType VARCHAR2(50 CHAR),
  processDefinitionId NUMBER(19, 0)
);

ALTER TABLE page ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id);

ALTER TABLE page ADD CONSTRAINT uk_page UNIQUE (tenantId, name, processDefinitionId);
