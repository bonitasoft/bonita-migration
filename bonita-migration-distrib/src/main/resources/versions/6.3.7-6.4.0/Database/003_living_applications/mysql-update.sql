CREATE TABLE business_app (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  version VARCHAR(50) NOT NULL,
  profileId BIGINT,
  path VARCHAR(255) NOT NULL,
  description TEXT,
  iconPath VARCHAR(255),
  creationDate BIGINT NOT NULL,
  createdBy BIGINT NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  updatedBy BIGINT NOT NULL,
  state VARCHAR(30) NOT NULL,
  homePageId BIGINT,
  displayName VARCHAR(255) NOT NULL
) ENGINE = INNODB;
@@

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
@@
ALTER TABLE business_app ADD CONSTRAINT uk_app_name_version UNIQUE (tenantId, name, version);
@@
ALTER TABLE business_app ADD CONSTRAINT fk_app_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
@@

CREATE INDEX idx_app_name ON business_app (name, tenantid);
@@

CREATE TABLE business_app_page (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  applicationId BIGINT NOT NULL,
  pageId BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL
) ENGINE = INNODB;
@@

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
@@
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_name UNIQUE (tenantId, applicationId, name);
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_app_page_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_bus_app_id FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id) ON DELETE CASCADE;
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page (tenantid, id);
@@
CREATE INDEX idx_app_page_name ON business_app_page (applicationId, name, tenantid);
@@
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid);
@@

INSERT INTO sequence (tenantid, id, nextid)
SELECT ID, 10200, 1 FROM tenant
ORDER BY id ASC;
@@
INSERT INTO sequence (tenantid, id, nextid)
SELECT ID, 10201, 1 FROM tenant
ORDER BY id ASC;
@@