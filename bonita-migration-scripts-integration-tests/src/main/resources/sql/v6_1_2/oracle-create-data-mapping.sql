CREATE TABLE data_mapping (
    tenantid NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	containerId NUMBER(19, 0),
	containerType VARCHAR2(60),
	dataName VARCHAR2(50),
	dataInstanceId NUMBER(19, 0) NOT NULL,
	UNIQUE (containerId, containerType, dataName),
	PRIMARY KEY (tenantid, id)
)