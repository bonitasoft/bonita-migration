CREATE TABLE dependency (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(150) NOT NULL,
  description NVARCHAR(MAX),
  filename NVARCHAR(255) NOT NULL,
  value_ VARBINARY(MAX) NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_dependency_name ON dependency (name, id)
GO
