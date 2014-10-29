CREATE TABLE business_app (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  token VARCHAR(50) NOT NULL,
  version VARCHAR(50) NOT NULL,
  profileId INT8,
  description TEXT,
  iconPath VARCHAR(255),
  creationDate INT8 NOT NULL,
  createdBy INT8 NOT NULL,
  lastUpdateDate INT8 NOT NULL,
  updatedBy INT8 NOT NULL,
  state VARCHAR(30) NOT NULL,
  homePageId INT8,
  displayName VARCHAR(255) NOT NULL
);
@@

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
@@
ALTER TABLE business_app ADD CONSTRAINT uk_app_token_version UNIQUE (tenantId, token, version);
@@
ALTER TABLE business_app ADD CONSTRAINT fk_app_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
@@
ALTER TABLE business_app ADD CONSTRAINT fk_app_profileId FOREIGN KEY (tenantid, profileId) REFERENCES profile (tenantid, id);
@@

CREATE INDEX idx_app_token ON business_app (token, tenantid);
@@

CREATE TABLE business_app_page (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  applicationId INT8 NOT NULL,
  pageId INT8 NOT NULL,
  token VARCHAR(255) NOT NULL
);
@@

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
@@
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_token UNIQUE (tenantId, applicationId, token);
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_app_page_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_bus_app_id FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id) ON DELETE CASCADE;
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page (tenantid, id);
@@

CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token, tenantid);
@@
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid);
@@

CREATE TABLE business_app_menu (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  applicationId INT8 NOT NULL,
  applicationPageId INT8,
  parentId INT8,
  index_ INT8
);
@@

ALTER TABLE business_app_menu ADD CONSTRAINT pk_business_app_menu PRIMARY KEY (tenantid, id);
@@

ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
@@

ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_appId FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id) ON DELETE CASCADE;
@@

ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_pageId FOREIGN KEY (tenantid, applicationPageId) REFERENCES business_app_page (tenantid, id) ON DELETE CASCADE;
@@

ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_parentId FOREIGN KEY (tenantid, parentId) REFERENCES business_app_menu (tenantid, id);
@@

CREATE INDEX idx_app_menu_app ON business_app_menu (applicationId, tenantid);
@@
CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId, tenantid);
@@
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId, tenantid);
@@

INSERT INTO sequence (tenantid, id, nextid)
	SELECT ID, 10200, 1 FROM tenant
	ORDER BY id ASC;
@@
INSERT INTO sequence (tenantid, id, nextid)
	SELECT ID, 10201, 1 FROM tenant
	ORDER BY id ASC;
@@
INSERT INTO sequence (tenantid, id, nextid)
	SELECT ID, 10202, 1 FROM tenant
	ORDER BY id ASC;
