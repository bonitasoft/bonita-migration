CREATE TABLE configuration (
  tenant_id INT8 NOT NULL,
  content_type VARCHAR(50) NOT NULL,
  resource_name VARCHAR(120) NOT NULL,
  resource_content BYTEA NOT NULL
);
