CREATE TABLE arch_data_instance (
    tenantId BIGINT NOT NULL,
	id BIGINT NOT NULL,
	name VARCHAR(50),
	sourceObjectId BIGINT NOT NULL,
	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;

CREATE TABLE arch_data_mapping (
    tenantid BIGINT NOT NULL,
	id BIGINT NOT NULL,
	dataName VARCHAR(50),
	dataInstanceId BIGINT NOT NULL,
	sourceObjectId BIGINT NOT NULL,
	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
