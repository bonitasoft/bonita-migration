CREATE TABLE tenant (
  id NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (id)
)@@
CREATE TABLE flownode_instance (
tenantid NUMBER(19, 0) NOT NULL,
id NUMBER(19, 0) NOT NULL,
kind VARCHAR2(25 CHAR) NOT NULL,
stateId INT NOT NULL,
stateName VARCHAR2(50 CHAR),
terminal NUMBER(1) NOT NULL,
stable NUMBER(1) ,
PRIMARY KEY (tenantid, id)
)@@