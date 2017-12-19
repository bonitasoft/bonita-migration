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