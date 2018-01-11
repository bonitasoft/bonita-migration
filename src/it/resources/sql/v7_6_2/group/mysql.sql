CREATE TABLE group_ (
   tenantid BIGINT NOT NULL,
   id BIGINT NOT NULL,
   name VARCHAR(125) NOT NULL,
   parentPath VARCHAR(255),
   displayName VARCHAR(255),
   description TEXT,
   createdBy BIGINT,
   creationDate BIGINT,
   lastUpdate BIGINT,
   iconid BIGINT,
   UNIQUE (tenantid, parentPath, name),
   PRIMARY KEY (tenantid, id)
 ) ENGINE = INNODB;
