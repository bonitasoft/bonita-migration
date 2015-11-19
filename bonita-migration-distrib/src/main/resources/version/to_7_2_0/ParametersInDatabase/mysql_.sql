CREATE TABLE proc_parameter (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  process_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  value MEDIUMTEXT NULL,
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB