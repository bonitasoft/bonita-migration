CREATE TABLE tenant (
  id NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (id)
)
@@

CREATE TABLE page (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  displayName NVARCHAR(255) NOT NULL,
  provided BIT,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
)
@@

CREATE TABLE business_app (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  token NVARCHAR(50) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  displayName NVARCHAR(255) NOT NULL
)
@@

ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id)@@


CREATE TABLE business_app_page (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  applicationId NUMERIC(19, 0) NOT NULL,
  pageId NUMERIC(19, 0) NOT NULL,
  token NVARCHAR(255) NOT NULL
)
@@

ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id)