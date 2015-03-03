--
-- Message_instance
-- 
ALTER TABLE message_instance MODIFY messageName VARCHAR2(255 CHAR) @@
ALTER TABLE message_instance MODIFY targetProcess VARCHAR2(255 CHAR) @@
ALTER TABLE message_instance MODIFY targetFlowNode VARCHAR2(255 CHAR) NULL @@
ALTER TABLE message_instance MODIFY flowNodeName VARCHAR2(255 CHAR) @@
ALTER TABLE message_instance MODIFY correlation1 VARCHAR2(128 CHAR) @@
ALTER TABLE message_instance MODIFY correlation2 VARCHAR2(128 CHAR) @@
ALTER TABLE message_instance MODIFY correlation3 VARCHAR2(128 CHAR) @@
ALTER TABLE message_instance MODIFY correlation4 VARCHAR2(128 CHAR) @@
ALTER TABLE message_instance MODIFY correlation5 VARCHAR2(128 CHAR) @@
