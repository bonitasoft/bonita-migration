CREATE TABLE bar_resource (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  process_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(16) NOT NULL,
  content LONGBLOB NOT NULL,
  UNIQUE (tenantId, process_id, name, type),
PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;
CREATE INDEX idx_bar_resource ON bar_resource (tenantId, process_id, type, name);

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

CREATE TABLE dependencymapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  artifactid BIGINT NOT NULL,
  artifacttype VARCHAR(50) NOT NULL,
  dependencyid BIGINT NOT NULL,
  UNIQUE (tenantid, dependencyid, artifactid, artifacttype),
PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid);
ALTER TABLE dependencymapping ADD CONSTRAINT fk_depmapping_depid FOREIGN KEY (tenantid, dependencyid) REFERENCES dependency(tenantid, id) ON DELETE CASCADE;

CREATE TABLE sequence (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  nextid BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE process_definition (
 tenantid BIGINT NOT NULL,
 processId BIGINT NOT NULL,
 name VARCHAR(150) NOT NULL,
 version VARCHAR(50) NOT NULL
) ENGINE = INNODB;