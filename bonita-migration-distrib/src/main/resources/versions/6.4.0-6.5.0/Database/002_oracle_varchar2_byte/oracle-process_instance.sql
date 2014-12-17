--
-- Process_instance
-- 

ALTER TABLE process_instance ADD name_temp VARCHAR2(75 CHAR) @@
UPDATE process_instance SET name_temp = name @@
ALTER TABLE process_instance DROP COLUMN name @@
ALTER TABLE process_instance RENAME COLUMN name_temp TO name @@
ALTER TABLE process_instance MODIFY name NOT NULL @@


ALTER TABLE process_instance ADD description_temp VARCHAR2(255 CHAR) @@
UPDATE process_instance SET description_temp = description @@
ALTER TABLE process_instance DROP COLUMN description @@
ALTER TABLE process_instance RENAME COLUMN description_temp TO description @@


ALTER TABLE process_instance ADD stateCategory_temp VARCHAR2(50 CHAR) @@
UPDATE process_instance SET stateCategory_temp = stateCategory @@
ALTER TABLE process_instance DROP COLUMN stateCategory @@
ALTER TABLE process_instance RENAME COLUMN stateCategory_temp TO stateCategory @@
ALTER TABLE process_instance MODIFY stateCategory NOT NULL @@


ALTER TABLE process_instance ADD callerType_temp VARCHAR2(50 CHAR) @@
UPDATE process_instance SET callerType_temp = callerType @@
ALTER TABLE process_instance DROP COLUMN callerType @@
ALTER TABLE process_instance RENAME COLUMN callerType_temp TO callerType @@


ALTER TABLE process_instance ADD stringIndex1_temp VARCHAR2(50 CHAR) @@
UPDATE process_instance SET stringIndex1_temp = stringIndex1 @@
ALTER TABLE process_instance DROP COLUMN stringIndex1 @@
ALTER TABLE process_instance RENAME COLUMN stringIndex1_temp TO stringIndex1 @@


ALTER TABLE process_instance ADD stringIndex2_temp VARCHAR2(50 CHAR) @@
UPDATE process_instance SET stringIndex2_temp = stringIndex2 @@
ALTER TABLE process_instance DROP COLUMN stringIndex2 @@
ALTER TABLE process_instance RENAME COLUMN stringIndex2_temp TO stringIndex2 @@


ALTER TABLE process_instance ADD stringIndex3_temp VARCHAR2(50 CHAR) @@
UPDATE process_instance SET stringIndex3_temp = stringIndex3 @@
ALTER TABLE process_instance DROP COLUMN stringIndex3 @@
ALTER TABLE process_instance RENAME COLUMN stringIndex3_temp TO stringIndex3 @@


ALTER TABLE process_instance ADD stringIndex4_temp VARCHAR2(50 CHAR) @@
UPDATE process_instance SET stringIndex4_temp = stringIndex4 @@
ALTER TABLE process_instance DROP COLUMN stringIndex4 @@
ALTER TABLE process_instance RENAME COLUMN stringIndex4_temp TO stringIndex4 @@


ALTER TABLE process_instance ADD stringIndex5_temp VARCHAR2(50 CHAR) @@
UPDATE process_instance SET stringIndex5_temp = stringIndex5 @@
ALTER TABLE process_instance DROP COLUMN stringIndex5 @@
ALTER TABLE process_instance RENAME COLUMN stringIndex5_temp TO stringIndex5 @@



--
-- Archived_process_instance
-- 

ALTER TABLE arch_process_instance ADD name_temp VARCHAR2(75 CHAR) @@
UPDATE arch_process_instance SET name_temp = name @@
ALTER TABLE arch_process_instance DROP COLUMN name @@
ALTER TABLE arch_process_instance RENAME COLUMN name_temp TO name @@
ALTER TABLE arch_process_instance MODIFY name NOT NULL @@


ALTER TABLE arch_process_instance ADD description_temp VARCHAR2(255 CHAR) @@
UPDATE arch_process_instance SET description_temp = description @@
ALTER TABLE arch_process_instance DROP COLUMN description @@
ALTER TABLE arch_process_instance RENAME COLUMN description_temp TO description @@

ALTER TABLE arch_process_instance MODIFY startedBySubstitute NOT NULL @@

ALTER TABLE arch_process_instance ADD stringIndex1_temp VARCHAR2(50 CHAR) @@
UPDATE arch_process_instance SET stringIndex1_temp = stringIndex1 @@
ALTER TABLE arch_process_instance DROP COLUMN stringIndex1 @@
ALTER TABLE arch_process_instance RENAME COLUMN stringIndex1_temp TO stringIndex1 @@


ALTER TABLE arch_process_instance ADD stringIndex2_temp VARCHAR2(50 CHAR) @@
UPDATE arch_process_instance SET stringIndex2_temp = stringIndex2 @@
ALTER TABLE arch_process_instance DROP COLUMN stringIndex2 @@
ALTER TABLE arch_process_instance RENAME COLUMN stringIndex2_temp TO stringIndex2 @@


ALTER TABLE arch_process_instance ADD stringIndex3_temp VARCHAR2(50 CHAR) @@
UPDATE arch_process_instance SET stringIndex3_temp = stringIndex3 @@
ALTER TABLE arch_process_instance DROP COLUMN stringIndex3 @@
ALTER TABLE arch_process_instance RENAME COLUMN stringIndex3_temp TO stringIndex3 @@


ALTER TABLE arch_process_instance ADD stringIndex4_temp VARCHAR2(50 CHAR) @@
UPDATE arch_process_instance SET stringIndex4_temp = stringIndex4 @@
ALTER TABLE arch_process_instance DROP COLUMN stringIndex4 @@
ALTER TABLE arch_process_instance RENAME COLUMN stringIndex4_temp TO stringIndex4 @@


ALTER TABLE arch_process_instance ADD stringIndex5_temp VARCHAR2(50 CHAR) @@
UPDATE arch_process_instance SET stringIndex5_temp = stringIndex5 @@
ALTER TABLE arch_process_instance DROP COLUMN stringIndex5 @@
ALTER TABLE arch_process_instance RENAME COLUMN stringIndex5_temp TO stringIndex5 @@

