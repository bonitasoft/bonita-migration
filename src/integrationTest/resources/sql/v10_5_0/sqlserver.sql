CREATE TABLE configuration (
  content_type  NVARCHAR(50) NOT NULL,
  resource_name  NVARCHAR(120) NOT NULL,
  resource_content  VARBINARY(MAX) NOT NULL,
  CONSTRAINT pk_configuration PRIMARY KEY (content_type, resource_name)
);
CREATE INDEX idx_configuration ON configuration (content_type);
