CREATE TABLE arch_bpm_failure (
  id NUMBER(19, 0) NOT NULL,
  processDefinitionId NUMBER(19, 0) NOT NULL,
  processInstanceId NUMBER(19, 0) NOT NULL,
  rootProcessInstanceId NUMBER(19, 0),
  flowNodeInstanceId NUMBER(19, 0),
  scope VARCHAR2(255 CHAR),
  context VARCHAR2(1024 CHAR),
  errorMessage VARCHAR2(1024 CHAR),
  stackTrace CLOB,
  failureDate NUMBER(19, 0) NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL,
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