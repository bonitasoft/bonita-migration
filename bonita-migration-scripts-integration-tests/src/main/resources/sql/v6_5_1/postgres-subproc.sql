CREATE TABLE tenant (
  id INT8 NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE flownode_instance (
tenantid INT8 NOT NULL,
id INT8 NOT NULL,
kind VARCHAR(25) NOT NULL,
stateId INT NOT NULL,
stateName VARCHAR(50),
terminal BOOLEAN NOT NULL,
stable BOOLEAN,
PRIMARY KEY (tenantid, id)
);
