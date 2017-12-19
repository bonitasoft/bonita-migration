CREATE TABLE user_ (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  enabled BIT NOT NULL,
  userName NVARCHAR(255) NOT NULL,
  password NVARCHAR(60),
  firstName NVARCHAR(255),
  lastName NVARCHAR(255),
  title NVARCHAR(50),
  jobTitle NVARCHAR(255),
  managerUserId NUMERIC(19, 0),
  iconName NVARCHAR(50),
  iconPath NVARCHAR(50),
  createdBy NUMERIC(19, 0),
  creationDate NUMERIC(19, 0),
  lastUpdate NUMERIC(19, 0),
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE group_ (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  iconName NVARCHAR(50),
  iconPath NVARCHAR(50),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE role (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  iconName NVARCHAR(50),
  iconPath NVARCHAR(50),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE sequence (
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    nextid NUMERIC(19, 0) NOT NULL,
    PRIMARY KEY (tenantid, id)
)
GO
CREATE TABLE tenant (
  id NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (id)
)
GO