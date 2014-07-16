CREATE TABLE arch_data_instance (
    tenantId NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	name NVARCHAR(50),
	sourceObjectId NUMERIC(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
)
@@

CREATE TABLE arch_data_mapping (
    tenantid NUMERIC(19, 0) NOT NULL,
	id NUMERIC(19, 0) NOT NULL,
	dataName NVARCHAR(50),
	dataInstanceId NUMERIC(19, 0) NOT NULL,
	sourceObjectId NUMERIC(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
)