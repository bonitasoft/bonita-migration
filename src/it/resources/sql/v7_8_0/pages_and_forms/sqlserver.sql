CREATE TABLE page (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(50) NOT NULL,
  displayName NVARCHAR(255) NOT NULL,
  description NVARCHAR(MAX),
  installationDate NUMERIC(19, 0) NOT NULL,
  installedBy NUMERIC(19, 0) NOT NULL,
  provided BIT,
  lastModificationDate NUMERIC(19, 0) NOT NULL,
  lastUpdatedBy NUMERIC(19, 0) NOT NULL,
  contentName NVARCHAR(50) NOT NULL,
  content VARBINARY(MAX),
  contentType NVARCHAR(50) NOT NULL,
  processDefinitionId NUMERIC(19,0) NOT NULL
)
GO

CREATE TABLE form_mapping (
  tenantId NUMERIC(19, 0) NOT NULL,
id NUMERIC(19, 0) NOT NULL,
process NUMERIC(19, 0) NOT NULL,
type INT NOT NULL,
task NVARCHAR(255),
page_mapping_tenant_id NUMERIC(19, 0),
page_mapping_id NUMERIC(19, 0),
lastUpdateDate NUMERIC(19, 0),
lastUpdatedBy NUMERIC(19, 0),
target NVARCHAR(16) NOT NULL,
PRIMARY KEY (tenantId, id)
)
GO

CREATE TABLE page_mapping (
  tenantId NUMERIC(19, 0) NOT NULL,
id NUMERIC(19, 0) NOT NULL,
key_ NVARCHAR(255) NOT NULL,
pageId NUMERIC(19, 0) NULL,
url NVARCHAR(1024) NULL,
urladapter NVARCHAR(255) NULL,
page_authoriz_rules NVARCHAR(MAX) NULL,
lastUpdateDate NUMERIC(19, 0) NULL,
lastUpdatedBy NUMERIC(19, 0) NULL,
CONSTRAINT UK_page_mapping UNIQUE (tenantId, key_),
PRIMARY KEY (tenantId, id)
)
GO

ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id)
GO

CREATE TABLE process_definition (
  tenantid NUMERIC(19, 0) NOT NULL,
id NUMERIC(19, 0) NOT NULL,
processId NUMERIC(19, 0) NOT NULL,
name NVARCHAR(150) NOT NULL,
version NVARCHAR(50) NOT NULL,
description NVARCHAR(255),
deploymentDate NUMERIC(19, 0) NOT NULL,
deployedBy NUMERIC(19, 0) NOT NULL,
activationState NVARCHAR(30) NOT NULL,
configurationState NVARCHAR(30) NOT NULL,
displayName NVARCHAR(75),
displayDescription NVARCHAR(255),
lastUpdateDate NUMERIC(19, 0),
categoryId NUMERIC(19, 0),
iconPath NVARCHAR(255),
content_tenantid NUMERIC(19, 0) NOT NULL,
content_id NUMERIC(19, 0) NOT NULL,
PRIMARY KEY (tenantid, id),
UNIQUE (tenantid, name, version)
)
GO

CREATE TABLE process_instance (
  tenantid NUMERIC(19, 0) NOT NULL,
id NUMERIC(19, 0) NOT NULL,
name NVARCHAR(75) NOT NULL,
processDefinitionId NUMERIC(19, 0) NOT NULL,
description NVARCHAR(255),
startDate NUMERIC(19, 0) NOT NULL,
startedBy NUMERIC(19, 0) NOT NULL,
startedBySubstitute NUMERIC(19, 0) NOT NULL,
endDate NUMERIC(19, 0) NOT NULL,
stateId INT NOT NULL,
stateCategory NVARCHAR(50) NOT NULL,
lastUpdate NUMERIC(19, 0) NOT NULL,
containerId NUMERIC(19, 0),
rootProcessInstanceId NUMERIC(19, 0),
callerId NUMERIC(19, 0),
callerType NVARCHAR(50),
interruptingEventId NUMERIC(19, 0),
stringIndex1 NVARCHAR(255),
stringIndex2 NVARCHAR(255),
stringIndex3 NVARCHAR(255),
stringIndex4 NVARCHAR(255),
stringIndex5 NVARCHAR(255),
PRIMARY KEY (tenantid, id)
)
GO

CREATE TABLE bar_resource (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  process_id NUMERIC(19, 0) NOT NULL,
  name NVARCHAR(255) NOT NULL,
  type NVARCHAR(16) NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  UNIQUE (tenantId, process_id, name, type),
  PRIMARY KEY (tenantId, id)
)
GO
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
GO