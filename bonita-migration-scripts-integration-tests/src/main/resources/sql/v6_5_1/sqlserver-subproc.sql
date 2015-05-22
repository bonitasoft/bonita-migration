CREATE TABLE tenant (
  id NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (id)
)@@
CREATE TABLE flownode_instance (
tenantid NUMERIC(19, 0) NOT NULL,
id NUMERIC(19, 0) NOT NULL,
kind NVARCHAR(25) NOT NULL,
stateId INT NOT NULL,
stateName NVARCHAR(50),
terminal BIT NOT NULL,
stable BIT ,
PRIMARY KEY (tenantid, id)
)@@