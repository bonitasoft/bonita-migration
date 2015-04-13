CREATE TABLE form_mapping (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  process BIGINT NOT NULL,
  type INT NOT NULL,
  task VARCHAR(255),
  page_mapping_tenant_id BIGINT NOT NULL,
  page_mapping_id BIGINT NOT NULL,
  lastUpdateDate BIGINT NULL,
  lastUpdatedBy BIGINT NULL,
  PRIMARY KEY (tenantId, id)
) ENGINE = INNODB
@@
CREATE TABLE page_mapping (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  key_ VARCHAR(255) NOT NULL,
  pageId BIGINT NULL,
  url VARCHAR(1024) NULL,
  urladapter VARCHAR(255) NULL,
  lastUpdateDate BIGINT NULL,
  lastUpdatedBy BIGINT NULL,
  PRIMARY KEY (tenantId, id),
  UNIQUE (tenantId, key_)
) ENGINE = INNODB
@@
ALTER TABLE form_mapping ADD CONSTRAINT fk_form_mapping_key FOREIGN KEY (page_mapping_tenant_id, page_mapping_id) REFERENCES page_mapping(tenantId, id)
@@
