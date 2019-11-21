CREATE TABLE dependency (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(150 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  filename VARCHAR2(255 CHAR) NOT NULL,
  value_ BLOB NOT NULL,
  CONSTRAINT UK_Dependency UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_dependency_name ON dependency (name);
CREATE TABLE dependencymapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  artifactid NUMBER(19, 0) NOT NULL,
  artifacttype VARCHAR2(50 CHAR) NOT NULL,
  dependencyid NUMBER(19, 0) NOT NULL,
  CONSTRAINT UK_Dependency_Mapping UNIQUE (tenantid, dependencyid, artifactid, artifacttype),
  PRIMARY KEY (tenantid, id)
);