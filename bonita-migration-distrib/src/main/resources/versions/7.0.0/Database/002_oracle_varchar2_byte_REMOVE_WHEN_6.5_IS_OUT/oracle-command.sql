--
-- Command
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE command DISABLE UNIQUE (tenantId, name) @@
ALTER TABLE command DROP UNIQUE (tenantId, name) @@

ALTER TABLE command ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE command SET name_temp = name @@
ALTER TABLE command DROP COLUMN name @@
ALTER TABLE command RENAME COLUMN name_temp TO name @@
ALTER TABLE command MODIFY name NOT NULL @@


ALTER TABLE command ADD description_temp VARCHAR2(1024 CHAR) @@
UPDATE command SET description_temp = description @@
ALTER TABLE command DROP COLUMN description @@
ALTER TABLE command RENAME COLUMN description_temp TO description @@


ALTER TABLE command ADD IMPLEMENTATION_temp VARCHAR2(100 CHAR) @@
UPDATE command SET IMPLEMENTATION_temp = IMPLEMENTATION @@
ALTER TABLE command DROP COLUMN IMPLEMENTATION @@
ALTER TABLE command RENAME COLUMN IMPLEMENTATION_temp TO IMPLEMENTATION @@
ALTER TABLE command MODIFY IMPLEMENTATION NOT NULL @@

ALTER TABLE command ADD CONSTRAINT UK_Command UNIQUE (tenantId, name) @@
ALTER TABLE command ENABLE CONSTRAINT UK_Command @@



--
-- Platform_command
-- 

ALTER TABLE platformCommand ADD name_temp VARCHAR2(50 CHAR) UNIQUE @@
UPDATE platformCommand SET name_temp = name @@
ALTER TABLE platformCommand DROP COLUMN name @@
ALTER TABLE platformCommand RENAME COLUMN name_temp TO name @@
ALTER TABLE platformCommand MODIFY name NOT NULL @@


ALTER TABLE platformCommand ADD description_temp VARCHAR2(1024 CHAR) @@
UPDATE platformCommand SET description_temp = description @@
ALTER TABLE platformCommand DROP COLUMN description @@
ALTER TABLE platformCommand RENAME COLUMN description_temp TO description @@


ALTER TABLE platformCommand ADD IMPLEMENTATION_temp VARCHAR2(100 CHAR) @@
UPDATE platformCommand SET IMPLEMENTATION_temp = IMPLEMENTATION @@
ALTER TABLE platformCommand DROP COLUMN IMPLEMENTATION @@
ALTER TABLE platformCommand RENAME COLUMN IMPLEMENTATION_temp TO IMPLEMENTATION @@
ALTER TABLE platformCommand MODIFY IMPLEMENTATION NOT NULL @@