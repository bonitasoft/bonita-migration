--
-- theme
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE theme DISABLE UNIQUE (tenantId, isDefault, type) @@
ALTER TABLE theme DROP UNIQUE (tenantId, isDefault, type) @@


ALTER TABLE theme ADD type_temp VARCHAR2(50 CHAR) @@
UPDATE theme SET type_temp = type @@
ALTER TABLE theme DROP COLUMN type @@
ALTER TABLE theme RENAME COLUMN type_temp TO type @@
ALTER TABLE theme MODIFY type NOT NULL @@

ALTER TABLE theme ADD CONSTRAINT UK_Theme UNIQUE (tenantId, isDefault, type) @@
ALTER TABLE theme ENABLE CONSTRAINT UK_Theme @@