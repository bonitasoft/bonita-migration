CREATE TABLE business_app (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  path NVARCHAR(255) NOT NULL,
  description NVARCHAR(MAX),
  iconPath NVARCHAR(255),
  creationDate NUMERIC(19, 0) NOT NULL,
  createdBy NUMERIC(19, 0) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  updatedBy NUMERIC(19, 0) NOT NULL,
  state NVARCHAR(30) NOT NULL,
  homePageId NUMERIC(19, 0),
  displayName NVARCHAR(255),
  UNIQUE (tenantId, name, version),
  PRIMARY KEY (tenantId, id)
)
@@

CREATE INDEX idx_app_name ON business_app (name, tenantid)
@@
ALTER TABLE business_app ADD CONSTRAINT fk_app_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id)
@@

CREATE TABLE business_app_page (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  applicationId NUMERIC(19, 0) NOT NULL,
  pageId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255),
  UNIQUE (tenantId, applicationId, name),
  PRIMARY KEY (tenantId, id)
)
@@

CREATE INDEX idx_app_page_name ON business_app_page (applicationId, name, tenantid)
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_app_page_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id)
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_bus_app_id FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id) ON DELETE CASCADE
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page (tenantid, id)
@@

INSERT INTO sequence (tenantid, id, nextid)
	SELECT id, 10200, 1 FROM tenant
	ORDER BY id ASC
@@

INSERT INTO sequence (tenantid, id, nextid)
	SELECT id, 10201, 1 FROM tenant
	ORDER BY id ASC
@@
