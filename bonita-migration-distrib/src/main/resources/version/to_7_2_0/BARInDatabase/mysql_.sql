CREATE TABLE bar_resource (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  process_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(16) NOT NULL,
  content LONGBLOB NOT NULL,
  UNIQUE (tenantId, process_id, name, type),
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB
@@
CREATE INDEX idx_bar_resource ON bar_resource (tenantId, process_id, type, name)