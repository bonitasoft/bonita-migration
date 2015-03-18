CREATE TABLE form_mapping (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  process NUMBER(19, 0) NOT NULL,
  task VARCHAR2(255) NULL,
  form VARCHAR2(1024) NULL,
  isexternal NUMBER(1) NOT NULL,
  type VARCHAR2(16) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NULL,
  lastUpdatedBy NUMBER(19, 0) NULL,
  PRIMARY KEY (tenantId, id)
)
@@