CREATE TABLE token (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processInstanceId NUMBER(19, 0) NOT NULL,
  ref_id NUMBER(19, 0) NOT NULL,
  parent_ref_id NUMBER(19, 0) NULL,
  PRIMARY KEY (tenantid, id)
)
@@
CREATE TABLE flownode_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  flownodeDefinitionId NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25) NOT NULL,
  rootContainerId NUMBER(19, 0) NOT NULL,
  parentContainerId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50) NOT NULL,
  displayName VARCHAR2(75),
  displayDescription VARCHAR2(255),
  stateId INT NOT NULL,
  stateName VARCHAR2(50),
  prev_state_id INT NOT NULL,
  terminal NUMBER(1) NOT NULL,
  stable NUMBER(1) ,
  actorId NUMBER(19, 0) NULL,
  assigneeId NUMBER(19, 0) DEFAULT 0 NOT NULL,
  reachedStateDate NUMBER(19, 0),
  lastUpdateDate NUMBER(19, 0),
  expectedEndDate NUMBER(19, 0),
  claimedDate NUMBER(19, 0),
  priority SMALLINT,
  gatewayType VARCHAR2(50),
  hitBys VARCHAR2(255),
  stateCategory VARCHAR2(50) NOT NULL,
  logicalGroup1 NUMBER(19, 0) NOT NULL,
  logicalGroup2 NUMBER(19, 0) NOT NULL,
  logicalGroup3 NUMBER(19, 0),
  logicalGroup4 NUMBER(19, 0) NOT NULL,
  loop_counter INT,
  loop_max INT,
  description VARCHAR2(255),
  sequential NUMBER(1),
  loopDataInputRef VARCHAR2(255),
  loopDataOutputRef VARCHAR2(255),
  dataInputItemRef VARCHAR2(255),
  dataOutputItemRef VARCHAR2(255),
  loopCardinality INT,
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy NUMBER(19, 0),
  executedByDelegate NUMBER(19, 0),
  activityInstanceId NUMBER(19, 0),
  state_executing NUMBER(1) DEFAULT 0,
  abortedByBoundary NUMBER(19, 0),
  triggeredByEvent NUMBER(1),
  interrupting NUMBER(1),
  deleted NUMBER(1) DEFAULT 0,
  tokenCount INT NOT NULL,
  token_ref_id NUMBER(19, 0) NULL,
  PRIMARY KEY (tenantid, id)
)
@@

CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId)
@@
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (logicalGroup4)
@@
CREATE TABLE sequence (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  nextid NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)