CREATE TABLE form_mapping (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  process NUMERIC(19, 0) NOT NULL,
  type INT NOT NULL,
  task NVARCHAR(255),
  page_mapping_tenant_id NUMERIC(19, 0) NOT NULL,
  page_mapping_id NUMERIC(19, 0) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NULL,
  lastUpdatedBy NUMERIC(19, 0) NULL,
  PRIMARY KEY (tenantId, id)
)
@@
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
@@
ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id)
@@