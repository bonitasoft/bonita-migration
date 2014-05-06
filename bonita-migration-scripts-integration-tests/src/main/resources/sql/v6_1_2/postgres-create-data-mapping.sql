CREATE TABLE data_mapping (
    tenantid INT8 NOT NULL,
	id INT8 NOT NULL,
	containerId INT8,
	containerType VARCHAR(60),
	dataName VARCHAR(50),
	dataInstanceId INT8 NOT NULL,
	UNIQUE (containerId, containerType, dataName),
	PRIMARY KEY (tenantid, id)
);
