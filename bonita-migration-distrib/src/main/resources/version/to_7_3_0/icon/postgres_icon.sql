CREATE TABLE icon (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  mimetype VARCHAR(255) NOT NULL,
  content BYTEA NOT NULL,
  CONSTRAINT pk_icon PRIMARY KEY (tenantId, id)
);
ALTER TABLE user_ add iconid INT8;