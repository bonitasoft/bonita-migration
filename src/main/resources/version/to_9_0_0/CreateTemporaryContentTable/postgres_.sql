CREATE TABLE temporary_content (
  id INT8 NOT NULL,
  creationDate INT8 NOT NULL,
  key_ VARCHAR(255) NOT NULL,
  fileName VARCHAR(255) NOT NULL,
  mimeType VARCHAR(255) NOT NULL,
  content OID NOT NULL,
  UNIQUE (key_),
  PRIMARY KEY (id)
)
@@
CREATE INDEX idx_temporary_content ON temporary_content (key_)
