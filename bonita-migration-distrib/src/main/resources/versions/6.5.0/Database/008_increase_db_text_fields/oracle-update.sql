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

ALTER TABLE user_ DROP COLUMN delegeeUserName @@

ALTER TABLE user_contactinfo MODIFY city VARCHAR2(255 CHAR) @@
ALTER TABLE user_contactinfo MODIFY state VARCHAR2(255 CHAR) @@
ALTER TABLE user_contactinfo MODIFY country VARCHAR2(255 CHAR) @@
ALTER TABLE user_contactinfo MODIFY website VARCHAR2(255 CHAR) @@

ALTER TABLE flownode_instance MODIFY name VARCHAR2(255 CHAR) @@
ALTER TABLE flownode_instance MODIFY displayName VARCHAR2(255 CHAR) @@
ALTER TABLE arch_flownode_instance MODIFY name VARCHAR2(255 CHAR) @@
ALTER TABLE arch_flownode_instance MODIFY displayName VARCHAR2(255 CHAR) @@
ALTER TABLE arch_transition_instance DROP COLUMN name @@
