CREATE TABLE user_ (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  enabled BOOLEAN NOT NULL,
  userName VARCHAR(255) NOT NULL,
  password VARCHAR(60),
  firstName VARCHAR(255),
  lastName VARCHAR(255),
  title VARCHAR(50),
  jobTitle VARCHAR(255),
  managerUserId INT8,
  iconName VARCHAR(50),
  iconPath VARCHAR(50),
  createdBy INT8,
  creationDate INT8,
  lastUpdate INT8,
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE group_ (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  iconName VARCHAR(50),
  iconPath VARCHAR(50),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE role (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  iconName VARCHAR(50),
  iconPath VARCHAR(50),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE sequence (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  nextid INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE tenant (
  id INT8 NOT NULL,
  PRIMARY KEY (id)
);