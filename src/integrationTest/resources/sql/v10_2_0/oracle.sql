CREATE TABLE business_app (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  token VARCHAR2(50 CHAR) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  iconPath VARCHAR2(255 CHAR),
  creationDate NUMBER(19, 0) NOT NULL,
  createdBy NUMBER(19, 0) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  updatedBy NUMBER(19, 0) NOT NULL,
  state VARCHAR2(30 CHAR) NOT NULL,
  homePageId NUMBER(19, 0),
  profileId NUMBER(19, 0),
  layoutId NUMBER(19, 0),
  themeId NUMBER(19, 0),
  iconMimeType VARCHAR2(255 CHAR),
  iconContent BLOB,
  displayName VARCHAR2(255 CHAR) NOT NULL,
  editable NUMBER(1),
  internalProfile VARCHAR2(255 CHAR)
);

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT UK_Business_app UNIQUE (tenantId, token, version);

CREATE INDEX idx_app_token ON business_app (token, tenantid);
CREATE INDEX idx_app_profile ON business_app (profileId, tenantid);
CREATE INDEX idx_app_homepage ON business_app (homePageId, tenantid);