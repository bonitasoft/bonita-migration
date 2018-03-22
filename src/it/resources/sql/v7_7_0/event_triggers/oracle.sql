CREATE TABLE event_trigger_instance (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	kind VARCHAR2(15 CHAR) NOT NULL,
  	eventInstanceId NUMBER(19, 0) NOT NULL,
  	eventInstanceName VARCHAR2(50 CHAR),
  	messageName VARCHAR2(255 CHAR),
  	targetProcess VARCHAR2(255 CHAR),
  	targetFlowNode VARCHAR2(255 CHAR),
  	signalName VARCHAR2(255 CHAR),
  	errorCode VARCHAR2(255 CHAR),
  	executionDate NUMBER(19, 0),
  	jobTriggerName VARCHAR2(255 CHAR),
  	PRIMARY KEY (tenantid, id)
);