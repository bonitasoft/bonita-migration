CREATE TABLE bpm_failure (
  id BIGINT NOT NULL,
  processDefinitionId BIGINT NOT NULL,
  processInstanceId BIGINT NOT NULL,
  rootProcessInstanceId BIGINT,
  flowNodeInstanceId BIGINT,
  scope VARCHAR(255),
  context VARCHAR(1024),
  errorMessage VARCHAR(1024),
  stackTrace LONGTEXT,
  failureDate BIGINT NOT NULL,
  PRIMARY KEY (id)
) ENGINE = INNODB
@@
CREATE INDEX idx_flownode_instance_id ON bpm_failure (flowNodeInstanceId)
@@
CREATE INDEX idx_process_instance_id ON bpm_failure (processInstanceId)
@@
CREATE INDEX idx_root_process_instance_id ON bpm_failure (rootProcessInstanceId)
@@
CREATE INDEX idx_process_definition_id ON bpm_failure (processDefinitionId)