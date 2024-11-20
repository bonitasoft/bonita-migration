CREATE TABLE arch_bpm_failure (
  id BIGINT NOT NULL,
  processDefinitionId BIGINT NOT NULL,
  processInstanceId BIGINT NOT NULL,
  flowNodeInstanceId BIGINT,
  scope VARCHAR(255),
  context VARCHAR(1024),
  errorMessage VARCHAR(1024),
  stackTrace LONGTEXT,
  failureDate BIGINT NOT NULL,
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL,
  PRIMARY KEY (id)
)  ENGINE = INNODB
@@
CREATE INDEX idx_arch_flownode_instance_id ON arch_bpm_failure (flowNodeInstanceId)
@@
CREATE INDEX idx_arch_process_instance_id ON arch_bpm_failure (processInstanceId)
@@
CREATE INDEX idx_arch_process_definition_id ON arch_bpm_failure (processDefinitionId)