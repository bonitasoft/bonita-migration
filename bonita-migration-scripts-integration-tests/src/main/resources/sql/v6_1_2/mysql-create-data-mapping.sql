CREATE TABLE data_mapping (
    tenantid BIGINT NOT NULL,
	id BIGINT NOT NULL,
	containerId BIGINT,
	containerType VARCHAR(60),
	dataName VARCHAR(50),
	dataInstanceId BIGINT NOT NULL,
	UNIQUE (containerId, containerType, dataName),
	PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;