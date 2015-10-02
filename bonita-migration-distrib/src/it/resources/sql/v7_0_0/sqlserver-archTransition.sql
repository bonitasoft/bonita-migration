CREATE TABLE tenant (
  id NUMERIC(19, 0) NOT NULL,
  created NUMERIC(19, 0) NOT NULL,
  createdBy NVARCHAR(50) NOT NULL,
  description NVARCHAR(255),
  defaultTenant BIT NOT NULL,
  iconname NVARCHAR(50),
  iconpath NVARCHAR(255),
  name NVARCHAR(50) NOT NULL,
  status NVARCHAR(15) NOT NULL,
  PRIMARY KEY (id)
)
@@

CREATE TABLE arch_transition_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  rootContainerId NUMERIC(19, 0) NOT NULL,
  parentContainerId NUMERIC(19, 0) NOT NULL,
  source NUMERIC(19, 0),
  target NUMERIC(19, 0),
  state NVARCHAR(50),
  terminal BIT NOT NULL,
  stable BIT ,
  stateCategory NVARCHAR(50) NOT NULL,
  logicalGroup1 NUMERIC(19, 0) NOT NULL,
  logicalGroup2 NUMERIC(19, 0) NOT NULL,
  logicalGroup3 NUMERIC(19, 0),
  logicalGroup4 NUMERIC(19, 0) NOT NULL,
  description NVARCHAR(255),
  sourceObjectId NUMERIC(19, 0),
  archiveDate NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
@@

CREATE INDEX idx1_arch_transition_instance ON arch_transition_instance (tenantid, rootcontainerid)
@@

ALTER TABLE arch_transition_instance ADD CONSTRAINT fk_arch_transition_instance_tenantId FOREIGN KEY (tenantid) REFERENCES tenant(id)
@@