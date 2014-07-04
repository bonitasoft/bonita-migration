CREATE TABLE tenant (
  id NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (id)
)
@@

CREATE TABLE sequence (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  nextid NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
@@

CREATE TABLE command (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  IMPLEMENTATION NVARCHAR(100) NOT NULL,
  system BIT,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
)
@@