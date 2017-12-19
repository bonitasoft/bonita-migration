CREATE TABLE process_content (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  content CLOB NOT NULL,
  PRIMARY KEY (tenantid, id)
)