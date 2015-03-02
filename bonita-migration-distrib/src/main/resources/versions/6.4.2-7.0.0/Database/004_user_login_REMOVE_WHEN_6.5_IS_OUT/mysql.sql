CREATE TABLE user_login (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  lastConnection BIGINT,
  PRIMARY KEY (tenantid, id)
);
INSERT INTO user_login (tenantid, id, lastConnection)
SELECT tenantid, id, lastConnection FROM user_;
ALTER TABLE user_
DROP COLUMN lastConnection;