ALTER TABLE process_instance MODIFY stringIndex1 VARCHAR2(255 CHAR) @@
ALTER TABLE process_instance MODIFY stringIndex2 VARCHAR2(255 CHAR) @@
ALTER TABLE process_instance MODIFY stringIndex3 VARCHAR2(255 CHAR) @@
ALTER TABLE process_instance MODIFY stringIndex4 VARCHAR2(255 CHAR) @@
ALTER TABLE process_instance MODIFY stringIndex5 VARCHAR2(255 CHAR) @@

ALTER TABLE arch_process_instance MODIFY stringIndex1 VARCHAR2(255 CHAR) @@
ALTER TABLE arch_process_instance MODIFY stringIndex2 VARCHAR2(255 CHAR) @@
ALTER TABLE arch_process_instance MODIFY stringIndex3 VARCHAR2(255 CHAR) @@
ALTER TABLE arch_process_instance MODIFY stringIndex4 VARCHAR2(255 CHAR) @@
ALTER TABLE arch_process_instance MODIFY stringIndex5 VARCHAR2(255 CHAR) @@

ALTER TABLE group_ MODIFY name VARCHAR2(125 CHAR) @@
ALTER TABLE group_ MODIFY displayName VARCHAR2(255 CHAR) @@

ALTER TABLE role MODIFY name VARCHAR2(255 CHAR) @@
ALTER TABLE role MODIFY displayName VARCHAR2(255 CHAR) @@

ALTER TABLE user_ MODIFY delegeeUserName VARCHAR2(255 CHAR) @@
