CREATE TABLE configuration (
  tenant_id NUMERIC(19, 0) NOT NULL,
  content_type  NVARCHAR(50) NOT NULL,
  resource_name  NVARCHAR(120) NOT NULL,
  resource_content  VARBINARY(MAX) NOT NULL
)
GO
ALTER TABLE configuration ADD CONSTRAINT pk_configuration PRIMARY KEY (tenant_id, content_type, resource_name)
GO