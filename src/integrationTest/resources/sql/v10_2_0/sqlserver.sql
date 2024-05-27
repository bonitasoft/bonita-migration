CREATE TABLE business_app (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  token NVARCHAR(50) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  iconPath NVARCHAR(255),
  creationDate NUMERIC(19, 0) NOT NULL,
  createdBy NUMERIC(19, 0) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  updatedBy NUMERIC(19, 0) NOT NULL,
  state NVARCHAR(30) NOT NULL,
  homePageId NUMERIC(19, 0),
  profileId NUMERIC(19, 0),
  layoutId NUMERIC(19, 0),
  themeId NUMERIC(19, 0),
  iconMimeType NVARCHAR(255),
  iconContent VARBINARY(MAX),
  displayName NVARCHAR(255) NOT NULL,
  editable BIT,
  internalProfile NVARCHAR(255)
)
GO

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id)
GO
ALTER TABLE business_app ADD CONSTRAINT uk_app_token_version UNIQUE (tenantId, token, version)
GO

CREATE INDEX idx_app_token ON business_app (token, tenantid)
GO
CREATE INDEX idx_app_profile ON business_app (profileId, tenantid)
GO
CREATE INDEX idx_app_homepage ON business_app (homePageId, tenantid)
GO