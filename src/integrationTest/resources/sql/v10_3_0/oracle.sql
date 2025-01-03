CREATE TABLE sequence (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  nextid NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE tenant (
  id NUMBER(19, 0) NOT NULL,
  created NUMBER(19, 0) NOT NULL,
  createdBy VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(255 CHAR),
  defaultTenant NUMBER(1) NOT NULL,
  iconname VARCHAR2(50 CHAR),
  iconpath VARCHAR2(255 CHAR),
  name VARCHAR2(50 CHAR) NOT NULL,
  status VARCHAR2(15 CHAR) NOT NULL,
  PRIMARY KEY (id)
);

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
CREATE INDEX idx_fni_loggroup3_terminal ON flownode_instance(logicalgroup3, terminal);
CREATE INDEX idx_fn_lg2_state ON flownode_instance (logicalGroup2, stateName);
CREATE INDEX idx_fni_activity_instance_id_kind ON flownode_instance(activityInstanceId, kind);
ALTER TABLE flownode_instance ADD CONSTRAINT fk_flownode_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);

CREATE TABLE pending_mapping (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  activityId NUMBER(19, 0) NOT NULL,
  actorId NUMBER(19, 0),
  userId NUMBER(19, 0),
  PRIMARY KEY (tenantid, id)
);
CREATE UNIQUE INDEX idx_UQ_pending_mapping ON pending_mapping (activityId, userId, actorId);
ALTER TABLE pending_mapping ADD CONSTRAINT fk_pMap_flnId FOREIGN KEY (tenantid, activityId) REFERENCES flownode_instance(tenantid, id);

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

CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (fn_inst_id);
CREATE INDEX idx_biz_data_inst3 ON ref_biz_data_inst (proc_inst_id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE;

CREATE TABLE page_mapping (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  key_ VARCHAR2(255 CHAR) NOT NULL,
  pageId NUMBER(19, 0) NULL,
  url VARCHAR2(1024 CHAR) NULL,
  urladapter VARCHAR2(255 CHAR) NULL,
  page_authoriz_rules VARCHAR2(1024 CHAR) NULL,
  lastUpdateDate NUMBER(19, 0) NULL,
  lastUpdatedBy NUMBER(19, 0) NULL,
  CONSTRAINT UK_page_mapping UNIQUE (tenantId, key_),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE form_mapping (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  process NUMBER(19, 0) NOT NULL,
  type INT NOT NULL,
  task VARCHAR2(255 CHAR),
  page_mapping_tenant_id NUMBER(19, 0),
  page_mapping_id NUMBER(19, 0),
  lastUpdateDate NUMBER(19, 0),
  lastUpdatedBy NUMBER(19, 0),
  target VARCHAR2(16 CHAR) NOT NULL,
  PRIMARY KEY (tenantId, id)
);
ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id);

CREATE TABLE page (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  displayName VARCHAR2(255 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  installationDate NUMBER(19, 0) NOT NULL,
  installedBy NUMBER(19, 0) NOT NULL,
  provided NUMBER(1),
  editable NUMBER(1),
  removable NUMBER(1),
  lastModificationDate NUMBER(19, 0) NOT NULL,
  lastUpdatedBy NUMBER(19, 0) NOT NULL,
  contentName VARCHAR2(280 CHAR) NOT NULL,
  content BLOB,
  contentType VARCHAR2(50 CHAR),
  processDefinitionId NUMBER(19, 0) NOT NULL,
  pageHash VARCHAR2(32 CHAR)
);
ALTER TABLE page ADD CONSTRAINT pk_page PRIMARY KEY (tenantid, id);
ALTER TABLE page ADD CONSTRAINT uk_page UNIQUE (tenantId, name, processDefinitionId);

CREATE TABLE profile (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  isDefault NUMBER(1) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  creationDate NUMBER(19, 0) NOT NULL,
  createdBy NUMBER(19, 0) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  lastUpdatedBy NUMBER(19, 0) NOT NULL,
  CONSTRAINT UK_Profile UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);

CREATE TABLE profilemember (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  profileId NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0) NOT NULL,
  groupId NUMBER(19, 0) NOT NULL,
  roleId NUMBER(19, 0) NOT NULL,
  UNIQUE (tenantId, profileId, userId, groupId, roleId),
  PRIMARY KEY (tenantId, id)
);

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
  internalProfile VARCHAR2(255 CHAR),
  isLink NUMBER(1) DEFAULT 0
);
ALTER TABLE business_app ADD CONSTRAINT pk_business_app PRIMARY KEY (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT UK_Business_app UNIQUE (tenantId, token, version);
CREATE INDEX idx_app_token ON business_app (token);
CREATE INDEX idx_app_profile ON business_app (profileId);
CREATE INDEX idx_app_homepage ON business_app (homePageId);
ALTER TABLE business_app ADD CONSTRAINT fk_app_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE business_app ADD CONSTRAINT fk_app_profileId FOREIGN KEY (tenantid, profileId) REFERENCES profile (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT fk_app_layoutId FOREIGN KEY (tenantid, layoutId) REFERENCES page (tenantid, id);
ALTER TABLE business_app ADD CONSTRAINT fk_app_themeId FOREIGN KEY (tenantid, themeId) REFERENCES page (tenantid, id);

CREATE TABLE business_app_page (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  applicationId NUMBER(19, 0) NOT NULL,
  pageId NUMBER(19, 0) NOT NULL,
  token VARCHAR2(255 CHAR) NOT NULL
);
ALTER TABLE business_app_page ADD CONSTRAINT pk_business_app_page PRIMARY KEY (tenantid, id);
ALTER TABLE business_app_page ADD CONSTRAINT UK_Business_app_page UNIQUE (tenantId, applicationId, token);
CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token);
CREATE INDEX idx_app_page_pageId ON business_app_page (pageId);
ALTER TABLE business_app_page ADD CONSTRAINT fk_app_page_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE business_app_page ADD CONSTRAINT fk_bus_app_id FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id) ON DELETE CASCADE;
ALTER TABLE business_app_page ADD CONSTRAINT fk_page_id FOREIGN KEY (tenantid, pageId) REFERENCES page (tenantid, id);

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
CREATE INDEX idx_app_menu_app ON business_app_menu (applicationId);
CREATE INDEX idx_app_menu_page ON business_app_menu (applicationPageId);
CREATE INDEX idx_app_menu_parent ON business_app_menu (parentId);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_appId FOREIGN KEY (tenantid, applicationId) REFERENCES business_app (tenantid, id);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_pageId FOREIGN KEY (tenantid, applicationPageId) REFERENCES business_app_page (tenantid, id);
ALTER TABLE business_app_menu ADD CONSTRAINT fk_app_menu_parentId FOREIGN KEY (tenantid, parentId) REFERENCES business_app_menu (tenantid, id);
