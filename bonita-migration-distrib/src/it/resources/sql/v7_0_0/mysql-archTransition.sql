
CREATE TABLE tenant (
  id BIGINT NOT NULL,
  PRIMARY KEY (id)
) ENGINE = INNODB;

CREATE TABLE arch_transition_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  rootContainerId BIGINT NOT NULL,
  parentContainerId BIGINT NOT NULL,
  source BIGINT,
  target BIGINT,
  state VARCHAR(50),
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  stateCategory VARCHAR(50) NOT NULL,
  logicalGroup1 BIGINT NOT NULL,
  logicalGroup2 BIGINT NOT NULL,
  logicalGroup3 BIGINT,
  logicalGroup4 BIGINT NOT NULL,
  description VARCHAR(255),
  sourceObjectId BIGINT,
  archiveDate BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE INDEX idx1_arch_transition_instance ON arch_transition_instance (tenantid, rootcontainerid);

ALTER TABLE arch_transition_instance ADD CONSTRAINT fk_arch_transition_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);