CREATE TABLE icon (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  mimetype VARCHAR2(255) NOT NULL,
  content BLOB NOT NULL,
  CONSTRAINT pk_icon PRIMARY KEY (tenantId, id)
);
ALTER TABLE user_ add iconid NUMBER(19, 0);
ALTER TABLE group_ add iconid NUMBER(19, 0);
ALTER TABLE role add iconid NUMBER(19, 0);