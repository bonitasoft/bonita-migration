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

CREATE TABLE process_definition (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  processId INT8 NOT NULL,
  name VARCHAR(150) NOT NULL,
  version VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  deploymentDate INT8 NOT NULL,
  deployedBy INT8 NOT NULL,
  activationState VARCHAR(30) NOT NULL,
  configurationState VARCHAR(30) NOT NULL,
  displayName VARCHAR(75),
  displayDescription VARCHAR(255),
  lastUpdateDate INT8,
  categoryId INT8,
  iconPath VARCHAR(255),
  content_tenantid INT8 NOT NULL,
  content_id INT8 NOT NULL,
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name, version)
);
INSERT INTO process_definition
(tenantid, id, processId, name, version, description, deploymentDate, deployedBy, activationState, configurationState, displayName, displayDescription, lastUpdateDate, categoryId, iconPath, content_tenantid, content_id)
VALUES(1, 0, 123456789000, 'process', '1.0', '', 0, 0, '', '', '', '', 0, 0, '', 0, 0);
