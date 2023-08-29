CREATE TABLE sequence (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  nextid INT8 NOT NULL,
  PRIMARY KEY (tenantid, id)
);

CREATE TABLE platform (
  id INT8 NOT NULL,
  version VARCHAR(50) NOT NULL,
  initial_bonita_version VARCHAR(50) NOT NULL,
  created INT8 NOT NULL,
  created_by VARCHAR(50) NOT NULL,
  information TEXT,
  PRIMARY KEY (id)
);

CREATE TABLE process_instance (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(75) NOT NULL,
  processDefinitionId INT8 NOT NULL,
  description VARCHAR(255),
  startDate INT8 NOT NULL,
  startedBy INT8 NOT NULL,
  startedBySubstitute INT8 NOT NULL,
  endDate INT8 NOT NULL,
  stateId INT NOT NULL,
  stateCategory VARCHAR(50) NOT NULL,
  lastUpdate INT8 NOT NULL,
  containerId INT8,
  rootProcessInstanceId INT8,
  callerId INT8,
  callerType VARCHAR(50),
  interruptingEventId INT8,
  stringIndex1 VARCHAR(255),
  stringIndex2 VARCHAR(255),
  stringIndex3 VARCHAR(255),
  stringIndex4 VARCHAR(255),
  stringIndex5 VARCHAR(255),
  PRIMARY KEY (tenantid, id)
);
CREATE INDEX idx1_proc_inst_pdef_state ON process_instance (tenantid, processdefinitionid, stateid);
CREATE TABLE tenant (
  id INT8 NOT NULL,
  created INT8 NOT NULL,
  createdBy VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  defaultTenant BOOLEAN NOT NULL,
  iconname VARCHAR(50),
  iconpath VARCHAR(255),
  name VARCHAR(50) NOT NULL,
  status VARCHAR(15) NOT NULL,
  PRIMARY KEY (id)
);
ALTER TABLE process_instance ADD CONSTRAINT fk_process_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id);
CREATE TABLE ref_biz_data_inst (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	name VARCHAR(255) NOT NULL,
  	proc_inst_id INT8,
  	fn_inst_id INT8,
  	data_id INT8,
  	data_classname VARCHAR(255) NOT NULL
);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_proc FOREIGN KEY (tenantid, proc_inst_id) REFERENCES process_instance(tenantid, id) ON DELETE CASCADE;
