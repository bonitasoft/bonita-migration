CREATE TABLE bpm_failure (
  id NUMBER(19, 0) NOT NULL,
  processDefinitionId NUMBER(19, 0) NOT NULL,
  processInstanceId NUMBER(19, 0) NOT NULL,
  rootProcessInstanceId NUMBER(19, 0),
  flowNodeInstanceId NUMBER(19, 0),
  scope VARCHAR2(255 CHAR),
  context VARCHAR2(1024 CHAR),
  errorMessage VARCHAR2(1024 CHAR),
  stackTrace CLOB,
  failureDate NUMBER(19, 0) NOT NULL,
  CONSTRAINT pk_bpm_failure PRIMARY KEY (id)
)
@@
CREATE INDEX idx_flownode_instance_id ON bpm_failure (flowNodeInstanceId)
@@
CREATE INDEX idx_process_instance_id ON bpm_failure (processInstanceId)
@@
CREATE INDEX idx_root_process_instance_id ON bpm_failure (rootProcessInstanceId)
@@
CREATE INDEX idx_process_definition_id ON bpm_failure (processDefinitionId)