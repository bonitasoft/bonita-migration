CREATE TABLE data_mapping (
    tenantid NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	containerId NUMERIC(19, 0),
	containerType NVARCHAR(60),
	dataName NVARCHAR(50),
	dataInstanceId NUMERIC(19, 0) NOT NULL,
	UNIQUE (containerId, containerType, dataName),
	PRIMARY KEY (tenantid, id)
)