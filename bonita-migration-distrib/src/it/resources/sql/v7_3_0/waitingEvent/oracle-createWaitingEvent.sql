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
)
@@
CREATE INDEX idx_waiting_event ON waiting_event (progress, tenantid, kind, locked, active)
@@

CREATE TABLE process_definition (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  processId NUMBER(19, 0) NOT NULL,
  name VARCHAR2(150 CHAR) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
  description VARCHAR2(255 CHAR),
  deploymentDate NUMBER(19, 0) NOT NULL,
  deployedBy NUMBER(19, 0) NOT NULL,
  activationState VARCHAR2(30 CHAR) NOT NULL,
  configurationState VARCHAR2(30 CHAR) NOT NULL,
  displayName VARCHAR2(75 CHAR),
  displayDescription VARCHAR2(255 CHAR),
  lastUpdateDate NUMBER(19, 0),
  categoryId NUMBER(19, 0),
  iconPath VARCHAR2(255 CHAR),
  content_tenantid NUMBER(19, 0) NOT NULL,
  content_id NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantId, id),
  CONSTRAINT UK_Process_Definition UNIQUE (tenantId, name, version)
)
@@
INSERT INTO process_definition
    (tenantid, id, processId, name, version, description, deploymentDate, deployedBy, activationState, configurationState, displayName, displayDescription, lastUpdateDate, categoryId, iconPath, content_tenantid, content_id)
    VALUES(1, 0, 123456789000, 'process', '1.0', '', 0, 0, '', '', '', '', 0, 0, '', 0, 0)
@@