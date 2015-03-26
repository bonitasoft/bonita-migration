--
-- Event_trigger_instance
-- 
ALTER TABLE event_trigger_instance MODIFY kind VARCHAR2(15 CHAR) @@
ALTER TABLE event_trigger_instance MODIFY eventInstanceName VARCHAR2(50 CHAR) @@
ALTER TABLE event_trigger_instance MODIFY messageName VARCHAR2(255 CHAR) @@
ALTER TABLE event_trigger_instance MODIFY targetProcess VARCHAR2(255 CHAR) @@
ALTER TABLE event_trigger_instance MODIFY targetFlowNode VARCHAR2(255 CHAR) @@
ALTER TABLE event_trigger_instance MODIFY signalName VARCHAR2(255 CHAR) @@
ALTER TABLE event_trigger_instance MODIFY errorCode VARCHAR2(255 CHAR) @@
ALTER TABLE event_trigger_instance MODIFY jobTriggerName VARCHAR2(255 CHAR) @@
