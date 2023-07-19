CREATE TABLE temporary_content (
  id BIGINT NOT NULL,
  creationDate BIGINT NOT NULL,
  key_ VARCHAR(255) NOT NULL,
  fileName VARCHAR(255) NOT NULL,
  mimeType VARCHAR(255) NOT NULL,
  content LONGBLOB NOT NULL,
  UNIQUE (key_),
  PRIMARY KEY (id)
) ENGINE = INNODB
@@
CREATE INDEX idx_temporary_content ON temporary_content (key_)