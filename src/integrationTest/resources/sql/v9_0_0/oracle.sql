CREATE TABLE sequence (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  nextid NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE platform (
  id NUMBER(19, 0) NOT NULL,
  version VARCHAR2(50 CHAR) NOT NULL,
  initial_bonita_version VARCHAR2(50 CHAR) NOT NULL,
  created NUMBER(19, 0) NOT NULL,
  created_by VARCHAR2(50 CHAR) NOT NULL,
  information CLOB,
  PRIMARY KEY (id)
);

CREATE TABLE process_instance (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(75 CHAR) NOT NULL,
  processDefinitionId NUMBER(19, 0) NOT NULL,
  description VARCHAR2(255 CHAR),
  startDate NUMBER(19, 0) NOT NULL,
  startedBy NUMBER(19, 0) NOT NULL,
  startedBySubstitute NUMBER(19, 0) NOT NULL,
  endDate NUMBER(19, 0) NOT NULL,
  stateId INT NOT NULL,
  stateCategory VARCHAR2(50 CHAR) NOT NULL,
  lastUpdate NUMBER(19, 0) NOT NULL,
  containerId NUMBER(19, 0),
  rootProcessInstanceId NUMBER(19, 0),
  callerId NUMBER(19, 0),
  callerType VARCHAR2(50 CHAR),
  interruptingEventId NUMBER(19, 0),
  stringIndex1 VARCHAR2(255 CHAR),
  stringIndex2 VARCHAR2(255 CHAR),
  stringIndex3 VARCHAR2(255 CHAR),
  stringIndex4 VARCHAR2(255 CHAR),
  stringIndex5 VARCHAR2(255 CHAR),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_proc_inst_pdef_state ON process_instance (tenantid, processdefinitionid, stateid);
CREATE TABLE ref_biz_data_inst (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	kind VARCHAR2(15 CHAR) NOT NULL,
  	name VARCHAR2(255 CHAR) NOT NULL,
  	proc_inst_id NUMBER(19, 0),
  	fn_inst_id NUMBER(19, 0),
  	data_id NUMBER(19, 0),
  	data_classname VARCHAR2(255 CHAR) NOT NULL
);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_proc FOREIGN KEY (tenantid, proc_inst_id) REFERENCES process_instance(tenantid, id) ON DELETE CASCADE;
