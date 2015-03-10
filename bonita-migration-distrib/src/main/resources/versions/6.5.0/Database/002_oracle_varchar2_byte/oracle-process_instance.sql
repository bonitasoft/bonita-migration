--
-- Process_instance
-- 
ALTER TABLE process_instance MODIFY name VARCHAR2(75 CHAR) @@
ALTER TABLE process_instance MODIFY description VARCHAR2(255 CHAR) @@
ALTER TABLE process_instance MODIFY stateCategory VARCHAR2(50 CHAR) @@
ALTER TABLE process_instance MODIFY callerType VARCHAR2(50 CHAR) @@
ALTER TABLE process_instance MODIFY stringIndex1 VARCHAR2(50 CHAR) @@
ALTER TABLE process_instance MODIFY stringIndex2 VARCHAR2(50 CHAR) @@
ALTER TABLE process_instance MODIFY stringIndex3 VARCHAR2(50 CHAR) @@
ALTER TABLE process_instance MODIFY stringIndex4 VARCHAR2(50 CHAR) @@
ALTER TABLE process_instance MODIFY stringIndex5 VARCHAR2(50 CHAR) @@


--
-- Archived_process_instance
-- 
ALTER TABLE arch_process_instance MODIFY name VARCHAR2(75 CHAR) @@
ALTER TABLE arch_process_instance MODIFY description VARCHAR2(255 CHAR) @@
ALTER TABLE arch_process_instance MODIFY stringIndex1 VARCHAR2(50 CHAR) @@
ALTER TABLE arch_process_instance MODIFY stringIndex2 VARCHAR2(50 CHAR) @@
ALTER TABLE arch_process_instance MODIFY stringIndex3 VARCHAR2(50 CHAR) @@
ALTER TABLE arch_process_instance MODIFY stringIndex4 VARCHAR2(50 CHAR) @@
ALTER TABLE arch_process_instance MODIFY stringIndex5 VARCHAR2(50 CHAR) @@
