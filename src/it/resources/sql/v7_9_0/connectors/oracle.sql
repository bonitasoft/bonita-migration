CREATE TABLE bar_resource (
  tenantId NUMBER(19, 0) NOT NULL,
id NUMBER(19, 0) NOT NULL,
process_id NUMBER(19, 0) NOT NULL,
name VARCHAR2(255) NOT NULL,
type VARCHAR2(16) NOT NULL,
content BLOB NOT NULL,
UNIQUE (tenantId, process_id, name, type),
PRIMARY KEY (tenantId, id)
);
CREATE INDEX idx_bar_resource ON bar_resource (tenantId, process_id, type, name);

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
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid);
ALTER TABLE dependencymapping ADD CONSTRAINT fk_depmapping_depid FOREIGN KEY (tenantid, dependencyid) REFERENCES dependency(tenantid, id) ON DELETE CASCADE;

CREATE TABLE sequence (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  nextid NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE process_definition (
 tenantid NUMBER(19, 0) NOT NULL,
 processId NUMBER(19, 0) NOT NULL,
 name VARCHAR2(150 CHAR) NOT NULL,
 version VARCHAR2(50 CHAR) NOT NULL
);