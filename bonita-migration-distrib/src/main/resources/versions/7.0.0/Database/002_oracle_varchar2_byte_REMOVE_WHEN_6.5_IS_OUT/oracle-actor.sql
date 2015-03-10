--
-- ACTOR
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE actor DISABLE UNIQUE (tenantid, id, scopeId, name) @@
ALTER TABLE actor DROP UNIQUE (tenantid, id, scopeId, name) @@

ALTER TABLE actor ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE actor SET name_temp = name @@
ALTER TABLE actor DROP COLUMN name @@
ALTER TABLE actor RENAME COLUMN name_temp TO name @@
ALTER TABLE actor MODIFY name NOT NULL @@


ALTER TABLE actor ADD displayName_temp VARCHAR2(75 CHAR) @@
UPDATE actor SET displayName_temp = displayName @@
ALTER TABLE actor DROP COLUMN displayName @@
ALTER TABLE actor RENAME COLUMN displayName_temp TO displayName @@


ALTER TABLE actor ADD description_temp VARCHAR2(1024 CHAR) @@
UPDATE actor SET description_temp = description @@
ALTER TABLE actor DROP COLUMN description @@
ALTER TABLE actor RENAME COLUMN description_temp TO description @@

ALTER TABLE actor ADD CONSTRAINT UK_Actor UNIQUE (tenantid, id, scopeId, name) @@
ALTER TABLE actor ENABLE CONSTRAINT UK_Actor @@