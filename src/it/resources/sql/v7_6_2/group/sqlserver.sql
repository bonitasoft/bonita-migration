CREATE TABLE group_ (
   tenantid NUMERIC(19, 0) NOT NULL,
   id NUMERIC(19, 0) NOT NULL,
   name NVARCHAR(125) NOT NULL,
   parentPath NVARCHAR(255),
   displayName NVARCHAR(255),
   description NVARCHAR(MAX),
   createdBy NUMERIC(19, 0),
   creationDate NUMERIC(19, 0),
   lastUpdate NUMERIC(19, 0),
   iconid NUMERIC(19, 0),
   UNIQUE (tenantid, parentPath, name),
   PRIMARY KEY (tenantid, id)
 )
 GO
