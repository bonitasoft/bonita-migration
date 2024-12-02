-- table process_instance is here for foreign key only:
CREATE TABLE process_instance (
  id INT8 NOT NULL,
  name VARCHAR(75) NOT NULL,
  processDefinitionId INT8 NOT NULL,
  description VARCHAR(255),
  startDate INT8 NOT NULL,
  startedBy INT8 NOT NULL,
  startedBySubstitute INT8 NOT NULL,
  endDate INT8 NOT NULL,
  stateId INT NOT NULL,
  stateCategory VARCHAR(50) NOT NULL,
  lastUpdate INT8 NOT NULL,
  containerId INT8,
  rootProcessInstanceId INT8,
  callerId INT8,
  callerType VARCHAR(50),
  interruptingEventId INT8,
  stringIndex1 VARCHAR(255),
  stringIndex2 VARCHAR(255),
  stringIndex3 VARCHAR(255),
  stringIndex4 VARCHAR(255),
  stringIndex5 VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE job_desc (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  jobclassname VARCHAR(100) NOT NULL,
  jobname VARCHAR(100) NOT NULL,
  description VARCHAR(50),
  PRIMARY KEY (tenantid, id)
);
-- no index for now, but will be created to be aligned with MySQL

CREATE TABLE job_param (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  jobDescriptorId INT8 NOT NULL,
  key_ VARCHAR(50) NOT NULL,
  value_ BYTEA NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_job_param_tenant_jobid ON job_param (tenantid, jobDescriptorId);
ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;

CREATE TABLE job_log (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  jobDescriptorId INT8 NOT NULL,
  retryNumber INT8,
  lastUpdateDate INT8,
  lastMessage TEXT,
  PRIMARY KEY (tenantid, id, jobDescriptorId)
);
-- no index for now, but will be created to be aligned with MySQL
ALTER TABLE job_log ADD CONSTRAINT fk_job_log_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;

CREATE TABLE tenant_resource (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(16) NOT NULL,
  content BYTEA NOT NULL,
  lastUpdatedBy INT8 NOT NULL,
  lastUpdateDate INT8,
  state VARCHAR(50) NOT NULL,
  CONSTRAINT UK_tenant_resource UNIQUE (tenantId, name, type),
  PRIMARY KEY (tenantId, id)
);
CREATE INDEX idx_tenant_resource ON tenant_resource (tenantId, type, name);

CREATE TABLE bar_resource (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  process_id INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(16) NOT NULL,
  content BYTEA NOT NULL,
  UNIQUE (tenantId, process_id, name, type),
  PRIMARY KEY (tenantId, id)
);
CREATE INDEX idx_bar_resource ON bar_resource (tenantId, process_id, type, name);

CREATE TABLE user_ (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  enabled BOOLEAN NOT NULL,
  userName VARCHAR(255) NOT NULL,
  password VARCHAR(60),
  firstName VARCHAR(255),
  lastName VARCHAR(255),
  title VARCHAR(50),
  jobTitle VARCHAR(255),
  managerUserId INT8,
  createdBy INT8,
  creationDate INT8,
  lastUpdate INT8,
  iconid INT8,
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_user_name ON user_ (tenantid, userName);

CREATE TABLE role (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  displayName VARCHAR(255),
  description TEXT,
  createdBy INT8,
  creationDate INT8,
  lastUpdate INT8,
  iconid INT8,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_role_name ON role (tenantid, name);

CREATE TABLE group_ (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(125) NOT NULL,
  parentPath VARCHAR(255),
  displayName VARCHAR(255),
  description TEXT,
  createdBy INT8,
  creationDate INT8,
  lastUpdate INT8,
  iconid INT8,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_group_name ON group_ (tenantid, parentPath, name);

CREATE TABLE custom_usr_inf_def (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(75) NOT NULL,
  description TEXT,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_custom_usr_inf_def_name ON custom_usr_inf_def (tenantid, name);

CREATE TABLE user_contactinfo (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  userId INT8 NOT NULL,
  email VARCHAR(255),
  phone VARCHAR(50),
  mobile VARCHAR(50),
  fax VARCHAR(50),
  building VARCHAR(50),
  room VARCHAR(50),
  address VARCHAR(255),
  zipCode VARCHAR(50),
  city VARCHAR(255),
  state VARCHAR(255),
  country VARCHAR(255),
  website VARCHAR(255),
  personal BOOLEAN NOT NULL,
  UNIQUE (tenantid, userId, personal),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_contact_user FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;
CREATE INDEX idx_user_contactinfo ON user_contactinfo (userId, tenantid, personal);

CREATE TABLE arch_data_instance (
    tenantId INT8 NOT NULL,
	id INT8 NOT NULL,
	name VARCHAR(50),
	description VARCHAR(50),
	transientData BOOLEAN,
	className VARCHAR(100),
	containerId INT8,
	containerType VARCHAR(60),
	namespace VARCHAR(100),
	element VARCHAR(60),
	intValue INT,
	longValue INT8,
	shortTextValue VARCHAR(255),
	booleanValue BOOLEAN,
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BYTEA,
	clobValue TEXT,
	discriminant VARCHAR(50) NOT NULL,
	archiveDate INT8 NOT NULL,
	sourceObjectId INT8 NOT NULL,
	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId, containerId, containerType, archiveDate, name, sourceObjectId);
CREATE INDEX idx2_arch_data_instance ON arch_data_instance (sourceObjectId, containerId, archiveDate, id, tenantId);

CREATE TABLE data_instance (
    tenantId INT8 NOT NULL,
	id INT8 NOT NULL,
	name VARCHAR(50),
	description VARCHAR(50),
	transientData BOOLEAN,
	className VARCHAR(100),
	containerId INT8,
	containerType VARCHAR(60),
	namespace VARCHAR(100),
	element VARCHAR(60),
	intValue INT,
	longValue INT8,
	shortTextValue VARCHAR(255),
	booleanValue BOOLEAN,
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BYTEA,
	clobValue TEXT,
	discriminant VARCHAR(50) NOT NULL,
	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_datai_container ON data_instance (tenantId, containerId, containerType, name);

CREATE TABLE business_app (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  token VARCHAR(50) NOT NULL,
  version VARCHAR(50) NOT NULL,
  description TEXT,
  iconPath VARCHAR(255),
  creationDate INT8 NOT NULL,
  createdBy INT8 NOT NULL,
  lastUpdateDate INT8 NOT NULL,
  updatedBy INT8 NOT NULL,
  state VARCHAR(30) NOT NULL,
  homePageId INT8,
  profileId INT8,
  layoutId INT8,
  themeId INT8,
  iconMimeType VARCHAR(255),
  iconContent BYTEA,
  displayName VARCHAR(255) NOT NULL,
  editable BOOLEAN,
  internalProfile VARCHAR(255)
);
ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT uk_app_token_version UNIQUE (tenantId, token, version);
CREATE INDEX idx_app_token ON business_app (token, tenantid);
CREATE INDEX idx_app_profile ON business_app (profileId, tenantid);
CREATE INDEX idx_app_homepage ON business_app (homePageId, tenantid);

CREATE TABLE business_app_page (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  applicationId INT8 NOT NULL,
  pageId INT8 NOT NULL,
  token VARCHAR(255) NOT NULL
);
ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_token UNIQUE (tenantId, applicationId, token);
CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token, tenantid);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid);

CREATE TABLE business_app_menu (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  applicationId INT8 NOT NULL,
  applicationPageId INT8,
  parentId INT8,
  index_ INT8
);
ALTER TABLE business_app_menu ADD CONSTRAINT pk_business_app_menu PRIMARY KEY (tenantid, id);
CREATE INDEX idx_app_menu_app ON business_app_menu (applicationId, tenantid);
CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId, tenantid);
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId, tenantid);

CREATE TABLE waiting_event (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	eventType VARCHAR(50),
  	messageName VARCHAR(255),
  	signalName VARCHAR(255),
  	errorCode VARCHAR(255),
  	processName VARCHAR(150),
  	flowNodeName VARCHAR(50),
  	flowNodeDefinitionId INT8,
  	subProcessId INT8,
  	processDefinitionId INT8,
  	rootProcessInstanceId INT8,
  	parentProcessInstanceId INT8,
  	flowNodeInstanceId INT8,
  	relatedActivityInstanceId INT8,
  	locked BOOLEAN,
  	active BOOLEAN,
  	progress SMALLINT,
  	correlation1 VARCHAR(128),
  	correlation2 VARCHAR(128),
  	correlation3 VARCHAR(128),
  	correlation4 VARCHAR(128),
  	correlation5 VARCHAR(128),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_waiting_event ON waiting_event (progress, tenantid, kind, locked, active);
CREATE INDEX idx_waiting_event_correl ON waiting_event (correlation1, correlation2, correlation3, correlation4, correlation5);

CREATE TABLE pending_mapping (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	activityId INT8 NOT NULL,
  	actorId INT8,
  	userId INT8,
  	PRIMARY KEY (tenantid, id)
);
CREATE UNIQUE INDEX idx_UQ_pending_mapping ON pending_mapping (tenantid, activityId, userId, actorId);

CREATE TABLE flownode_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  flownodeDefinitionId INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  rootContainerId INT8 NOT NULL,
  parentContainerId INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  displayName VARCHAR(255),
  displayDescription VARCHAR(255),
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  prev_state_id INT NOT NULL,
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  actorId INT8 NULL,
  assigneeId INT8 DEFAULT 0 NOT NULL,
  reachedStateDate INT8,
  lastUpdateDate INT8,
  expectedEndDate INT8,
  claimedDate INT8,
  priority SMALLINT,
  gatewayType VARCHAR(50),
  hitBys VARCHAR(255),
  stateCategory VARCHAR(50) NOT NULL,
  logicalGroup1 INT8 NOT NULL,
  logicalGroup2 INT8 NOT NULL,
  logicalGroup3 INT8,
  logicalGroup4 INT8 NOT NULL,
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
  executedBy INT8,
  executedBySubstitute INT8,
  activityInstanceId INT8,
  state_executing BOOLEAN DEFAULT FALSE,
  abortedByBoundary INT8,
  triggeredByEvent BOOLEAN,
  interrupting BOOLEAN,
  tokenCount INT NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId);
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (logicalGroup4);
CREATE INDEX idx_fni_loggroup3_terminal ON flownode_instance(logicalgroup3, terminal, tenantid);
CREATE INDEX idx_fn_lg2_state_tenant_del ON flownode_instance (logicalGroup2, stateName, tenantid);
CREATE INDEX idx_fni_activity_instance_id_kind ON flownode_instance(activityInstanceId, kind, tenantid);

CREATE TABLE ref_biz_data_inst (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	name VARCHAR(255) NOT NULL,
  	proc_inst_id INT8,
  	fn_inst_id INT8,
  	data_id INT8,
  	data_classname VARCHAR(255) NOT NULL
);
CREATE INDEX idx_biz_data_inst1 ON ref_biz_data_inst (tenantid, proc_inst_id);
CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (tenantid, fn_inst_id);
CREATE INDEX idx_biz_data_inst3 ON ref_biz_data_inst (proc_inst_id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_proc FOREIGN KEY (proc_inst_id) REFERENCES process_instance(id) ON DELETE CASCADE;
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE;

CREATE TABLE arch_ref_biz_data_inst (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	name VARCHAR(255) NOT NULL,
  	orig_proc_inst_id BIGINT,
  	orig_fn_inst_id BIGINT,
  	data_id BIGINT,
  	data_classname VARCHAR(255) NOT NULL
);
CREATE INDEX idx_arch_biz_data_inst1 ON arch_ref_biz_data_inst (tenantid, orig_proc_inst_id);
CREATE INDEX idx_arch_biz_data_inst2 ON arch_ref_biz_data_inst (tenantid, orig_fn_inst_id);
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT pk_arch_ref_biz_data_inst PRIMARY KEY (tenantid, id);

CREATE TABLE arch_process_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(75) NOT NULL,
  processDefinitionId INT8 NOT NULL,
  description VARCHAR(255),
  startDate INT8 NOT NULL,
  startedBy INT8 NOT NULL,
  startedBySubstitute INT8 NOT NULL,
  endDate INT8 NOT NULL,
  archiveDate INT8 NOT NULL,
  stateId INT NOT NULL,
  lastUpdate INT8 NOT NULL,
  rootProcessInstanceId INT8,
  callerId INT8,
  sourceObjectId INT8 NOT NULL,
  stringIndex1 VARCHAR(255),
  stringIndex2 VARCHAR(255),
  stringIndex3 VARCHAR(255),
  stringIndex4 VARCHAR(255),
  stringIndex5 VARCHAR(255),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, rootProcessInstanceId, callerId);
CREATE INDEX idx2_arch_process_instance ON arch_process_instance (tenantId, processDefinitionId, archiveDate);
CREATE INDEX idx3_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, callerId, stateId);

CREATE TABLE arch_flownode_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  flownodeDefinitionId INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  sourceObjectId INT8,
  archiveDate INT8 NOT NULL,
  rootContainerId INT8 NOT NULL,
  parentContainerId INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  displayName VARCHAR(255),
  displayDescription VARCHAR(255),
  stateId INT NOT NULL,
  stateName VARCHAR(50),
  terminal BOOLEAN NOT NULL,
  stable BOOLEAN ,
  actorId INT8 NULL,
  assigneeId INT8 DEFAULT 0 NOT NULL,
  reachedStateDate INT8,
  lastUpdateDate INT8,
  expectedEndDate INT8,
  claimedDate INT8,
  priority SMALLINT,
  gatewayType VARCHAR(50),
  hitBys VARCHAR(255),
  logicalGroup1 INT8 NOT NULL,
  logicalGroup2 INT8 NOT NULL,
  logicalGroup3 INT8,
  logicalGroup4 INT8 NOT NULL,
  loop_counter INT,
  loop_max INT,
  loopCardinality INT,
  loopDataInputRef VARCHAR(255),
  loopDataOutputRef VARCHAR(255),
  description VARCHAR(255),
  sequential BOOLEAN,
  dataInputItemRef VARCHAR(255),
  dataOutputItemRef VARCHAR(255),
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy INT8,
  executedBySubstitute INT8,
  activityInstanceId INT8,
  aborting BOOLEAN NOT NULL,
  triggeredByEvent BOOLEAN,
  interrupting BOOLEAN,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(logicalGroup2, tenantId, kind, executedBy);
CREATE INDEX idx_afi_kind_lg3 ON arch_flownode_instance(tenantId, kind, logicalGroup3);
CREATE INDEX idx_afi_kind_lg4 ON arch_flownode_instance(tenantId, logicalGroup4);
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind);
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (tenantId, rootContainerId, parentContainerId);
CREATE INDEX idx_lg4_lg2 on arch_flownode_instance(tenantid, logicalGroup4, logicalGroup2);

CREATE TABLE arch_connector_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  containerId INT8 NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  connectorId VARCHAR(255) NOT NULL,
  version VARCHAR(50) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  sourceObjectId INT8,
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType);

CREATE TABLE connector_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  containerId INT8 NOT NULL,
  containerType VARCHAR(10) NOT NULL,
  connectorId VARCHAR(255) NOT NULL,
  version VARCHAR(50) NOT NULL,
  name VARCHAR(255) NOT NULL,
  activationEvent VARCHAR(30),
  state VARCHAR(50),
  executionOrder INT,
  exceptionMessage VARCHAR(255),
  stackTrace TEXT,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_ci_container_activation ON connector_instance (tenantid, containerId, containerType, activationEvent);

CREATE TABLE arch_document_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  sourceObjectId INT8,
  processinstanceid INT8 NOT NULL,
  documentid INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  version VARCHAR(50) NOT NULL,
  index_ INT NOT NULL,
  archiveDate INT8 NOT NULL,
  PRIMARY KEY (tenantid, ID)
);
CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid, tenantid);

CREATE TABLE arch_process_comment (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  userId INT8,
  processInstanceId INT8 NOT NULL,
  postDate INT8 NOT NULL,
  content VARCHAR(512) NOT NULL,
  archiveDate INT8 NOT NULL,
  sourceObjectId INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_process_comment on arch_process_comment (sourceobjectid, tenantid);
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate, tenantid);

CREATE TABLE process_comment (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  kind VARCHAR(25) NOT NULL,
  userId INT8,
  processInstanceId INT8 NOT NULL,
  postDate INT8 NOT NULL,
  content VARCHAR(512) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_process_comment on process_comment (processInstanceId, tenantid);

CREATE TABLE contract_data (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  kind VARCHAR(20) NOT NULL,
  scopeId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  val TEXT
);
ALTER TABLE contract_data ADD CONSTRAINT pk_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE contract_data ADD CONSTRAINT uc_cd_scope_name UNIQUE (kind, scopeId, name, tenantid);
CREATE INDEX idx_cd_scope_name ON contract_data (kind, scopeId, name, tenantid);

CREATE TABLE arch_contract_data (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  kind VARCHAR(20) NOT NULL,
  scopeId INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  val TEXT,
  archiveDate INT8 NOT NULL,
  sourceObjectId INT8 NOT NULL
);
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid);
CREATE INDEX idx_acd_scope_name ON arch_contract_data (kind, scopeId, name, tenantid);
