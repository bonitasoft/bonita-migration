CREATE TABLE data_mapping (
    tenantId BIGINT NOT NULL,
	id BIGINT NOT NULL,
	containerId BIGINT,
	containerType VARCHAR(60),
	dataName VARCHAR(50),
	dataInstanceId BIGINT NOT NULL,
	UNIQUE (tenantId, containerId, containerType, dataName),
	PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;
