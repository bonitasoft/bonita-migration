CREATE TABLE platform (
  id NUMERIC(19, 0) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  initial_bonita_version NVARCHAR(50) NOT NULL,
  application_version NVARCHAR(50) NOT NULL,
  maintenance_message NVARCHAR(MAX),
  maintenance_message_active BIT NOT NULL,
  created NUMERIC(19, 0) NOT NULL,
  created_by NVARCHAR(50) NOT NULL,
  information NVARCHAR(MAX),
  PRIMARY KEY (id)
);

CREATE TABLE sequence (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  nextid NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

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
);
