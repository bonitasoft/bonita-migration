--
-- pdependency
-- 

ALTER TABLE pdependency ADD name_temp VARCHAR2(50 CHAR) UNIQUE @@
UPDATE pdependency SET name_temp = name @@
ALTER TABLE pdependency DROP COLUMN name @@
ALTER TABLE pdependency RENAME COLUMN name_temp TO name @@
ALTER TABLE pdependency MODIFY name NOT NULL @@


ALTER TABLE pdependency ADD description_temp VARCHAR2(1024 CHAR) @@
UPDATE pdependency SET description_temp = description @@
ALTER TABLE pdependency DROP COLUMN description @@
ALTER TABLE pdependency RENAME COLUMN description_temp TO description @@


ALTER TABLE pdependency ADD filename_temp VARCHAR2(255 CHAR) @@
UPDATE pdependency SET filename_temp = filename @@
ALTER TABLE pdependency DROP COLUMN filename @@
ALTER TABLE pdependency RENAME COLUMN filename_temp TO filename @@
ALTER TABLE pdependency MODIFY filename NOT NULL @@


--
-- pdependencymapping
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE pdependencymapping DISABLE UNIQUE (dependencyid, artifactid, artifacttype) @@
ALTER TABLE pdependencymapping DROP UNIQUE (dependencyid, artifactid, artifacttype) @@

ALTER TABLE pdependencymapping ADD artifacttype_temp VARCHAR2(50 CHAR) @@
UPDATE pdependencymapping SET artifacttype_temp = artifacttype @@
ALTER TABLE pdependencymapping DROP COLUMN artifacttype @@
ALTER TABLE pdependencymapping RENAME COLUMN artifacttype_temp TO artifacttype @@
ALTER TABLE pdependencymapping MODIFY artifacttype NOT NULL @@

ALTER TABLE pdependencymapping ADD CONSTRAINT UK_PDependency_Mapping UNIQUE (dependencyid, artifactid, artifacttype) @@
ALTER TABLE pdependencymapping ENABLE CONSTRAINT UK_PDependency_Mapping @@



--
-- dependency
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE dependency DISABLE UNIQUE (tenantId, name) @@
ALTER TABLE dependency DROP UNIQUE (tenantId, name) @@

ALTER TABLE dependency ADD name_temp VARCHAR2(150 CHAR) @@
UPDATE dependency SET name_temp = name @@
ALTER TABLE dependency DROP COLUMN name @@
ALTER TABLE dependency RENAME COLUMN name_temp TO name @@
ALTER TABLE dependency MODIFY name NOT NULL @@


ALTER TABLE dependency ADD description_temp VARCHAR2(1024 CHAR) @@
UPDATE dependency SET description_temp = description @@
ALTER TABLE dependency DROP COLUMN description @@
ALTER TABLE dependency RENAME COLUMN description_temp TO description @@


ALTER TABLE dependency ADD filename_temp VARCHAR2(255 CHAR) @@
UPDATE dependency SET filename_temp = filename @@
ALTER TABLE dependency DROP COLUMN filename @@
ALTER TABLE dependency RENAME COLUMN filename_temp TO filename @@
ALTER TABLE dependency MODIFY filename NOT NULL @@

ALTER TABLE dependency ADD CONSTRAINT UK_Dependency UNIQUE (tenantId, name) @@
ALTER TABLE dependency ENABLE CONSTRAINT UK_Dependency @@



--
-- dependencymapping
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE dependencymapping DISABLE UNIQUE (tenantid, dependencyid, artifactid, artifacttype) @@
ALTER TABLE dependencymapping DROP UNIQUE (tenantid, dependencyid, artifactid, artifacttype) @@

ALTER TABLE dependencymapping ADD artifacttype_temp VARCHAR2(50 CHAR) @@
UPDATE dependencymapping SET artifacttype_temp = artifacttype @@
ALTER TABLE dependencymapping DROP COLUMN artifacttype @@
ALTER TABLE dependencymapping RENAME COLUMN artifacttype_temp TO artifacttype @@
ALTER TABLE dependencymapping MODIFY artifacttype NOT NULL @@

ALTER TABLE dependencymapping ADD CONSTRAINT UK_Dependency_Mapping UNIQUE (tenantid, dependencyid, artifactid, artifacttype) @@
ALTER TABLE dependencymapping ENABLE CONSTRAINT UK_Dependency_Mapping @@