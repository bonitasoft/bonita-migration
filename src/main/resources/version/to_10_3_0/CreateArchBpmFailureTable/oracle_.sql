CREATE TABLE arch_bpm_failure (
  id NUMBER(19, 0) NOT NULL,
  processDefinitionId NUMBER(19, 0) NOT NULL,
  processInstanceId NUMBER(19, 0) NOT NULL,
  flowNodeInstanceId NUMBER(19, 0),
  scope VARCHAR2(255 CHAR),
  context VARCHAR2(1024 CHAR),
  errorMessage VARCHAR2(1024 CHAR),
  stackTrace CLOB,
  failureDate NUMBER(19, 0) NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (id)
)
@@
CREATE INDEX idx_arch_flownode_instance_id ON arch_bpm_failure (flowNodeInstanceId)
@@
CREATE INDEX idx_arch_process_instance_id ON arch_bpm_failure (processInstanceId)
@@
CREATE INDEX idx_arch_process_definition_id ON arch_bpm_failure (processDefinitionId)