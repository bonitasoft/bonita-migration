CREATE TABLE tenant (
  id INT8 NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE page (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  provided BOOLEAN,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE business_app (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  token VARCHAR(50) NOT NULL,
  version VARCHAR(50) NOT NULL,
  displayName VARCHAR(255) NOT NULL
);

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);

CREATE TABLE business_app_page (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  applicationId INT8 NOT NULL,
  pageId INT8 NOT NULL,
  token VARCHAR(255) NOT NULL
);

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);