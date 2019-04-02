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

CREATE TABLE message_instance (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	messageName VARCHAR2(255 CHAR) NOT NULL,
  	targetProcess VARCHAR2(255 CHAR) NOT NULL,
  	targetFlowNode VARCHAR2(255 CHAR) NULL,
  	locked NUMBER(1) NOT NULL,
  	handled NUMBER(1) NOT NULL,
  	processDefinitionId NUMBER(19, 0) NOT NULL,
  	flowNodeName VARCHAR2(255 CHAR),
  	correlation1 VARCHAR2(128 CHAR),
  	correlation2 VARCHAR2(128 CHAR),
  	correlation3 VARCHAR2(128 CHAR),
  	correlation4 VARCHAR2(128 CHAR),
  	correlation5 VARCHAR2(128 CHAR),
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3);
