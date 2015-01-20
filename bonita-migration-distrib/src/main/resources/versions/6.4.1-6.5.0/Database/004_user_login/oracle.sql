CREATE TABLE user_login (
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  lastConnection NUMBER(19, 0),
  PRIMARY KEY (tenantid, id)
)@@
INSERT INTO user_login (tenantid, id, lastConnection)
SELECT tenantid, id, lastConnection FROM user_@@
ALTER TABLE user_
DROP COLUMN lastConnection@@