CREATE TABLE user_login (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  lastConnection NUMERIC(19, 0),
  PRIMARY KEY (tenantid, id)
)
GO
INSERT INTO user_login (tenantid, id, lastConnection)
SELECT tenantid, id, lastConnection FROM user_
GO
ALTER TABLE user_
DROP COLUMN lastConnection
GO