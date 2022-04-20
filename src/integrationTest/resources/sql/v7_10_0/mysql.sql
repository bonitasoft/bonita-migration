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
  	creationDate BIGINT NOT NULL,
  	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3);
CREATE INDEX idx_message_instance_correl ON message_instance (correlation1, correlation2, correlation3, correlation4, correlation5);