CREATE TABLE process_instance (
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75 CHAR) NOT NULL,
  processDefinitionId NUMBER(19, 0) NOT NULL,
  description VARCHAR2(255 CHAR),
  startDate NUMBER(19, 0) NOT NULL,
  startedBy NUMBER(19, 0) NOT NULL,
  startedBySubstitute NUMBER(19, 0) NOT NULL,
  endDate NUMBER(19, 0) NOT NULL,
  stateId INT NOT NULL,
  stateCategory VARCHAR2(50 CHAR) NOT NULL,
  lastUpdate NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0),
  rootProcessInstanceId NUMBER(19, 0),
  callerId NUMBER(19, 0),
  callerType VARCHAR2(50 CHAR),
  interruptingEventId NUMBER(19, 0),
  stringIndex1 VARCHAR2(255 CHAR),
  stringIndex2 VARCHAR2(255 CHAR),
  stringIndex3 VARCHAR2(255 CHAR),
  stringIndex4 VARCHAR2(255 CHAR),
  stringIndex5 VARCHAR2(255 CHAR),
  PRIMARY KEY (id)
);
CREATE INDEX idx1_proc_inst_pdef_state ON process_instance (processdefinitionid, stateid);

CREATE TABLE contract_data (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(20 CHAR) NOT NULL,
  scopeId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  val CLOB
);
ALTER TABLE contract_data ADD CONSTRAINT pk_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE contract_data ADD CONSTRAINT uc_cd_scope_name UNIQUE (kind, scopeId, name, tenantid);

CREATE TABLE arch_contract_data (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(20 CHAR) NOT NULL,
  scopeId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  val CLOB,
  archiveDate NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL
);
ALTER TABLE arch_contract_data ADD CONSTRAINT pk_arch_contract_data PRIMARY KEY (tenantid, id, scopeId);
ALTER TABLE arch_contract_data ADD CONSTRAINT uc_acd_scope_name UNIQUE (kind, scopeId, name, tenantid);

CREATE TABLE arch_process_comment (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0),
  processInstanceId NUMBER(19, 0) NOT NULL,
  postDate NUMBER(19, 0) NOT NULL,
  content VARCHAR2(512 CHAR) NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_process_comment on arch_process_comment (sourceobjectid, tenantid);
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate, tenantid);

CREATE TABLE process_comment (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25 CHAR) NOT NULL,
  userId NUMBER(19, 0),
  processInstanceId NUMBER(19, 0) NOT NULL,
  postDate NUMBER(19, 0) NOT NULL,
  content VARCHAR2(512 CHAR) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_process_comment on process_comment (processInstanceId, tenantid);

CREATE TABLE arch_document_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0),
  processinstanceid NUMBER(19, 0) NOT NULL,
  documentid NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  version VARCHAR2(50 CHAR) NOT NULL,
  index_ INT NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_a_doc_mp_pr_id ON arch_document_mapping (processinstanceid, tenantid);

CREATE TABLE arch_process_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75 CHAR) NOT NULL,
  processDefinitionId NUMBER(19, 0) NOT NULL,
  description VARCHAR2(255 CHAR),
  startDate NUMBER(19, 0) NOT NULL,
  startedBy NUMBER(19, 0) NOT NULL,
  startedBySubstitute NUMBER(19, 0) NOT NULL,
  endDate NUMBER(19, 0) NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  stateId INT NOT NULL,
  lastUpdate NUMBER(19, 0) NOT NULL,
  rootProcessInstanceId NUMBER(19, 0),
  callerId NUMBER(19, 0),
  sourceObjectId NUMBER(19, 0) NOT NULL,
  stringIndex1 VARCHAR2(255 CHAR),
  stringIndex2 VARCHAR2(255 CHAR),
  stringIndex3 VARCHAR2(255 CHAR),
  stringIndex4 VARCHAR2(255 CHAR),
  stringIndex5 VARCHAR2(255 CHAR),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, rootProcessInstanceId, callerId);
CREATE INDEX idx2_arch_process_instance ON arch_process_instance (tenantId, processDefinitionId, archiveDate);
CREATE INDEX idx3_arch_process_instance ON arch_process_instance (tenantId, sourceObjectId, callerId, stateId);

CREATE TABLE arch_flownode_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  flownodeDefinitionId NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25 CHAR) NOT NULL,
  sourceObjectId NUMBER(19, 0),
  archiveDate NUMBER(19, 0) NOT NULL,
  rootContainerId NUMBER(19, 0) NOT NULL,
  parentContainerId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  displayName VARCHAR2(255 CHAR),
  displayDescription VARCHAR2(255 CHAR),
  stateId INT NOT NULL,
  stateName VARCHAR2(50 CHAR),
  terminal NUMBER(1) NOT NULL,
  stable NUMBER(1) ,
  actorId NUMBER(19, 0) NULL,
  assigneeId NUMBER(19, 0) DEFAULT 0 NOT NULL,
  reachedStateDate NUMBER(19, 0),
  lastUpdateDate NUMBER(19, 0),
  expectedEndDate NUMBER(19, 0),
  claimedDate NUMBER(19, 0),
  priority SMALLINT,
  gatewayType VARCHAR2(50 CHAR),
  hitBys VARCHAR2(255 CHAR),
  logicalGroup1 NUMBER(19, 0) NOT NULL,
  logicalGroup2 NUMBER(19, 0) NOT NULL,
  logicalGroup3 NUMBER(19, 0),
  logicalGroup4 NUMBER(19, 0) NOT NULL,
  loop_counter INT,
  loop_max INT,
  loopCardinality INT,
  loopDataInputRef VARCHAR2(255 CHAR),
  loopDataOutputRef VARCHAR2(255 CHAR),
  description VARCHAR2(255 CHAR),
  sequential NUMBER(1),
  dataInputItemRef VARCHAR2(255 CHAR),
  dataOutputItemRef VARCHAR2(255 CHAR),
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy NUMBER(19, 0),
  executedBySubstitute NUMBER(19, 0),
  activityInstanceId NUMBER(19, 0),
  aborting NUMBER(1) NOT NULL,
  triggeredByEvent NUMBER(1),
  interrupting NUMBER(1),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(logicalGroup2, tenantId, kind, executedBy);
CREATE INDEX idx_afi_kind_lg3 ON arch_flownode_instance(tenantId, kind, logicalGroup3);
CREATE INDEX idx_afi_kind_lg4 ON arch_flownode_instance(tenantId, logicalGroup4);
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind);
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (tenantId, rootContainerId, parentContainerId);
CREATE INDEX idx_lg4_lg2 on arch_flownode_instance(tenantid, logicalGroup4, logicalGroup2);

CREATE TABLE arch_connector_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0) NOT NULL,
  containerType VARCHAR2(10 CHAR) NOT NULL,
  connectorId VARCHAR2(255 CHAR) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  activationEvent VARCHAR2(30 CHAR),
  state VARCHAR2(50 CHAR),
  sourceObjectId NUMBER(19, 0),
  archiveDate NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType);

CREATE TABLE flownode_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  flownodeDefinitionId NUMBER(19, 0) NOT NULL,
  kind VARCHAR2(25 CHAR) NOT NULL,
  rootContainerId NUMBER(19, 0) NOT NULL,
  parentContainerId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  displayName VARCHAR2(255 CHAR),
  displayDescription VARCHAR2(255 CHAR),
  stateId INT NOT NULL,
  stateName VARCHAR2(50 CHAR),
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
  gatewayType VARCHAR2(50 CHAR),
  hitBys VARCHAR2(255 CHAR),
  stateCategory VARCHAR2(50 CHAR) NOT NULL,
  logicalGroup1 NUMBER(19, 0) NOT NULL,
  logicalGroup2 NUMBER(19, 0) NOT NULL,
  logicalGroup3 NUMBER(19, 0),
  logicalGroup4 NUMBER(19, 0) NOT NULL,
  loop_counter INT,
  loop_max INT,
  description VARCHAR2(255 CHAR),
  sequential NUMBER(1),
  loopDataInputRef VARCHAR2(255 CHAR),
  loopDataOutputRef VARCHAR2(255 CHAR),
  dataInputItemRef VARCHAR2(255 CHAR),
  dataOutputItemRef VARCHAR2(255 CHAR),
  loopCardinality INT,
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy NUMBER(19, 0),
  executedBySubstitute NUMBER(19, 0),
  activityInstanceId NUMBER(19, 0),
  state_executing NUMBER(1) DEFAULT 0,
  abortedByBoundary NUMBER(19, 0),
  triggeredByEvent NUMBER(1),
  interrupting NUMBER(1),
  tokenCount INT NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId);
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (logicalGroup4);
CREATE INDEX idx_fni_loggroup3_terminal ON flownode_instance(logicalgroup3, terminal, tenantid);
CREATE INDEX idx_fn_lg2_state_tenant_del ON flownode_instance (logicalGroup2, stateName, tenantid);
CREATE INDEX idx_fni_activity_instance_id_kind ON flownode_instance(activityInstanceId, kind, tenantid);

CREATE TABLE connector_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0) NOT NULL,
  containerType VARCHAR2(10 CHAR) NOT NULL,
  connectorId VARCHAR2(255 CHAR) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  activationEvent VARCHAR2(30 CHAR),
  state VARCHAR2(50 CHAR),
  executionOrder INT,
  exceptionMessage VARCHAR2(255 CHAR),
  stackTrace CLOB,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_ci_container_activation ON connector_instance (tenantid, containerId, containerType, activationEvent);

CREATE TABLE waiting_event (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	kind VARCHAR2(15 CHAR) NOT NULL,
  	eventType VARCHAR2(50 CHAR),
  	messageName VARCHAR2(255 CHAR),
  	signalName VARCHAR2(255 CHAR),
  	errorCode VARCHAR2(255 CHAR),
  	processName VARCHAR2(150 CHAR),
  	flowNodeName VARCHAR2(50 CHAR),
  	flowNodeDefinitionId NUMBER(19, 0),
  	subProcessId NUMBER(19, 0),
  	processDefinitionId NUMBER(19, 0),
  	rootProcessInstanceId NUMBER(19, 0),
  	parentProcessInstanceId NUMBER(19, 0),
  	flowNodeInstanceId NUMBER(19, 0),
  	relatedActivityInstanceId NUMBER(19, 0),
  	locked NUMBER(1),
  	active NUMBER(1),
  	progress SMALLINT,
  	correlation1 VARCHAR2(128 CHAR),
  	correlation2 VARCHAR2(128 CHAR),
  	correlation3 VARCHAR2(128 CHAR),
  	correlation4 VARCHAR2(128 CHAR),
  	correlation5 VARCHAR2(128 CHAR),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_waiting_event ON waiting_event (progress, tenantid, kind, locked, active);
CREATE INDEX idx_waiting_event_correl ON waiting_event (correlation1, correlation2, correlation3, correlation4, correlation5);

CREATE TABLE pending_mapping (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	activityId NUMBER(19, 0) NOT NULL,
  	actorId NUMBER(19, 0),
  	userId NUMBER(19, 0),
  	PRIMARY KEY (tenantid, id)
);
CREATE UNIQUE INDEX idx_UQ_pending_mapping ON pending_mapping (tenantid, activityId, userId, actorId);

CREATE TABLE ref_biz_data_inst (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	kind VARCHAR2(15 CHAR) NOT NULL,
  	name VARCHAR2(255 CHAR) NOT NULL,
  	proc_inst_id NUMBER(19, 0),
  	fn_inst_id NUMBER(19, 0),
  	data_id NUMBER(19, 0),
  	data_classname VARCHAR2(255 CHAR) NOT NULL
);

CREATE INDEX idx_biz_data_inst1 ON ref_biz_data_inst (tenantid, proc_inst_id);
CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (tenantid, fn_inst_id);
CREATE INDEX idx_biz_data_inst3 ON ref_biz_data_inst (proc_inst_id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_proc FOREIGN KEY (proc_inst_id) REFERENCES process_instance(id) ON DELETE CASCADE;
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE;

CREATE TABLE arch_ref_biz_data_inst (
    tenantid NUMBER(19, 0) NOT NULL,
    id NUMBER(19, 0) NOT NULL,
    kind VARCHAR2(15 CHAR) NOT NULL,
    name VARCHAR2(255 CHAR) NOT NULL,
    orig_proc_inst_id NUMBER(19, 0),
    orig_fn_inst_id NUMBER(19, 0),
    data_id NUMBER(19, 0),
    data_classname VARCHAR2(255 CHAR) NOT NULL
);
CREATE INDEX idx_arch_biz_data_inst1 ON arch_ref_biz_data_inst (tenantid, orig_proc_inst_id);
CREATE INDEX idx_arch_biz_data_inst2 ON arch_ref_biz_data_inst (tenantid, orig_fn_inst_id);
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT pk_arch_ref_biz_data_inst PRIMARY KEY (tenantid, id);

CREATE TABLE business_app (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  token VARCHAR2(50 CHAR) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  iconPath VARCHAR2(255 CHAR),
  creationDate NUMBER(19, 0) NOT NULL,
  createdBy NUMBER(19, 0) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  updatedBy NUMBER(19, 0) NOT NULL,
  state VARCHAR2(30 CHAR) NOT NULL,
  homePageId NUMBER(19, 0),
  profileId NUMBER(19, 0),
  layoutId NUMBER(19, 0),
  themeId NUMBER(19, 0),
  iconMimeType VARCHAR2(255 CHAR),
  iconContent BLOB,
  displayName VARCHAR2(255 CHAR) NOT NULL,
  editable NUMBER(1),
  internalProfile VARCHAR2(255 CHAR)
);
ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT UK_Business_app UNIQUE (tenantId, token, version);
CREATE INDEX idx_app_token ON business_app (token, tenantid);
CREATE INDEX idx_app_profile ON business_app (profileId, tenantid);
CREATE INDEX idx_app_homepage ON business_app (homePageId, tenantid);

CREATE TABLE business_app_page (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  applicationId NUMBER(19, 0) NOT NULL,
  pageId NUMBER(19, 0) NOT NULL,
  token VARCHAR2(255 CHAR) NOT NULL
);
ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT UK_Business_app_page UNIQUE (tenantId, applicationId, token);
CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token, tenantid);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId, tenantid);

CREATE TABLE business_app_menu (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  displayName VARCHAR2(255 CHAR) NOT NULL,
  applicationId NUMBER(19, 0) NOT NULL,
  applicationPageId NUMBER(19, 0),
  parentId NUMBER(19, 0),
  index_ NUMBER(19, 0)
);
ALTER TABLE business_app_menu ADD CONSTRAINT pk_business_app_menu PRIMARY KEY (tenantid, id);
CREATE INDEX idx_app_menu_app ON business_app_menu (applicationId, tenantid);
CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId, tenantid);
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId, tenantid);

CREATE TABLE arch_data_instance (
    tenantId NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	name VARCHAR2(50 CHAR),
	description VARCHAR2(50 CHAR),
	transientData NUMBER(1),
	className VARCHAR2(100 CHAR),
	containerId NUMBER(19, 0),
	containerType VARCHAR2(60 CHAR),
	namespace VARCHAR2(100 CHAR),
	element VARCHAR2(60 CHAR),
	intValue INT,
	longValue NUMBER(19, 0),
	shortTextValue VARCHAR2(255 CHAR),
	booleanValue NUMBER(1),
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BLOB,
	clobValue CLOB,
	discriminant VARCHAR2(50 CHAR) NOT NULL,
	archiveDate NUMBER(19, 0) NOT NULL,
	sourceObjectId NUMBER(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId, containerId, containerType, archiveDate, name, sourceObjectId);
CREATE INDEX idx2_arch_data_instance ON arch_data_instance (sourceObjectId, containerId, archiveDate, id, tenantId);

CREATE TABLE data_instance (
    tenantId NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	name VARCHAR2(50 CHAR),
	description VARCHAR2(50 CHAR),
	transientData NUMBER(1),
	className VARCHAR2(100 CHAR),
	containerId NUMBER(19, 0),
	containerType VARCHAR2(60 CHAR),
	namespace VARCHAR2(100 CHAR),
	element VARCHAR2(60 CHAR),
	intValue INT,
	longValue NUMBER(19, 0),
	shortTextValue VARCHAR2(255 CHAR),
	booleanValue NUMBER(1),
	doubleValue NUMERIC(19,5),
	floatValue REAL,
	blobValue BLOB,
	clobValue CLOB,
	discriminant VARCHAR2(50 CHAR) NOT NULL,
	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_datai_container ON data_instance (tenantId, containerId, containerType, name);

CREATE TABLE group_ (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(125 CHAR) NOT NULL,
  parentPath VARCHAR2(255 CHAR),
  displayName VARCHAR2(255 CHAR),
  description VARCHAR2(1024 CHAR),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  iconid NUMBER(19, 0),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_group_name ON group_ (tenantid, parentPath, name);

CREATE TABLE role (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  displayName VARCHAR2(255 CHAR),
  description VARCHAR2(1024 CHAR),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  iconid NUMBER(19, 0),
  CONSTRAINT UK_Role UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
);


CREATE TABLE user_ (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  enabled NUMBER(1) NOT NULL,
  userName VARCHAR2(255 CHAR) NOT NULL,
  password VARCHAR2(60 CHAR),
  firstName VARCHAR2(255 CHAR),
  lastName VARCHAR2(255 CHAR),
  title VARCHAR2(50 CHAR),
  jobTitle VARCHAR2(255 CHAR),
  managerUserId NUMBER(19, 0),
  createdBy NUMBER(19, 0),
  creationDate NUMBER(19, 0),
  lastUpdate NUMBER(19, 0),
  iconid NUMBER(19, 0),
  CONSTRAINT UK_User UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE user_contactinfo (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  email VARCHAR2(255 CHAR),
  phone VARCHAR2(50 CHAR),
  mobile VARCHAR2(50 CHAR),
  fax VARCHAR2(50 CHAR),
  building VARCHAR2(50 CHAR),
  room VARCHAR2(50 CHAR),
  address VARCHAR2(255 CHAR),
  zipCode VARCHAR2(50 CHAR),
  city VARCHAR2(255 CHAR),
  state VARCHAR2(255 CHAR),
  country VARCHAR2(255 CHAR),
  website VARCHAR2(255 CHAR),
  personal NUMBER(1) NOT NULL,
  UNIQUE (tenantid, userId, personal),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_contact_user FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;

CREATE TABLE custom_usr_inf_def (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  CONSTRAINT UK_Custom_Usr_Inf_Def UNIQUE (tenantId, name),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE job_desc (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  jobclassname VARCHAR2(100 CHAR) NOT NULL,
  jobname VARCHAR2(100 CHAR) NOT NULL,
  description VARCHAR2(50 CHAR),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE job_param (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  jobDescriptorId NUMBER(19, 0) NOT NULL,
  key_ VARCHAR2(50 CHAR) NOT NULL,
  value_ BLOB NOT NULL,
  PRIMARY KEY (tenantId, id)
);
CREATE INDEX idx_job_param_tenant_jobid ON job_param (tenantid, jobDescriptorId);

CREATE TABLE job_log (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  jobDescriptorId NUMBER(19, 0) NOT NULL,
  retryNumber NUMBER(19, 0),
  lastUpdateDate NUMBER(19, 0),
  lastMessage CLOB,
  UNIQUE (tenantId, jobDescriptorId),
  PRIMARY KEY (tenantId, id)
);
ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;
ALTER TABLE job_log ADD CONSTRAINT fk_job_log_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;

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

CREATE TABLE tenant_resource (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255) NOT NULL,
  type VARCHAR2(16) NOT NULL,
  content BLOB NOT NULL,
  lastUpdatedBy NUMBER(19,0) NOT NULL,
  lastUpdateDate NUMBER(19,0),
  state VARCHAR2(50) NOT NULL,
  CONSTRAINT UK_tenant_resource UNIQUE (tenantId, name, type),
  PRIMARY KEY (tenantId, id)
);
CREATE INDEX idx_tenant_resource ON tenant_resource (tenantId, type, name);
