CREATE TABLE platform (
  id BIGINT NOT NULL,
  version VARCHAR(50) NOT NULL,
  initial_bonita_version VARCHAR(50) NOT NULL,
  application_version VARCHAR(50) NOT NULL,
  maintenance_message TEXT,
  maintenance_message_active BOOLEAN NOT NULL,
  created BIGINT NOT NULL,
  created_by VARCHAR(50) NOT NULL,
  information TEXT,
  PRIMARY KEY (id)
) ENGINE = INNODB;

CREATE TABLE sequence (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  nextid BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE tenant (
  id BIGINT NOT NULL,
  created BIGINT NOT NULL,
  createdBy VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  defaultTenant BOOLEAN NOT NULL,
  iconname VARCHAR(50),
  iconpath VARCHAR(255),
  name VARCHAR(50) NOT NULL,
  status VARCHAR(15) NOT NULL,
  PRIMARY KEY (id)
) ENGINE = INNODB;
