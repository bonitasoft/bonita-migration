CREATE TABLE business_app (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  version VARCHAR(50) NOT NULL,
  path VARCHAR(255) NOT NULL,
  description TEXT,
  iconPath VARCHAR(255),
  creationDate BIGINT NOT NULL,
  createdBy BIGINT NOT NULL,
  lastUpdateDate BIGINT NOT NULL,
  updatedBy BIGINT NOT NULL,
  state VARCHAR(30) NOT NULL,
  homePageId BIGINT,
  displayName VARCHAR(255),
  UNIQUE (tenantId, name, version),
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;
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
  name VARCHAR(255),
  UNIQUE (tenantId, applicationId, name),
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_app_page_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_bus_app_id FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id) ON DELETE CASCADE;
@@
ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page (tenantid, id);
@@
CREATE INDEX idx_app_page_name ON business_app_page (applicationId, name, tenantid);
@@

INSERT INTO sequence (tenantid, id, nextid)
SELECT ID, 10200, 1 FROM tenant
ORDER BY id ASC;
@@
INSERT INTO sequence (tenantid, id, nextid)
SELECT ID, 10201, 1 FROM tenant
ORDER BY id ASC;
@@