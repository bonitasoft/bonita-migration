CREATE TABLE proc_parameter (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  process_id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255 CHAR) NOT NULL,
  value VARCHAR2(1024 CHAR) NULL,
  PRIMARY KEY (tenantId, id)
)