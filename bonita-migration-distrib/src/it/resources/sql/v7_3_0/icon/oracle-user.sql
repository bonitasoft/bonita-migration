CREATE TABLE user_ (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  enabled NUMBER(1) NOT NULL,
  userName VARCHAR2(255 CHAR) NOT NULL,
  password VARCHAR2(60 CHAR),
  firstName VARCHAR2(255 CHAR),
  lastName VARCHAR2(255 CHAR),
  title VARCHAR2(50 CHAR),
  jobTitle VARCHAR2(255 CHAR),
  managerUserId NUMBER(19, 0),
  iconName VARCHAR2(50 CHAR),
  iconPath VARCHAR2(50 CHAR),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  CONSTRAINT UK_User UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE group_ (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  iconName VARCHAR2(50 CHAR),
  iconPath VARCHAR2(50 CHAR),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE role (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  iconName VARCHAR2(50 CHAR),
  iconPath VARCHAR2(50 CHAR),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE sequence (
    tenantid NUMBER(19, 0) NOT NULL,
    id NUMBER(19, 0) NOT NULL,
    nextid NUMBER(19, 0) NOT NULL,
    PRIMARY KEY (tenantid, id)
  )