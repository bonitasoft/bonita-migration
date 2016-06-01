CREATE TABLE user_ (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  enabled BOOLEAN NOT NULL,
  userName VARCHAR(255) NOT NULL,
  password VARCHAR(60),
  firstName VARCHAR(255),
  lastName VARCHAR(255),
  title VARCHAR(50),
  jobTitle VARCHAR(255),
  managerUserId BIGINT,
  iconName VARCHAR(50),
  iconPath VARCHAR(50),
  createdBy BIGINT,
  creationDate BIGINT,
  lastUpdate BIGINT,
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE TABLE group_ (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  iconName VARCHAR(50),
  iconPath VARCHAR(50),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE TABLE role (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  iconName VARCHAR(50),
  iconPath VARCHAR(50),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE TABLE sequence (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  nextid BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB
