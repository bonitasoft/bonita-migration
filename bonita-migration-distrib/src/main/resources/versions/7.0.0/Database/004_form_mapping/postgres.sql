CREATE TABLE form_mapping (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  process INT8 NOT NULL,
  type INT NOT NULL,
  task VARCHAR(255),
  page_mapping_tenant_id INT8 NOT NULL,
  page_mapping_id INT8 NOT NULL,
  lastUpdateDate INT8 NULL,
  lastUpdatedBy INT8 NULL,
  PRIMARY KEY (tenantId, id)
)
@@
CREATE TABLE page_mapping (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  key_ VARCHAR(255) NOT NULL,
  pageId INT8 NULL,
  url VARCHAR(1024) NULL,
  urladapter VARCHAR(255) NULL,
  lastUpdateDate INT8 NULL,
  lastUpdatedBy INT8 NULL,
  CONSTRAINT UK_page_mapping UNIQUE (tenantId, key_),
  PRIMARY KEY (tenantId, id)
)
@@
ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id)
@@
