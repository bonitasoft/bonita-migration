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
  	creationDate NUMBER(19, 0) NOT NULL,
  	PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3);
CREATE INDEX idx_message_instance_correl ON message_instance (correlation1, correlation2, correlation3, correlation4, correlation5);