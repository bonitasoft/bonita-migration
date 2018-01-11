CREATE TABLE group_ (
   tenantid NUMBER(19, 0) NOT NULL,
   id NUMBER(19, 0) NOT NULL,
   name VARCHAR2(125 CHAR) NOT NULL,
   parentPath VARCHAR2(255 CHAR),
   displayName VARCHAR2(255 CHAR),
   description VARCHAR2(1024 CHAR),
   createdBy NUMBER(19, 0),
   creationDate NUMBER(19, 0),
   lastUpdate NUMBER(19, 0),
   iconid NUMBER(19, 0),
   CONSTRAINT UK_Group UNIQUE (tenantid, parentPath, name),
   PRIMARY KEY (tenantid, id)
 );
