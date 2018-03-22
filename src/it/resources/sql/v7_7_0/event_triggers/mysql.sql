CREATE TABLE event_trigger_instance (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	eventInstanceId BIGINT NOT NULL,
  	eventInstanceName VARCHAR(50),
  	messageName VARCHAR(255),
  	targetProcess VARCHAR(255),
  	targetFlowNode VARCHAR(255),
  	signalName VARCHAR(255),
  	errorCode VARCHAR(255),
  	executionDate BIGINT,
  	jobTriggerName VARCHAR(255),
  	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;