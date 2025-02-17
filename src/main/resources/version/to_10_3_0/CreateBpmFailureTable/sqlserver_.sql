CREATE TABLE bpm_failure (
  id NUMERIC(19, 0) NOT NULL,
  processDefinitionId NUMERIC(19, 0) NOT NULL,
  processInstanceId NUMERIC(19, 0) NOT NULL,
  rootProcessInstanceId NUMERIC(19, 0),
  flowNodeInstanceId NUMERIC(19, 0),
  scope NVARCHAR(255),
  context NVARCHAR(1024),
  errorMessage NVARCHAR(1024),
  stackTrace NVARCHAR(MAX),
  failureDate NUMERIC(19, 0) NOT NULL,
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