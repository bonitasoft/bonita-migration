CREATE TABLE page (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  displayName NVARCHAR(255) NOT NULL,
  description NVARCHAR(MAX),
  installationDate NUMERIC(19, 0) NOT NULL,
  installedBy NUMERIC(19, 0) NOT NULL,
  provided BIT,
  hidden BIT NOT NULL,
  lastModificationDate NUMERIC(19, 0) NOT NULL,
  lastUpdatedBy NUMERIC(19, 0) NOT NULL,
  contentName NVARCHAR(280) NOT NULL,
  content VARBINARY(MAX),
  contentType NVARCHAR(50) NOT NULL,
  processDefinitionId NUMERIC(19,0) NOT NULL
)
GO
ALTER TABLE page ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id)
GO
ALTER TABLE page ADD CONSTRAINT  uk_page UNIQUE  (tenantId, name, processDefinitionId)
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
  created NUMERIC(19, 0) NOT NULL,
  createdBy NVARCHAR(50) NOT NULL,
  description NVARCHAR(255),
  defaultTenant BIT NOT NULL,
  iconname NVARCHAR(50),
  iconpath NVARCHAR(255),
  name NVARCHAR(50) NOT NULL,
  status NVARCHAR(15) NOT NULL,
  PRIMARY KEY (id)
)
GO