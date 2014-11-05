ALTER TABLE event_trigger_instance ADD eventInstanceName VARCHAR2(50)
@@
ALTER TABLE event_trigger_instance ADD executionDate NUMBER(19, 0)
@@
ALTER TABLE event_trigger_instance ADD jobTriggerName VARCHAR2(255)
@@

ALTER TABLE event_trigger_instance DROP COLUMN timerType
@@
ALTER TABLE event_trigger_instance DROP COLUMN timerValue
@@