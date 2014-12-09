CREATE TABLE arch_data_instance (
    tenantId NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	name VARCHAR2(50),
	sourceObjectId NUMBER(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
)@@
CREATE TABLE arch_data_mapping (
    tenantid NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	dataName VARCHAR2(50),
	dataInstanceId NUMBER(19, 0) NOT NULL,
	sourceObjectId NUMBER(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
)@@
CREATE TABLE flownode_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  parentContainerId NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25) NOT NULL,
  stateId INT NOT NULL,
  stateName VARCHAR2(50),
  prev_state_id INT NOT NULL,
  terminal NUMBER(1) NOT NULL,
  stable NUMBER(1) ,
  hitBys VARCHAR2(255),
  token_ref_id NUMBER(19, 0) NULL,
  PRIMARY KEY (tenantid, id)
)@@
CREATE TABLE arch_flownode_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL,
  stateId INT NOT NULL,
  stateName VARCHAR2(50),
  terminal NUMBER(1) NOT NULL,
  stable NUMBER(1) ,
  hitBys VARCHAR2(255),
  PRIMARY KEY (tenantid, id)
)@@
CREATE TABLE tenant (
  id NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (id)
)@@
CREATE TABLE token (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processInstanceId NUMBER(19, 0) NOT NULL,
  ref_id NUMBER(19, 0) NOT NULL,
  parent_ref_id NUMBER(19, 0) NULL,
  PRIMARY KEY (tenantid, id)
)
