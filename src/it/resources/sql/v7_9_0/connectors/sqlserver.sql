CREATE TABLE bar_resource (
  tenantId NUMERIC(19, 0) NOT NULL,
id NUMERIC(19, 0) NOT NULL,
process_id NUMERIC(19, 0) NOT NULL,
name NVARCHAR(255) NOT NULL,
type NVARCHAR(16) NOT NULL,
content VARBINARY(MAX) NOT NULL,
UNIQUE (tenantId, process_id, name, type),
PRIMARY KEY (tenantId, id)
)
GO
CREATE INDEX idx_bar_resource ON bar_resource (tenantId, process_id, type, name)
GO

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
CREATE TABLE dependencymapping (
  tenantid NUMERIC(19, 0) NOT NULL,
id NUMERIC(19, 0) NOT NULL,
artifactid NUMERIC(19, 0) NOT NULL,
artifacttype NVARCHAR(50) NOT NULL,
dependencyid NUMERIC(19, 0) NOT NULL,
UNIQUE (tenantid, dependencyid, artifactid, artifacttype),
PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid, id)
GO
ALTER TABLE dependencymapping ADD CONSTRAINT fk_depmapping_depid FOREIGN KEY (tenantid, dependencyid) REFERENCES dependency(tenantid, id) ON DELETE CASCADE
GO

CREATE TABLE sequence (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  nextid NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE process_definition (
  tenantid NUMERIC(19, 0) NOT NULL,
  processId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(150) NOT NULL,
  version NVARCHAR(50) NOT NULL
)
GO

