
CREATE TABLE form_mapping (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  process NUMBER(19, 0) NOT NULL,
  type INT NOT NULL,
  task VARCHAR2(255 CHAR),
  page_mapping_tenant_id NUMBER(19, 0) NULL,
  page_mapping_id NUMBER(19, 0) NULL,
  lastUpdateDate NUMBER(19, 0) NULL,
  lastUpdatedBy NUMBER(19, 0) NULL,
  PRIMARY KEY (tenantId, id)
)
@@
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
)
@@
ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id)
@@
