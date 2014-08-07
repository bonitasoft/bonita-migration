CREATE TABLE arch_data_instance (
    tenantId BIGINT NOT NULL,
	id BIGINT NOT NULL,
	name VARCHAR(50),
	sourceObjectId BIGINT NOT NULL,
	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE arch_data_mapping (
    tenantid BIGINT NOT NULL,
	id BIGINT NOT NULL,
	dataName VARCHAR(50),
	dataInstanceId BIGINT NOT NULL,
	sourceObjectId BIGINT NOT NULL,
	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE flownode_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  parentContainerId BIGINT NOT NULL,
  kind VARCHAR(25) NOT NULL,
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  prev_state_id INT NOT NULL,
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  hitBys VARCHAR(255),
  token_ref_id BIGINT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE arch_flownode_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  sourceObjectId BIGINT,
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  hitBys VARCHAR(255),
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE tenant (
  id BIGINT NOT NULL,
  PRIMARY KEY (id)
) ENGINE = INNODB;

CREATE TABLE token (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processInstanceId BIGINT NOT NULL,
  ref_id BIGINT NOT NULL,
  parent_ref_id BIGINT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
