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
  CONSTRAINT pk_bpm_failure PRIMARY KEY (id)
) ENGINE = INNODB
@@
CREATE INDEX idx_bpm_failure_flownodeinstanceid ON bpm_failure (flowNodeInstanceId)
@@
CREATE INDEX idx_bpm_failure_processinstanceid ON bpm_failure (processInstanceId)
@@
CREATE INDEX idx_bpm_failure_rootprocessinstanceid ON bpm_failure (rootProcessInstanceId)
@@
CREATE INDEX idx_bpm_failure_processdefinitionid ON bpm_failure (processDefinitionId)