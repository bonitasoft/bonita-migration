CREATE TABLE bar_resource (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  process_id INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(16) NOT NULL,
  content BYTEA NOT NULL,
  UNIQUE (tenantId, process_id, name, type),
PRIMARY KEY (tenantId, id)
);
CREATE INDEX idx_bar_resource ON bar_resource (tenantId, process_id, type, name);

CREATE TABLE dependency (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  filename VARCHAR(255) NOT NULL,
  value_ BYTEA NOT NULL,
  UNIQUE (tenantId, name),
PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependency_name ON dependency (name);

CREATE TABLE dependencymapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  artifactid INT8 NOT NULL,
  artifacttype VARCHAR(50) NOT NULL,
  dependencyid INT8 NOT NULL,
  UNIQUE (tenantid, dependencyid, artifactid, artifacttype),
PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid);
ALTER TABLE dependencymapping ADD CONSTRAINT fk_depmapping_depid FOREIGN KEY (tenantid, dependencyid) REFERENCES dependency(tenantid, id) ON DELETE CASCADE;

CREATE TABLE sequence (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  nextid INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE process_definition (
 tenantid INT8 NOT NULL,
 processId INT8 NOT NULL,
 name VARCHAR(150) NOT NULL,
 version VARCHAR(50) NOT NULL
);