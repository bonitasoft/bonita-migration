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