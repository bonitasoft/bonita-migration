CREATE TABLE bpm_failure (
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
  CONSTRAINT pk_bpm_failure PRIMARY KEY (id)
)
@@
CREATE INDEX idx_bpm_failure_flownodeinstanceid ON bpm_failure (flowNodeInstanceId)
@@
CREATE INDEX idx_bpm_failure_processinstanceid ON bpm_failure (processInstanceId)
@@
CREATE INDEX idx_bpm_failure_rootprocessinstanceid ON bpm_failure (rootProcessInstanceId)
@@
CREATE INDEX idx_bpm_failure_processdefinitionid ON bpm_failure (processDefinitionId)