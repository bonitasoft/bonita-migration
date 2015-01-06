--
-- Process_definition
-- 
-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE process_definition DISABLE UNIQUE (tenantId, name, version) @@
ALTER TABLE process_definition DROP UNIQUE (tenantId, name, version) @@

ALTER TABLE process_definition ADD name_temp VARCHAR2(150 CHAR) @@
UPDATE process_definition SET name_temp = name @@
ALTER TABLE process_definition DROP COLUMN name @@
ALTER TABLE process_definition RENAME COLUMN name_temp TO name @@
ALTER TABLE process_definition MODIFY name NOT NULL @@


ALTER TABLE process_definition ADD version_temp VARCHAR2(50 CHAR) @@
UPDATE process_definition SET version_temp = version @@
ALTER TABLE process_definition DROP COLUMN version @@
ALTER TABLE process_definition RENAME COLUMN version_temp TO version @@
ALTER TABLE process_definition MODIFY version NOT NULL @@


ALTER TABLE process_definition ADD description_temp VARCHAR2(255 CHAR) @@
UPDATE process_definition SET description_temp = description @@
ALTER TABLE process_definition DROP COLUMN description @@
ALTER TABLE process_definition RENAME COLUMN description_temp TO description @@


ALTER TABLE process_definition ADD activationState_temp VARCHAR2(30 CHAR) @@
UPDATE process_definition SET activationState_temp = activationState @@
ALTER TABLE process_definition DROP COLUMN activationState @@
ALTER TABLE process_definition RENAME COLUMN activationState_temp TO activationState @@
ALTER TABLE process_definition MODIFY activationState NOT NULL @@


ALTER TABLE process_definition ADD configurationState_temp VARCHAR2(30 CHAR) @@
UPDATE process_definition SET configurationState_temp = configurationState @@
ALTER TABLE process_definition DROP COLUMN configurationState @@
ALTER TABLE process_definition RENAME COLUMN configurationState_temp TO configurationState @@
ALTER TABLE process_definition MODIFY configurationState NOT NULL @@


ALTER TABLE process_definition ADD displayName_temp VARCHAR2(75 CHAR) @@
UPDATE process_definition SET displayName_temp = displayName @@
ALTER TABLE process_definition DROP COLUMN displayName @@
ALTER TABLE process_definition RENAME COLUMN displayName_temp TO displayName @@


ALTER TABLE process_definition ADD displayDescription_temp VARCHAR2(255 CHAR) @@
UPDATE process_definition SET displayDescription_temp = displayDescription @@
ALTER TABLE process_definition DROP COLUMN displayDescription @@
ALTER TABLE process_definition RENAME COLUMN displayDescription_temp TO displayDescription @@


ALTER TABLE process_definition ADD iconPath_temp VARCHAR2(255 CHAR) @@
UPDATE process_definition SET iconPath_temp = iconPath @@
ALTER TABLE process_definition DROP COLUMN iconPath @@
ALTER TABLE process_definition RENAME COLUMN iconPath_temp TO iconPath @@

ALTER TABLE process_definition ADD CONSTRAINT UK_Process_Definition UNIQUE (tenantId, name, version) @@
ALTER TABLE process_definition ENABLE CONSTRAINT UK_Process_Definition @@
