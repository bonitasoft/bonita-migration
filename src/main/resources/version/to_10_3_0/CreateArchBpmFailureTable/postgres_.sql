CREATE TABLE arch_bpm_failure (
  id INT8 NOT NULL,
  processDefinitionId INT8 NOT NULL,
  processInstanceId INT8 NOT NULL,
  rootProcessInstanceId INT8,
  flowNodeInstanceId INT8,
  scope VARCHAR(255),
  context VARCHAR(1024),
  errorMessage VARCHAR(1024),
  stackTrace TEXT,
  failureDate INT8 NOT NULL,
  archiveDate INT8 NOT NULL,
  sourceObjectId INT8 NOT NULL,
  PRIMARY KEY (id)
)
@@
CREATE INDEX idx_arch_flownode_instance_id ON arch_bpm_failure (flowNodeInstanceId)
@@
CREATE INDEX idx_arch_process_instance_id ON arch_bpm_failure (processInstanceId)
@@
CREATE INDEX idx_arch_root_process_instance_id ON arch_bpm_failure (rootProcessInstanceId)
@@
CREATE INDEX idx_arch_process_definition_id ON arch_bpm_failure (processDefinitionId)