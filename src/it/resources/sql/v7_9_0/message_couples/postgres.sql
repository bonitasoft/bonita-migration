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

CREATE TABLE message_instance (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	messageName VARCHAR(255) NOT NULL,
  	targetProcess VARCHAR(255) NOT NULL,
  	targetFlowNode VARCHAR(255) NULL,
  	locked BOOLEAN NOT NULL,
  	handled BOOLEAN NOT NULL,
  	processDefinitionId INT8 NOT NULL,
  	flowNodeName VARCHAR(255),
  	correlation1 VARCHAR(128),
  	correlation2 VARCHAR(128),
  	correlation3 VARCHAR(128),
  	correlation4 VARCHAR(128),
  	correlation5 VARCHAR(128),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3);