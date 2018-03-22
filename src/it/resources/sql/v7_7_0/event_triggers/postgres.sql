CREATE TABLE event_trigger_instance (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	eventInstanceId INT8 NOT NULL,
  	eventInstanceName VARCHAR(50),
  	messageName VARCHAR(255),
  	targetProcess VARCHAR(255),
  	targetFlowNode VARCHAR(255),
  	signalName VARCHAR(255),
  	errorCode VARCHAR(255),
  	executionDate INT8,
  	jobTriggerName VARCHAR(255),
  	PRIMARY KEY (tenantid, id)
);