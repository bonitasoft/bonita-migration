
CREATE TABLE form_mapping (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  process INT8 NOT NULL,
  task VARCHAR(255) NULL,
  form VARCHAR(1024) NULL,
  isexternal BOOLEAN NOT NULL,
  type VARCHAR(16) NOT NULL,
  lastUpdateDate INT8 NULL,
  lastUpdatedBy INT8 NULL,
  PRIMARY KEY (tenantId, id)
)
