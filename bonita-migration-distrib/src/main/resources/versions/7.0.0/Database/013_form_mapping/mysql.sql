CREATE TABLE form_mapping (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  process BIGINT NOT NULL,
  task VARCHAR(255) NULL,
  form VARCHAR(1024) NULL,
  isexternal BOOLEAN NOT NULL,
  type VARCHAR(16) NOT NULL,
  lastUpdateDate BIGINT NULL,
  lastUpdatedBy BIGINT NULL,
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB
@@