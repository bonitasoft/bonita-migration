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

CREATE TABLE platform (
  id INT8 NOT NULL,
  version VARCHAR(50) NOT NULL,
  initial_bonita_version VARCHAR(50) NOT NULL,
  application_version VARCHAR(50) NOT NULL,
  maintenance_message TEXT,
  maintenance_message_active BOOLEAN NOT NULL,
  created INT8 NOT NULL,
  created_by VARCHAR(50) NOT NULL,
  information TEXT,
  PRIMARY KEY (id)
);
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
CREATE INDEX idx1_arch_process_instance ON arch_process_instance(tenantId, sourceObjectId, rootProcessInstanceId, callerId);
CREATE INDEX idx2_arch_process_instance ON arch_process_instance(tenantId, processDefinitionId, archiveDate);
CREATE INDEX idx3_arch_process_instance ON arch_process_instance(tenantId, sourceObjectId, callerId, stateId);
