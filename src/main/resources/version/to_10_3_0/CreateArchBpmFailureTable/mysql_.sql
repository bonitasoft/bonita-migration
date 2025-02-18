CREATE TABLE arch_bpm_failure (
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
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL,
  CONSTRAINT pk_arch_bpm_failure PRIMARY KEY (id)
)  ENGINE = INNODB
@@
CREATE INDEX idx_arch_bpm_failure_flownodeinstanceid ON arch_bpm_failure (flowNodeInstanceId)
@@
CREATE INDEX idx_arch_bpm_failure_processinstanceid ON arch_bpm_failure (processInstanceId)
@@
CREATE INDEX idx_arch_bpm_failure_rootprocessinstanceid ON arch_bpm_failure (rootProcessInstanceId)
@@
CREATE INDEX idx_arch_bpm_failure_processdefinitionid ON arch_bpm_failure (processDefinitionId)