CREATE TABLE arch_bpm_failure (
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
  archiveDate NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0) NOT NULL,
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