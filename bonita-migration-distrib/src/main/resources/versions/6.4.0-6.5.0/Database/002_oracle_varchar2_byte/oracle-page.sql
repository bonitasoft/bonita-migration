--
-- Page
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE page DISABLE UNIQUE (tenantId, name) @@
ALTER TABLE page DROP UNIQUE (tenantId, name) @@


ALTER TABLE page ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE page SET name_temp = name @@
ALTER TABLE page DROP COLUMN name @@
ALTER TABLE page RENAME COLUMN name_temp TO name @@
ALTER TABLE page MODIFY name NOT NULL @@


ALTER TABLE page ADD description_temp VARCHAR2(1024 CHAR) @@
UPDATE page SET description_temp = description @@
ALTER TABLE page DROP COLUMN description @@
ALTER TABLE page RENAME COLUMN description_temp TO description @@


ALTER TABLE page ADD displayName_temp VARCHAR2(255 CHAR) @@
UPDATE page SET displayName_temp = displayName @@
ALTER TABLE page DROP COLUMN displayName @@
ALTER TABLE page RENAME COLUMN displayName_temp TO displayName @@
ALTER TABLE page MODIFY displayName NOT NULL @@


ALTER TABLE page ADD contentName_temp VARCHAR2(50 CHAR) @@
UPDATE page SET contentName_temp = contentName @@
ALTER TABLE page DROP COLUMN contentName @@
ALTER TABLE page RENAME COLUMN contentName_temp TO contentName @@
ALTER TABLE page MODIFY contentName NOT NULL @@

ALTER TABLE page ADD CONSTRAINT UK_Page UNIQUE (tenantId, name) @@
ALTER TABLE page ENABLE CONSTRAINT UK_Page @@