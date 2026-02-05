CREATE TABLE configuration (
  content_type VARCHAR(50) NOT NULL,
  resource_name VARCHAR(120) NOT NULL,
  resource_content BLOB,
  CONSTRAINT pk_configuration PRIMARY KEY (content_type, resource_name)
) ENGINE = INNODB;
CREATE INDEX idx_configuration ON configuration (content_type);
