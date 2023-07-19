CREATE TABLE temporary_content (
  id NUMBER(19, 0) NOT NULL,
  creationDate NUMBER(19, 0) NOT NULL,
  key_ VARCHAR2(255) NOT NULL,
  fileName VARCHAR2(255) NOT NULL,
  mimeType VARCHAR2(255) NOT NULL,
  content BLOB NOT NULL,
  UNIQUE (key_),
  PRIMARY KEY (id)
)