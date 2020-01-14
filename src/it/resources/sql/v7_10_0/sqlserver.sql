CREATE TABLE message_instance (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	messageName NVARCHAR(255) NOT NULL,
  	targetProcess NVARCHAR(255) NOT NULL,
  	targetFlowNode NVARCHAR(255) NULL,
  	locked BIT NOT NULL,
  	handled BIT NOT NULL,
  	processDefinitionId NUMERIC(19, 0) NOT NULL,
  	flowNodeName NVARCHAR(255),
  	correlation1 NVARCHAR(128),
  	correlation2 NVARCHAR(128),
  	correlation3 NVARCHAR(128),
  	correlation4 NVARCHAR(128),
  	correlation5 NVARCHAR(128),
  	creationDate NUMERIC(19, 0) NOT NULL,
  	PRIMARY KEY (tenantid, id)
)
GO
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3)
GO
CREATE INDEX idx_message_instance_correl ON message_instance (correlation1, correlation2, correlation3, correlation4, correlation5)
GO
