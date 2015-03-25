CREATE TABLE form_mapping (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  process NUMBER(19, 0) NOT NULL,
  task VARCHAR2(255 CHAR) NULL,
  form VARCHAR2(1024 CHAR) NULL,
  target VARCHAR2(16 CHAR) NOT NULL,
  type VARCHAR2(16 CHAR) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NULL,
  lastUpdatedBy NUMBER(19, 0) NULL,
  PRIMARY KEY (tenantId, id)
)
@@
