CREATE TABLE bar_resource (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  process_id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255) NOT NULL,
  type VARCHAR2(16) NOT NULL,
  content BLOB NOT NULL,
  UNIQUE (tenantId, process_id, name, type),
  PRIMARY KEY (tenantId, id)
)
@@
CREATE INDEX idx_bar_resource ON bar_resource (tenantId, process_id, type, name)