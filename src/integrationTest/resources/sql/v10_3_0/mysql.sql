CREATE TABLE sequence (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  nextid BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE tenant (
  id BIGINT NOT NULL,
  created BIGINT NOT NULL,
  createdBy VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  defaultTenant BOOLEAN NOT NULL,
  iconname VARCHAR(50),
  iconpath VARCHAR(255),
  name VARCHAR(50) NOT NULL,
  status VARCHAR(15) NOT NULL,
  PRIMARY KEY (id)
) ENGINE = INNODB;

CREATE TABLE flownode_instance (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  flownodeDefinitionId BIGINT NOT NULL,
  kind VARCHAR(25) NOT NULL,
  rootContainerId BIGINT NOT NULL,
  parentContainerId BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  displayName VARCHAR(255),
  displayDescription VARCHAR(255),
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  prev_state_id INT NOT NULL,
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  actorId BIGINT NULL,
  assigneeId BIGINT DEFAULT 0 NOT NULL,
  reachedStateDate BIGINT,
  lastUpdateDate BIGINT,
  expectedEndDate BIGINT,
  claimedDate BIGINT,
  priority TINYINT,
  gatewayType VARCHAR(50),
  hitBys VARCHAR(255),
  stateCategory VARCHAR(50) NOT NULL,
  logicalGroup1 BIGINT NOT NULL,
  logicalGroup2 BIGINT NOT NULL,
  logicalGroup3 BIGINT,
  logicalGroup4 BIGINT NOT NULL,
  loop_counter INT,
  loop_max INT,
  description VARCHAR(255),
  sequential BOOLEAN,
  loopDataInputRef VARCHAR(255),
  loopDataOutputRef VARCHAR(255),
  dataInputItemRef VARCHAR(255),
  dataOutputItemRef VARCHAR(255),
  loopCardinality INT,
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy BIGINT,
  executedBySubstitute BIGINT,
  activityInstanceId BIGINT,
  state_executing BOOLEAN DEFAULT FALSE,
  abortedByBoundary BIGINT,
  triggeredByEvent BOOLEAN,
  interrupting BOOLEAN,
  tokenCount INT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId);
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (logicalGroup4);
CREATE INDEX idx_fni_loggroup3_terminal ON flownode_instance(logicalgroup3, terminal);
CREATE INDEX idx_fn_lg2_state ON flownode_instance (logicalGroup2, stateName);
CREATE INDEX idx_fni_activity_instance_id_kind ON flownode_instance(activityInstanceId, kind);
ALTER TABLE flownode_instance ADD CONSTRAINT fk_flownode_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE pending_mapping (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  activityId BIGINT NOT NULL,
  actorId BIGINT,
  userId BIGINT,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE UNIQUE INDEX idx_UQ_pending_mapping ON pending_mapping (activityId, userId, actorId);
ALTER TABLE pending_mapping ADD CONSTRAINT fk_pending_mapping_flownode_instanceId FOREIGN KEY (tenantid, activityId) REFERENCES flownode_instance(tenantid, id);

CREATE TABLE ref_biz_data_inst (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  kind VARCHAR(15) NOT NULL,
  name VARCHAR(255) NOT NULL,
  proc_inst_id BIGINT,
  fn_inst_id BIGINT,
  data_id BIGINT,
  data_classname VARCHAR(255) NOT NULL
) ENGINE = INNODB;
CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (fn_inst_id);
CREATE INDEX idx_biz_data_inst3 ON ref_biz_data_inst (proc_inst_id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE;

CREATE TABLE form_mapping (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  process BIGINT NOT NULL,
  type INT NOT NULL,
  task VARCHAR(255),
  page_mapping_tenant_id BIGINT,
  page_mapping_id BIGINT,
  lastUpdateDate BIGINT,
  lastUpdatedBy BIGINT,
  target VARCHAR(16) NOT NULL,
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;

CREATE TABLE page_mapping (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  key_ VARCHAR(255) NOT NULL,
  pageId BIGINT NULL,
  url VARCHAR(1024) NULL,
  urladapter VARCHAR(255) NULL,
  page_authoriz_rules TEXT NULL,
  lastUpdateDate BIGINT NULL,
  lastUpdatedBy BIGINT NULL,
  PRIMARY KEY (tenantId, id),
  UNIQUE (tenantId, key_)
) ENGINE = INNODB;

ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id);
