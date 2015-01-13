--
-- Category
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE category DISABLE UNIQUE (tenantid, name) @@
ALTER TABLE category DROP UNIQUE (tenantid, name) @@

ALTER TABLE category ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE category SET name_temp = name @@
ALTER TABLE category DROP COLUMN name @@
ALTER TABLE category RENAME COLUMN name_temp TO name @@
ALTER TABLE category MODIFY name NOT NULL @@


ALTER TABLE category ADD description_temp VARCHAR2(1024 CHAR) @@
UPDATE category SET description_temp = description @@
ALTER TABLE category DROP COLUMN description @@
ALTER TABLE category RENAME COLUMN description_temp TO description @@

ALTER TABLE category ADD CONSTRAINT UK_Category UNIQUE (tenantid, name) @@
ALTER TABLE category ENABLE CONSTRAINT UK_Category @@