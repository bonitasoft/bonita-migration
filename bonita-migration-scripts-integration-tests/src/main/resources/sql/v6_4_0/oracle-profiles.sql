CREATE TABLE tenant (
  id NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (id)
)@@
CREATE TABLE sequence (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  nextid NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)@@
CREATE TABLE profile (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  isDefault NUMBER(1) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  description VARCHAR2(1024),
  creationDate NUMBER(19, 0) NOT NULL,
  createdBy NUMBER(19, 0) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  lastUpdatedBy NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
)@@

CREATE TABLE profileentry (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  profileId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50),
  description VARCHAR2(1024),
  parentId NUMBER(19, 0),
  index_ NUMBER(19, 0),
  type VARCHAR2(50),
  page VARCHAR2(50),
  custom NUMBER(1) DEFAULT 0,
  PRIMARY KEY (tenantId, id)
)@@