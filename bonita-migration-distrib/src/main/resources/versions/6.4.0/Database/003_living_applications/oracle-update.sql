CREATE TABLE business_app (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  token VARCHAR2(50) NOT NULL,
  version VARCHAR2(50) NOT NULL,
  profileId NUMBER(19, 0),
  description VARCHAR2(1024),
  iconPath VARCHAR2(255),
  creationDate NUMBER(19, 0) NOT NULL,
  createdBy NUMBER(19, 0) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  updatedBy NUMBER(19, 0) NOT NULL,
  state VARCHAR2(30) NOT NULL,
  homePageId NUMBER(19, 0),
  displayName VARCHAR2(255) NOT NULL
)
@@
ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id)
@@

CREATE INDEX idx_app_token ON business_app (token, tenantid)
@@
CREATE INDEX idx_app_profile ON business_app (profileId, tenantid)
@@
CREATE INDEX idx_app_homepage ON business_app (homePageId, tenantid)
@@

CREATE TABLE business_app_page (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  applicationId NUMBER(19, 0) NOT NULL,
  pageId NUMBER(19, 0) NOT NULL,
  token VARCHAR2(255) NOT NULL
)
@@
ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id)
@@


CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token, tenantid)
@@
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid)
@@

CREATE TABLE business_app_menu (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  displayName VARCHAR2(255) NOT NULL,
  applicationId NUMBER(19, 0) NOT NULL,
  applicationPageId NUMBER(19, 0),
  parentId NUMBER(19, 0),
  index_ NUMBER(19, 0)
)
@@
ALTER TABLE business_app_menu ADD CONSTRAINT pk_business_app_menu PRIMARY KEY (tenantid, id)
@@

CREATE INDEX idx_app_menu_app ON business_app_menu (applicationId, tenantid)
@@
CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId, tenantid)
@@
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId, tenantid)
@@

ALTER TABLE business_app ADD CONSTRAINT uk_app_token_version UNIQUE (tenantId, token, version)
@@
ALTER TABLE business_app ADD CONSTRAINT fk_app_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id)
@@
ALTER TABLE business_app ADD CONSTRAINT fk_app_profileId FOREIGN KEY (tenantid, profileId) REFERENCES profile (tenantid, id)
@@

ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_token UNIQUE (tenantId, applicationId, token)
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_app_page_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id)
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_bus_app_id FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id) ON DELETE CASCADE
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page (tenantid, id)
@@

ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id)
@@
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_appId FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id)
@@
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_pageId FOREIGN KEY (tenantid, applicationPageId) REFERENCES business_app_page (tenantid, id)
@@
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_parentId FOREIGN KEY (tenantid, parentId) REFERENCES business_app_menu (tenantid, id)
@@

INSERT INTO "SEQUENCE" ("TENANTID", "ID", "NEXTID")
	SELECT "ID", 10200, 1 FROM "TENANT"
	ORDER BY "ID" ASC 
@@
	
INSERT INTO "SEQUENCE" ("TENANTID", "ID", "NEXTID")
	SELECT "ID", 10201, 1 FROM "TENANT"
	ORDER BY "ID" ASC 
@@

INSERT INTO "SEQUENCE" ("TENANTID", "ID", "NEXTID")
	SELECT "ID", 10202, 1 FROM "TENANT"
	ORDER BY "ID" ASC
@@
