CREATE TABLE page (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  displayName VARCHAR2(255 CHAR) NOT NULL,
  description VARCHAR2(1024 CHAR),
  installationDate NUMBER(19, 0) NOT NULL,
  installedBy NUMBER(19, 0) NOT NULL,
  provided NUMBER(1),
  lastModificationDate NUMBER(19, 0) NOT NULL,
  lastUpdatedBy NUMBER(19, 0) NOT NULL,
  contentName VARCHAR2(50 CHAR) NOT NULL,
  content BLOB,
  contentType VARCHAR2(50 CHAR),
  processDefinitionId NUMBER(19, 0) NOT NULL
);

CREATE TABLE form_mapping (
  tenantId NUMBER(19, 0) NOT NULL,
id NUMBER(19, 0) NOT NULL,
process NUMBER(19, 0) NOT NULL,
type INT NOT NULL,
task VARCHAR2(255 CHAR),
page_mapping_tenant_id NUMBER(19, 0),
page_mapping_id NUMBER(19, 0),
lastUpdateDate NUMBER(19, 0),
lastUpdatedBy NUMBER(19, 0),
target VARCHAR2(16 CHAR) NOT NULL,
PRIMARY KEY (tenantId, id)
);

CREATE TABLE page_mapping (
  tenantId NUMBER(19, 0) NOT NULL,
id NUMBER(19, 0) NOT NULL,
key_ VARCHAR2(255 CHAR) NOT NULL,
pageId NUMBER(19, 0) NULL,
url VARCHAR2(1024 CHAR) NULL,
urladapter VARCHAR2(255 CHAR) NULL,
page_authoriz_rules VARCHAR2(1024 CHAR) NULL,
lastUpdateDate NUMBER(19, 0) NULL,
lastUpdatedBy NUMBER(19, 0) NULL,
CONSTRAINT UK_page_mapping UNIQUE (tenantId, key_),
PRIMARY KEY (tenantId, id)
);

CREATE TABLE process_definition (
  tenantId NUMBER(19, 0) NOT NULL,
id NUMBER(19, 0) NOT NULL,
processId NUMBER(19, 0) NOT NULL,
name VARCHAR2(150 CHAR) NOT NULL,
version VARCHAR2(50 CHAR) NOT NULL,
description VARCHAR2(255 CHAR),
deploymentDate NUMBER(19, 0) NOT NULL,
deployedBy NUMBER(19, 0) NOT NULL,
activationState VARCHAR2(30 CHAR) NOT NULL,
configurationState VARCHAR2(30 CHAR) NOT NULL,
displayName VARCHAR2(75 CHAR),
displayDescription VARCHAR2(255 CHAR),
lastUpdateDate NUMBER(19, 0),
categoryId NUMBER(19, 0),
iconPath VARCHAR2(255 CHAR),
content_tenantid NUMBER(19, 0) NOT NULL,
content_id NUMBER(19, 0) NOT NULL,
PRIMARY KEY (tenantId, id)
);

CREATE TABLE process_instance (
  tenantid NUMBER(19, 0) NOT NULL,
id NUMBER(19, 0) NOT NULL,
name VARCHAR2(75 CHAR) NOT NULL,
processDefinitionId NUMBER(19, 0) NOT NULL,
description VARCHAR2(255 CHAR),
startDate NUMBER(19, 0) NOT NULL,
startedBy NUMBER(19, 0) NOT NULL,
startedBySubstitute NUMBER(19, 0) NOT NULL,
endDate NUMBER(19, 0) NOT NULL,
stateId INT NOT NULL,
stateCategory VARCHAR2(50 CHAR) NOT NULL,
lastUpdate NUMBER(19, 0) NOT NULL,
containerId NUMBER(19, 0),
rootProcessInstanceId NUMBER(19, 0),
callerId NUMBER(19, 0),
callerType VARCHAR2(50 CHAR),
interruptingEventId NUMBER(19, 0),
stringIndex1 VARCHAR2(255 CHAR),
stringIndex2 VARCHAR2(255 CHAR),
stringIndex3 VARCHAR2(255 CHAR),
stringIndex4 VARCHAR2(255 CHAR),
stringIndex5 VARCHAR2(255 CHAR),
PRIMARY KEY (tenantid, id)
);
ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id);

CREATE TABLE bar_resource (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  process_id NUMBER(19, 0) NOT NULL,
  name VARCHAR2(255) NOT NULL,
  type VARCHAR2(16) NOT NULL,
  content BLOB NOT NULL,
  UNIQUE (tenantId, process_id, name, type),
  PRIMARY KEY (tenantId, id)
);
