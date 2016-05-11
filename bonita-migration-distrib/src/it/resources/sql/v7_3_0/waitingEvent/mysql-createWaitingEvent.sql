CREATE TABLE waiting_event (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	eventType VARCHAR(50),
  	messageName VARCHAR(255),
  	signalName VARCHAR(255),
  	errorCode VARCHAR(255),
  	processName VARCHAR(150),
  	flowNodeName VARCHAR(50),
  	flowNodeDefinitionId BIGINT,
  	subProcessId BIGINT,
  	processDefinitionId BIGINT,
  	rootProcessInstanceId BIGINT,
  	parentProcessInstanceId BIGINT,
  	flowNodeInstanceId BIGINT,
  	relatedActivityInstanceId BIGINT,
  	locked BOOLEAN,
  	active BOOLEAN,
  	progress TINYINT,
  	correlation1 VARCHAR(128),
  	correlation2 VARCHAR(128),
  	correlation3 VARCHAR(128),
  	correlation4 VARCHAR(128),
  	correlation5 VARCHAR(128),
  	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_waiting_event ON waiting_event (progress, tenantid, kind, locked, active);


CREATE TABLE process_definition (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processId BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  version VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  deploymentDate BIGINT NOT NULL,
  deployedBy BIGINT NOT NULL,
  activationState VARCHAR(30) NOT NULL,
  configurationState VARCHAR(30) NOT NULL,
  displayName VARCHAR(75),
  displayDescription VARCHAR(255),
  lastUpdateDate BIGINT,
  categoryId BIGINT,
  iconPath VARCHAR(255),
  content_tenantid BIGINT NOT NULL,
  content_id BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name, version)
) ENGINE = INNODB;
INSERT INTO process_definition
    (tenantid, id, processId, name, version, description, deploymentDate, deployedBy, activationState, configurationState, displayName, displayDescription, lastUpdateDate, categoryId, iconPath, content_tenantid, content_id)
    VALUES(1, 0, 123456789000, 'process', '1.0', '', 0, 0, '', '', '', '', 0, 0, '', 0, 0);
