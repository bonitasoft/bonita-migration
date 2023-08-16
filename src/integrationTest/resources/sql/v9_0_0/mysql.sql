CREATE TABLE sequence (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  nextid BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE platform (
  id BIGINT NOT NULL,
  version VARCHAR(50) NOT NULL,
  initial_bonita_version VARCHAR(50) NOT NULL,
  created BIGINT NOT NULL,
  created_by VARCHAR(50) NOT NULL,
  information TEXT,
  PRIMARY KEY (id)
) ENGINE = INNODB;