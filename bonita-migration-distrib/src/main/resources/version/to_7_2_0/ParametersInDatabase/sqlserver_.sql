CREATE TABLE proc_parameter (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  process_id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  value NVARCHAR(MAX) NULL,
  PRIMARY KEY (tenantId, id)
)