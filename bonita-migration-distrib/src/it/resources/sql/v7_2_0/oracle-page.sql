CREATE TABLE page (
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
CREATE TABLE profileentry (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  profileId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR),
  description VARCHAR2(1024 CHAR),
  parentId NUMBER(19, 0),
  index_ NUMBER(19, 0),
  type VARCHAR2(50 CHAR),
  page VARCHAR2(50 CHAR),
  custom NUMBER(1) DEFAULT 0,
  PRIMARY KEY (tenantId, id)
)