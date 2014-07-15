CREATE TABLE arch_data_instance (
    tenantId NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	name VARCHAR2(50),
	sourceObjectId NUMBER(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
)@@
CREATE TABLE arch_data_mapping (
    tenantid NUMBER(19, 0) NOT NULL,
	id NUMBER(19, 0) NOT NULL,
	dataName VARCHAR2(50),
	dataInstanceId NUMBER(19, 0) NOT NULL,
	sourceObjectId NUMBER(19, 0) NOT NULL,
	PRIMARY KEY (tenantid, id)
)
