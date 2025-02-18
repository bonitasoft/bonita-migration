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
  CONSTRAINT pk_arch_bpm_failure PRIMARY KEY (id)
)
@@
CREATE INDEX idx_arch_bpm_failure_flownodeinstanceid ON arch_bpm_failure (flowNodeInstanceId)
@@
CREATE INDEX idx_arch_bpm_failure_processinstanceid ON arch_bpm_failure (processInstanceId)
@@
CREATE INDEX idx_arch_bpm_failure_rootprocessinstanceid ON arch_bpm_failure (rootProcessInstanceId)
@@
CREATE INDEX idx_arch_bpm_failure_processdefinitionid ON arch_bpm_failure (processDefinitionId)