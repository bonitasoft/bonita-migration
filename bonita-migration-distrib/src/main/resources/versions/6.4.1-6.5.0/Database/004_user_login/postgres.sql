CREATE TABLE user_login (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  lastConnection INT8,
  PRIMARY KEY (tenantid, id)
);
INSERT INTO user_login (tenantid, id, lastConnection)
SELECT tenantid, id, lastConnection FROM user_;
ALTER TABLE user_
DROP COLUMN lastConnection;