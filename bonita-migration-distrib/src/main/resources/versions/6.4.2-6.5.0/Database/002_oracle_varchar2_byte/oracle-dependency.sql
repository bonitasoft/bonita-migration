--
-- pdependency
-- 

ALTER TABLE pdependency MODIFY name VARCHAR2(50 CHAR) UNIQUE @@
ALTER TABLE pdependency MODIFY description VARCHAR2(1024 CHAR) @@
ALTER TABLE pdependency MODIFY filename VARCHAR2(255 CHAR) @@

--
-- pdependencymapping
-- 
ALTER TABLE pdependencymapping MODIFY artifacttype VARCHAR2(50 CHAR) @@


--
-- dependency
-- 
ALTER TABLE dependency MODIFY name VARCHAR2(150 CHAR) @@
ALTER TABLE dependency MODIFY description VARCHAR2(1024 CHAR) @@
ALTER TABLE dependency MODIFY filename VARCHAR2(255 CHAR) @@


--
-- dependencymapping
-- 
ALTER TABLE dependencymapping MODIFY artifacttype VARCHAR2(50 CHAR) @@
