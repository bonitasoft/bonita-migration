CREATE TABLE group_ (
   tenantid INT8 NOT NULL,
   id INT8 NOT NULL,
   name VARCHAR(125) NOT NULL,
   parentPath VARCHAR(255),
   displayName VARCHAR(255),
   description TEXT,
   createdBy INT8,
   creationDate INT8,
   lastUpdate INT8,
   iconid INT8,
   UNIQUE (tenantid, parentPath, name),
   PRIMARY KEY (tenantid, id)
 );
