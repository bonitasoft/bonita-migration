CREATE TABLE proc_parameter (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  process_id INT8 NOT NULL,
  name VARCHAR(255) NOT NULL,
  value VARCHAR(1024) NULL,
  PRIMARY KEY (tenantId, id)
)