CREATE TABLE event_trigger_instance (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	kind NVARCHAR(15) NOT NULL,
  	eventInstanceId NUMERIC(19, 0) NOT NULL,
  	eventInstanceName NVARCHAR(50),
  	messageName NVARCHAR(255),
  	targetProcess NVARCHAR(255),
  	targetFlowNode NVARCHAR(255),
  	signalName NVARCHAR(255),
  	errorCode NVARCHAR(255),
  	executionDate NUMERIC(19, 0),
  	jobTriggerName NVARCHAR(255),
  	PRIMARY KEY (tenantid, id)
)
GO