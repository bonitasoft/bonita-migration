CREATE TABLE form_mapping (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  process NUMERIC(19, 0) NOT NULL,
  task NVARCHAR(255) NULL,
  form NVARCHAR(1024) NULL,
  isexternal BIT NOT NULL,
  type NVARCHAR(16) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NULL,
  lastUpdatedBy NUMERIC(19, 0) NULL,
  PRIMARY KEY (tenantId, id)
)
@@