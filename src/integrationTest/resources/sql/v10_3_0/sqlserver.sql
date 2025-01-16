CREATE TABLE sequence (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  nextid NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE tenant (
  id NUMERIC(19, 0) NOT NULL,
  created NUMERIC(19, 0) NOT NULL,
  createdBy NVARCHAR(50) NOT NULL,
  description NVARCHAR(255),
  defaultTenant BIT NOT NULL,
  iconname NVARCHAR(50),
  iconpath NVARCHAR(255),
  name NVARCHAR(50) NOT NULL,
  status NVARCHAR(15) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE flownode_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  flownodeDefinitionId NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(25) NOT NULL,
  rootContainerId NUMERIC(19, 0) NOT NULL,
  parentContainerId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  displayName NVARCHAR(255),
  displayDescription NVARCHAR(255),
  stateId INT NOT NULL,
  stateName NVARCHAR(50),
  prev_state_id INT NOT NULL,
  terminal BIT NOT NULL,
  stable BIT ,
  actorId NUMERIC(19, 0) NULL,
  assigneeId NUMERIC(19, 0) DEFAULT 0 NOT NULL,
  reachedStateDate NUMERIC(19, 0),
  lastUpdateDate NUMERIC(19, 0),
  expectedEndDate NUMERIC(19, 0),
  claimedDate NUMERIC(19, 0),
  priority TINYINT,
  gatewayType NVARCHAR(50),
  hitBys NVARCHAR(255),
  stateCategory NVARCHAR(50) NOT NULL,
  logicalGroup1 NUMERIC(19, 0) NOT NULL,
  logicalGroup2 NUMERIC(19, 0) NOT NULL,
  logicalGroup3 NUMERIC(19, 0),
  logicalGroup4 NUMERIC(19, 0) NOT NULL,
  loop_counter INT,
  loop_max INT,
  description NVARCHAR(255),
  sequential BIT,
  loopDataInputRef NVARCHAR(255),
  loopDataOutputRef NVARCHAR(255),
  dataInputItemRef NVARCHAR(255),
  dataOutputItemRef NVARCHAR(255),
  loopCardinality INT,
  nbActiveInst INT,
  nbCompletedInst INT,
  nbTerminatedInst INT,
  executedBy NUMERIC(19, 0),
  executedBySubstitute NUMERIC(19, 0),
  activityInstanceId NUMERIC(19, 0),
  state_executing BIT DEFAULT 0,
  abortedByBoundary NUMERIC(19, 0),
  triggeredByEvent BIT,
  interrupting BIT,
  tokenCount INT NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_fni_rootcontid ON flownode_instance (rootContainerId);
CREATE INDEX idx_fni_loggroup4 ON flownode_instance (logicalGroup4);
CREATE INDEX idx_fni_loggroup3_terminal ON flownode_instance(logicalgroup3, terminal);
CREATE INDEX idx_fn_lg2_state ON flownode_instance (logicalGroup2, stateName);
CREATE INDEX idx_fni_activity_instance_id_kind ON flownode_instance(activityInstanceId, kind);
ALTER TABLE flownode_instance ADD CONSTRAINT fk_flownode_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE pending_mapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  activityId NUMERIC(19, 0) NOT NULL,
  actorId NUMERIC(19, 0),
  userId NUMERIC(19, 0),
  PRIMARY KEY (tenantid, id)
);
CREATE UNIQUE INDEX idx_UQ_pending_mapping ON pending_mapping (activityId, userId, actorId);
ALTER TABLE pending_mapping ADD CONSTRAINT fk_pending_mapping_flownode_instanceId FOREIGN KEY (tenantid, activityId) REFERENCES flownode_instance(tenantid, id);

CREATE TABLE ref_biz_data_inst (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(15) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  proc_inst_id NUMERIC(19, 0),
  fn_inst_id NUMERIC(19, 0),
  data_id NUMERIC(19, 0),
  data_classname NVARCHAR(255) NOT NULL
);

CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (fn_inst_id);
CREATE INDEX idx_biz_data_inst3 ON ref_biz_data_inst (proc_inst_id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data PRIMARY KEY (tenantid, id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE;

CREATE TABLE page_mapping (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  key_ NVARCHAR(255) NOT NULL,
  pageId NUMERIC(19, 0) NULL,
  url NVARCHAR(1024) NULL,
  urladapter NVARCHAR(255) NULL,
  page_authoriz_rules NVARCHAR(MAX) NULL,
  lastUpdateDate NUMERIC(19, 0) NULL,
  lastUpdatedBy NUMERIC(19, 0) NULL,
  CONSTRAINT UK_page_mapping UNIQUE (tenantId, key_),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE form_mapping (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  process NUMERIC(19, 0) NOT NULL,
  type INT NOT NULL,
  task NVARCHAR(255),
  page_mapping_tenant_id NUMERIC(19, 0),
  page_mapping_id NUMERIC(19, 0),
  lastUpdateDate NUMERIC(19, 0),
  lastUpdatedBy NUMERIC(19, 0),
  target NVARCHAR(16) NOT NULL,
  PRIMARY KEY (tenantId, id)
);
ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id);

CREATE TABLE page (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  displayName NVARCHAR(255) NOT NULL,
  description NVARCHAR(MAX),
  installationDate NUMERIC(19, 0) NOT NULL,
  installedBy NUMERIC(19, 0) NOT NULL,
  provided BIT,
  editable BIT,
  removable BIT,
  lastModificationDate NUMERIC(19, 0) NOT NULL,
  lastUpdatedBy NUMERIC(19, 0) NOT NULL,
  contentName NVARCHAR(280) NOT NULL,
  content VARBINARY(MAX),
  contentType NVARCHAR(50) NOT NULL,
  processDefinitionId NUMERIC(19,0) NOT NULL,
  pageHash NVARCHAR(32)
);
ALTER TABLE page ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id);
ALTER TABLE page ADD CONSTRAINT uk_page UNIQUE (tenantId, name, processDefinitionId);

CREATE TABLE profile (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  isDefault BIT NOT NULL,
  name NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  creationDate NUMERIC(19, 0) NOT NULL,
  createdBy NUMERIC(19, 0) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  lastUpdatedBy NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);
ALTER TABLE profile ADD CONSTRAINT fk_profile_tenantId FOREIGN KEY (tenantId) REFERENCES tenant(id);

CREATE TABLE profilemember (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  profileId NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  groupId NUMERIC(19, 0) NOT NULL,
  roleId NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantId, profileId, userId, groupId, roleId),
  PRIMARY KEY (tenantId, id)
);
ALTER TABLE profilemember ADD CONSTRAINT fk_profilemember_tenantId FOREIGN KEY (tenantId) REFERENCES tenant(id);
ALTER TABLE profilemember ADD CONSTRAINT fk_profilemember_profileId FOREIGN KEY (tenantId, profileId) REFERENCES profile(tenantId, id);

CREATE TABLE business_app (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  token NVARCHAR(50) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  description NVARCHAR(MAX),
  iconPath NVARCHAR(255),
  creationDate NUMERIC(19, 0) NOT NULL,
  createdBy NUMERIC(19, 0) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  updatedBy NUMERIC(19, 0) NOT NULL,
  state NVARCHAR(30) NOT NULL,
  homePageId NUMERIC(19, 0),
  profileId NUMERIC(19, 0),
  layoutId NUMERIC(19, 0),
  themeId NUMERIC(19, 0),
  iconMimeType NVARCHAR(255),
  iconContent VARBINARY(MAX),
  displayName NVARCHAR(255) NOT NULL,
  editable BIT,
  internalProfile NVARCHAR(255),
  isLink BIT DEFAULT 0
);
ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT uk_app_token_version UNIQUE (tenantId, token, version);
CREATE INDEX idx_app_token ON business_app (token);
CREATE INDEX idx_app_profile ON business_app (profileId);
CREATE INDEX idx_app_homepage ON business_app (homePageId);
ALTER TABLE business_app ADD CONSTRAINT fk_app_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE business_app ADD CONSTRAINT fk_app_profileId FOREIGN KEY (tenantid, profileId) REFERENCES profile (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT fk_app_layoutId FOREIGN KEY (tenantid, layoutId) REFERENCES page (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT fk_app_themeId FOREIGN KEY (tenantid, themeId) REFERENCES page (tenantid, id);

CREATE TABLE business_app_page (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  applicationId NUMERIC(19, 0) NOT NULL,
  pageId NUMERIC(19, 0) NOT NULL,
  token NVARCHAR(255) NOT NULL
);
ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_token UNIQUE (tenantId, applicationId, token);
CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId);
ALTER TABLE business_app_page ADD CONSTRAINT fk_app_page_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE business_app_page ADD CONSTRAINT fk_bus_app_id FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id) ON DELETE CASCADE;
ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page (tenantid, id);

CREATE TABLE business_app_menu (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  displayName NVARCHAR(255) NOT NULL,
  applicationId NUMERIC(19, 0) NOT NULL,
  applicationPageId NUMERIC(19, 0),
  parentId NUMERIC(19, 0),
  index_ NUMERIC(19, 0)
);
ALTER TABLE business_app_menu ADD CONSTRAINT pk_business_app_menu PRIMARY KEY (tenantid, id);
CREATE INDEX idx_app_menu_app ON business_app_menu (applicationId);
CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId);
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_appId FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_pageId FOREIGN KEY (tenantid, applicationPageId) REFERENCES business_app_page (tenantid, id);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_parentId FOREIGN KEY (tenantid, parentId) REFERENCES business_app_menu (tenantid, id);

CREATE TABLE job_desc (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  jobclassname NVARCHAR(100) NOT NULL,
  jobname NVARCHAR(100) NOT NULL,
  description NVARCHAR(50),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_job_desc_id ON job_desc(id);
ALTER TABLE job_desc ADD CONSTRAINT fk_job_desc_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE job_param (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  jobDescriptorId NUMERIC(19, 0) NOT NULL,
  key_ NVARCHAR(50) NOT NULL,
  value_ VARBINARY(MAX) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;
CREATE INDEX idx_job_param_jobid ON job_param (jobDescriptorId);
ALTER TABLE job_param ADD CONSTRAINT fk_job_param_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE job_log (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  jobDescriptorId NUMERIC(19, 0) NOT NULL,
  retryNumber NUMERIC(19, 0),
  lastUpdateDate NUMERIC(19, 0),
  lastMessage NVARCHAR(MAX),
  UNIQUE (tenantId, jobDescriptorId),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE job_log ADD CONSTRAINT fk_job_log_jobid FOREIGN KEY (tenantid, jobDescriptorId) REFERENCES job_desc(tenantid, id) ON DELETE CASCADE;
CREATE INDEX idx_job_log_jobdescid ON job_log(jobdescriptorid);

CREATE TABLE group_ (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(125) NOT NULL,
  parentPath NVARCHAR(255),
  displayName NVARCHAR(255),
  description NVARCHAR(MAX),
  createdBy NUMERIC(19, 0),
  creationDate NUMERIC(19, 0),
  lastUpdate NUMERIC(19, 0),
  iconid NUMERIC(19, 0),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_group_name ON group_ (parentPath, name);
ALTER TABLE group_ ADD CONSTRAINT fk_group__tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE role (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  displayName NVARCHAR(255),
  description NVARCHAR(MAX),
  createdBy NUMERIC(19, 0),
  creationDate NUMERIC(19, 0),
  lastUpdate NUMERIC(19, 0),
  iconid NUMERIC(19, 0),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_role_name ON role (name);
ALTER TABLE role ADD CONSTRAINT fk_role_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE user_ (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  enabled BIT NOT NULL,
  userName NVARCHAR(255) NOT NULL,
  password NVARCHAR(60),
  firstName NVARCHAR(255),
  lastName NVARCHAR(255),
  title NVARCHAR(50),
  jobTitle NVARCHAR(255),
  managerUserId NUMERIC(19, 0),
  createdBy NUMERIC(19, 0),
  creationDate NUMERIC(19, 0),
  lastUpdate NUMERIC(19, 0),
  iconid NUMERIC(19, 0),
  UNIQUE (tenantid, userName),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_user_name ON user_ (userName);
ALTER TABLE user_ ADD CONSTRAINT fk_user__tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE user_login (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  lastConnection NUMERIC(19, 0),
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE user_contactinfo (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  email NVARCHAR(255),
  phone NVARCHAR(50),
  mobile NVARCHAR(50),
  fax NVARCHAR(50),
  building NVARCHAR(50),
  room NVARCHAR(50),
  address NVARCHAR(255),
  zipCode NVARCHAR(50),
  city NVARCHAR(255),
  state NVARCHAR(255),
  country NVARCHAR(255),
  website NVARCHAR(255),
  personal BIT NOT NULL,
  UNIQUE (tenantid, userId, personal),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE user_contactinfo ADD CONSTRAINT fk_contact_user FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;
CREATE INDEX idx_user_contactinfo ON user_contactinfo (userId, personal);

CREATE TABLE custom_usr_inf_def (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(75) NOT NULL,
  description NVARCHAR(MAX),
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_custom_usr_inf_def_name ON custom_usr_inf_def (name);
ALTER TABLE custom_usr_inf_def ADD CONSTRAINT fk_custom_usr_inf_def_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE custom_usr_inf_val (
  id NUMERIC(19, 0) NOT NULL,
  tenantid NUMERIC(19, 0) NOT NULL,
  definitionId NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  value NVARCHAR(255),
  UNIQUE (tenantid, definitionId, userId),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_user_id FOREIGN KEY (tenantid, userId) REFERENCES user_ (tenantid, id) ON DELETE CASCADE;
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_definition_id FOREIGN KEY (tenantid, definitionId) REFERENCES custom_usr_inf_def (tenantid, id) ON DELETE CASCADE;
ALTER TABLE custom_usr_inf_val ADD CONSTRAINT fk_custom_usr_inf_val_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE user_membership (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  roleId NUMERIC(19, 0) NOT NULL,
  groupId NUMERIC(19, 0) NOT NULL,
  assignedBy NUMERIC(19, 0),
  assignedDate NUMERIC(19, 0),
  UNIQUE (tenantid, userId, roleId, groupId),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE user_membership ADD CONSTRAINT fk_user_membership_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE icon (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  mimetype NVARCHAR(255) NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  CONSTRAINT pk_icon PRIMARY KEY (tenantId, id)
);

CREATE TABLE arch_process_comment(
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0),
  processInstanceId NUMERIC(19, 0) NOT NULL,
  postDate NUMERIC(19, 0) NOT NULL,
  content NVARCHAR(512) NOT NULL,
  archiveDate NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_arch_process_comment on arch_process_comment (sourceobjectid);
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate);
ALTER TABLE arch_process_comment ADD CONSTRAINT fk_arch_process_comment_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE process_comment (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  kind NVARCHAR(25) NOT NULL,
  userId NUMERIC(19, 0),
  processInstanceId NUMERIC(19, 0) NOT NULL,
  postDate NUMERIC(19, 0) NOT NULL,
  content NVARCHAR(512) NOT NULL,
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_process_comment on process_comment (processInstanceId);
ALTER TABLE process_comment ADD CONSTRAINT fk_process_comment_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE category (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  creator NUMERIC(19, 0),
  description NVARCHAR(MAX),
  creationDate NUMERIC(19, 0) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantid, name),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE category ADD CONSTRAINT fk_category_tenantId FOREIGN KEY (tenantid) REFERENCES tenant (id);

CREATE TABLE processcategorymapping (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  categoryid NUMERIC(19, 0) NOT NULL,
  processid NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantid, categoryid, processid),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE processcategorymapping ADD CONSTRAINT fk_processcategorymapping_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE processcategorymapping ADD CONSTRAINT fk_catmapping_catid FOREIGN KEY (tenantid, categoryid) REFERENCES category(tenantid, id) ON DELETE CASCADE;

CREATE TABLE process_content (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  content NVARCHAR(MAX) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE process_definition (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processId NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(150) NOT NULL,
  version NVARCHAR(50) NOT NULL,
  description NVARCHAR(255),
  deploymentDate NUMERIC(19, 0) NOT NULL,
  deployedBy NUMERIC(19, 0) NOT NULL,
  activationState NVARCHAR(30) NOT NULL,
  configurationState NVARCHAR(30) NOT NULL,
  displayName NVARCHAR(75),
  displayDescription NVARCHAR(255),
  lastUpdateDate NUMERIC(19, 0),
  categoryId NUMERIC(19, 0),
  iconPath NVARCHAR(255),
  content_tenantid NUMERIC(19, 0) NOT NULL,
  content_id NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name, version)
);
ALTER TABLE process_definition ADD CONSTRAINT fk_process_definition_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE process_definition ADD CONSTRAINT fk_process_definition_content FOREIGN KEY (content_tenantid, content_id) REFERENCES process_content(tenantid, id);

CREATE TABLE processsupervisor (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  processDefId NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0) NOT NULL,
  groupId NUMERIC(19, 0) NOT NULL,
  roleId NUMERIC(19, 0) NOT NULL,
  UNIQUE (tenantid, processDefId, userId, groupId, roleId),
  PRIMARY KEY (tenantid, id)
);
ALTER TABLE processsupervisor ADD CONSTRAINT fk_processsupervisor_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);