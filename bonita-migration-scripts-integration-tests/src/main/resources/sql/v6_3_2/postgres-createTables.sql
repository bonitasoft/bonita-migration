CREATE TABLE arch_data_instance (
    tenantId INT8 NOT NULL,
	id INT8 NOT NULL,
	name VARCHAR(50),
	sourceObjectId INT8 NOT NULL,
	PRIMARY KEY (tenantid, id)
);

CREATE TABLE arch_data_mapping (
    tenantid INT8 NOT NULL,
	id INT8 NOT NULL,
	dataName VARCHAR(50),
	dataInstanceId INT8 NOT NULL,
	sourceObjectId INT8 NOT NULL,
	PRIMARY KEY (tenantid, id)
);

CREATE TABLE flownode_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  parentContainerId INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  prev_state_id INT NOT NULL,
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  hitBys VARCHAR(255),
  token_ref_id INT8 NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE arch_flownode_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  sourceObjectId INT8 NOT NULL,
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  hitBys VARCHAR(255),
  PRIMARY KEY (tenantid, id)
);
CREATE TABLE tenant (
  id INT8 NOT NULL,
  PRIMARY KEY (id)
);
CREATE TABLE token (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processInstanceId INT8 NOT NULL,
  ref_id INT8 NOT NULL,
  parent_ref_id INT8 NULL,
  PRIMARY KEY (tenantid, id)
);