--
-- Event_trigger_instance
-- 

ALTER TABLE event_trigger_instance ADD kind_temp VARCHAR2(15 CHAR) @@
UPDATE event_trigger_instance SET kind_temp = kind @@
ALTER TABLE event_trigger_instance DROP COLUMN kind @@
ALTER TABLE event_trigger_instance RENAME COLUMN kind_temp TO kind @@
ALTER TABLE event_trigger_instance MODIFY kind NOT NULL @@


ALTER TABLE event_trigger_instance ADD eventInstanceName_temp VARCHAR2(50 CHAR) @@
UPDATE event_trigger_instance SET eventInstanceName_temp = eventInstanceName @@
ALTER TABLE event_trigger_instance DROP COLUMN eventInstanceName @@
ALTER TABLE event_trigger_instance RENAME COLUMN eventInstanceName_temp TO eventInstanceName @@


ALTER TABLE event_trigger_instance ADD messageName_temp VARCHAR2(255 CHAR) @@
UPDATE event_trigger_instance SET messageName_temp = messageName @@
ALTER TABLE event_trigger_instance DROP COLUMN messageName @@
ALTER TABLE event_trigger_instance RENAME COLUMN messageName_temp TO messageName @@


ALTER TABLE event_trigger_instance ADD targetProcess_temp VARCHAR2(255 CHAR) @@
UPDATE event_trigger_instance SET targetProcess_temp = targetProcess @@
ALTER TABLE event_trigger_instance DROP COLUMN targetProcess @@
ALTER TABLE event_trigger_instance RENAME COLUMN targetProcess_temp TO targetProcess @@


ALTER TABLE event_trigger_instance ADD targetFlowNode_temp VARCHAR2(255 CHAR) @@
UPDATE event_trigger_instance SET targetFlowNode_temp = targetFlowNode @@
ALTER TABLE event_trigger_instance DROP COLUMN targetFlowNode @@
ALTER TABLE event_trigger_instance RENAME COLUMN targetFlowNode_temp TO targetFlowNode @@


ALTER TABLE event_trigger_instance ADD signalName_temp VARCHAR2(255 CHAR) @@
UPDATE event_trigger_instance SET signalName_temp = signalName @@
ALTER TABLE event_trigger_instance DROP COLUMN signalName @@
ALTER TABLE event_trigger_instance RENAME COLUMN signalName_temp TO signalName @@


ALTER TABLE event_trigger_instance ADD errorCode_temp VARCHAR2(255 CHAR) @@
UPDATE event_trigger_instance SET errorCode_temp = errorCode @@
ALTER TABLE event_trigger_instance DROP COLUMN errorCode @@
ALTER TABLE event_trigger_instance RENAME COLUMN errorCode_temp TO errorCode @@


ALTER TABLE event_trigger_instance ADD jobTriggerName_temp VARCHAR2(255 CHAR) @@
UPDATE event_trigger_instance SET jobTriggerName_temp = jobTriggerName @@
ALTER TABLE event_trigger_instance DROP COLUMN jobTriggerName @@
ALTER TABLE event_trigger_instance RENAME COLUMN jobTriggerName_temp TO jobTriggerName @@