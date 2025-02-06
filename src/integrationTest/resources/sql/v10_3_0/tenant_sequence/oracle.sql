CREATE TABLE platform (
  id NUMBER(19, 0) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
  initial_bonita_version VARCHAR2(50 CHAR) NOT NULL,
  application_version VARCHAR2(50 CHAR) NOT NULL,
  maintenance_message VARCHAR2(1024 CHAR),
  maintenance_message_active NUMBER(1) NOT NULL,
  created NUMBER(19, 0) NOT NULL,
  created_by VARCHAR2(50 CHAR) NOT NULL,
  information CLOB,
  PRIMARY KEY (id)
);

CREATE TABLE sequence (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  nextid NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE tenant (
  id NUMBER(19, 0) NOT NULL,
  created NUMBER(19, 0) NOT NULL,
  createdBy VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(255 CHAR),
  defaultTenant NUMBER(1) NOT NULL,
  iconname VARCHAR2(50 CHAR),
  iconpath VARCHAR2(255 CHAR),
  name VARCHAR2(50 CHAR) NOT NULL,
  status VARCHAR2(15 CHAR) NOT NULL,
  PRIMARY KEY (id)
);
