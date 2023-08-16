CREATE TABLE sequence (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  nextid NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE platform (
  id NUMERIC(19, 0) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  initial_bonita_version NVARCHAR(50) NOT NULL,
  created NUMERIC(19, 0) NOT NULL,
  created_by NVARCHAR(50) NOT NULL,
  information NVARCHAR(MAX),
  PRIMARY KEY (id)
)
GO