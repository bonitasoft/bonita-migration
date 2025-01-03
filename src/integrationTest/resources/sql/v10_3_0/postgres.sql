CREATE TABLE sequence (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  nextid INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE tenant (
  id INT8 NOT NULL,
  created INT8 NOT NULL,
  createdBy VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  defaultTenant BOOLEAN NOT NULL,
  iconname VARCHAR(50),
  iconpath VARCHAR(255),
  name VARCHAR(50) NOT NULL,
  status VARCHAR(15) NOT NULL,
  PRIMARY KEY (id)
);

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
CREATE INDEX idx_fni_loggroup3_terminal ON flownode_instance(logicalgroup3, terminal);
CREATE INDEX idx_fn_lg2_state ON flownode_instance (logicalGroup2, stateName);
CREATE INDEX idx_fni_activity_instance_id_kind ON flownode_instance(activityInstanceId, kind);
ALTER TABLE flownode_instance ADD CONSTRAINT fk_flownode_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE pending_mapping (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  activityId INT8 NOT NULL,
  actorId INT8,
  userId INT8,
  PRIMARY KEY (tenantid, id)
);
CREATE UNIQUE INDEX idx_UQ_pending_mapping ON pending_mapping (activityId, userId, actorId);
ALTER TABLE pending_mapping ADD CONSTRAINT fk_pending_mapping_flownode_instanceId FOREIGN KEY (tenantid, activityId) REFERENCES flownode_instance(tenantid, id);

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
CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (fn_inst_id);
CREATE INDEX idx_biz_data_inst3 ON ref_biz_data_inst (proc_inst_id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE;

CREATE TABLE page_mapping (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  key_ VARCHAR(255) NOT NULL,
  pageId INT8 NULL,
  url VARCHAR(1024) NULL,
  urladapter VARCHAR(255) NULL,
  page_authoriz_rules TEXT NULL,
  lastUpdateDate INT8 NULL,
  lastUpdatedBy INT8 NULL,
  CONSTRAINT UK_page_mapping UNIQUE (tenantId, key_),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE form_mapping (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  process INT8 NOT NULL,
  type INT NOT NULL,
  task VARCHAR(255),
  page_mapping_tenant_id INT8,
  page_mapping_id INT8,
  lastUpdateDate INT8,
  lastUpdatedBy INT8,
  target VARCHAR(16) NOT NULL,
  PRIMARY KEY (tenantId, id)
);
ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id);

CREATE TABLE page (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  description TEXT,
  installationDate INT8 NOT NULL,
  installedBy INT8 NOT NULL,
  provided BOOLEAN,
  editable BOOLEAN,
  removable BOOLEAN,
  lastModificationDate INT8 NOT NULL,
  lastUpdatedBy INT8 NOT NULL,
  contentName VARCHAR(280) NOT NULL,
  content BYTEA,
  contentType VARCHAR(50) NOT NULL,
  processDefinitionId INT8 NOT NULL,
  pageHash VARCHAR(32)
);
ALTER TABLE page ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id);
ALTER TABLE page ADD CONSTRAINT uk_page UNIQUE (tenantId, name, processDefinitionId);

CREATE TABLE profile (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  isDefault BOOLEAN NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  creationDate INT8 NOT NULL,
  createdBy INT8 NOT NULL,
  lastUpdateDate INT8 NOT NULL,
  lastUpdatedBy INT8 NOT NULL,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);
ALTER TABLE profile ADD CONSTRAINT fk_profile_tenantId FOREIGN KEY (tenantId) REFERENCES tenant(id);

CREATE TABLE profilemember (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  profileId INT8 NOT NULL,
  userId INT8 NOT NULL,
  groupId INT8 NOT NULL,
  roleId INT8 NOT NULL,
  UNIQUE (tenantId, profileId, userId, groupId, roleId),
  PRIMARY KEY (tenantId, id)
);
ALTER TABLE profilemember ADD CONSTRAINT fk_profilemember_tenantId FOREIGN KEY (tenantId) REFERENCES tenant(id);
ALTER TABLE profilemember ADD CONSTRAINT fk_profilemember_profileId FOREIGN KEY (tenantId, profileId) REFERENCES profile(tenantId, id);

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
  internalProfile VARCHAR(255),
  isLink BOOLEAN DEFAULT FALSE
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
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  applicationId INT8 NOT NULL,
  pageId INT8 NOT NULL,
  token VARCHAR(255) NOT NULL
);
ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT uk_app_page_appId_token UNIQUE (tenantId, applicationId, token);
CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId);
ALTER TABLE business_app_page ADD CONSTRAINT fk_app_page_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE business_app_page ADD CONSTRAINT fk_bus_app_id FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id) ON DELETE CASCADE;
ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page (tenantid, id);

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
CREATE INDEX idx_app_menu_app ON business_app_menu (applicationId);
CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId);
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_appId FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_pageId FOREIGN KEY (tenantid, applicationPageId) REFERENCES business_app_page (tenantid, id);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_parentId FOREIGN KEY (tenantid, parentId) REFERENCES business_app_menu (tenantid, id);
