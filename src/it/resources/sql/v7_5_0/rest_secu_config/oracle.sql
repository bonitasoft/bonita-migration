CREATE TABLE configuration (
  tenant_id NUMBER(19, 0) NOT NULL,
  content_type VARCHAR2(50 CHAR) NOT NULL,
  resource_name VARCHAR2(120 CHAR) NOT NULL,
  resource_content BLOB NOT NULL
);
ALTER TABLE configuration ADD CONSTRAINT pk_configuration PRIMARY KEY (tenant_id, content_type, resource_name);
CREATE INDEX idx_configuration ON configuration (tenant_id, content_type);
