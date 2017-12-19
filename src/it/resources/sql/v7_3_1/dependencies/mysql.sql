CREATE TABLE dependency (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  filename VARCHAR(255) NOT NULL,
  value_ MEDIUMBLOB NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_dependency_name ON dependency (name);