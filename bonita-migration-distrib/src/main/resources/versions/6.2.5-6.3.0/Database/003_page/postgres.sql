CREATE TABLE page (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  name VARCHAR(50) NOT NULL,
  displayName VARCHAR(255) NOT NULL,
  description TEXT,
  installationDate INT8 NOT NULL,
  installedBy INT8 NOT NULL,
  provided BOOLEAN,
  lastModificationDate INT8 NOT NULL,
  content BYTEA,
  UNIQUE (tenantId, name),
  PRIMARY KEY (tenantId, id)
);
INSERT INTO sequence (tenantid, id, nextid)
	SELECT ID, 10120, 1 FROM tenant
	ORDER BY id ASC;
ALTER TABLE profileentry ADD custom BOOLEAN DEFAULT FALSE;