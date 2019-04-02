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

CREATE TABLE message_instance (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	messageName VARCHAR(255) NOT NULL,
  	targetProcess VARCHAR(255) NOT NULL,
  	targetFlowNode VARCHAR(255) NULL,
  	locked BOOLEAN NOT NULL,
  	handled BOOLEAN NOT NULL,
  	processDefinitionId BIGINT NOT NULL,
  	flowNodeName VARCHAR(255),
  	correlation1 VARCHAR(128),
  	correlation2 VARCHAR(128),
  	correlation3 VARCHAR(128),
  	correlation4 VARCHAR(128),
  	correlation5 VARCHAR(128),
  	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3);
