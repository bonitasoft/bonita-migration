CREATE TABLE configuration (
  content_type VARCHAR2(50 CHAR) NOT NULL,
  resource_name VARCHAR2(120 CHAR) NOT NULL,
  resource_content BLOB NOT NULL,
  CONSTRAINT pk_configuration PRIMARY KEY (content_type, resource_name)
);
CREATE INDEX idx_configuration ON configuration (content_type);
