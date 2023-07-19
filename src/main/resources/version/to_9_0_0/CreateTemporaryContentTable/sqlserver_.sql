CREATE TABLE temporary_content (
  id NUMERIC(19, 0) NOT NULL,
  creationDate NUMERIC(19, 0) NOT NULL,
  key_ NVARCHAR(255) NOT NULL,
  fileName NVARCHAR(255) NOT NULL,
  mimeType NVARCHAR(255) NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  UNIQUE (key_),
  PRIMARY KEY (id)
)
@@
CREATE INDEX idx_temporary_content ON temporary_content (key_)