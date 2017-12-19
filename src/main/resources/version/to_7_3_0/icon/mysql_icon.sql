CREATE TABLE icon (
  tenantId BIGINT NOT NULL,
  id BIGINT NOT NULL,
  mimetype VARCHAR(255) NOT NULL,
  content LONGBLOB NOT NULL,
  CONSTRAINT pk_icon PRIMARY KEY (tenantId, id)
) ENGINE = INNODB;
ALTER TABLE user_ add iconid BIGINT;
ALTER TABLE group_ add iconid BIGINT;
ALTER TABLE role add iconid BIGINT;