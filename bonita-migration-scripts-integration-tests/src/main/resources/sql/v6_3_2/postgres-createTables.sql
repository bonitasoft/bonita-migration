CREATE TABLE arch_data_instance (
    tenantId INT8 NOT NULL,
	id INT8 NOT NULL,
	name VARCHAR(50),
	sourceObjectId INT8 NOT NULL,
	PRIMARY KEY (tenantid, id)
);

CREATE TABLE arch_data_mapping (
    tenantid INT8 NOT NULL,
	id INT8 NOT NULL,
	dataName VARCHAR(50),
	dataInstanceId INT8 NOT NULL,
	sourceObjectId INT8 NOT NULL,
	PRIMARY KEY (tenantid, id)
);