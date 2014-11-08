ALTER TABLE event_trigger_instance ADD eventInstanceName NVARCHAR(50)
@@
ALTER TABLE event_trigger_instance ADD executionDate NUMERIC(19, 0)
@@
ALTER TABLE event_trigger_instance ADD jobTriggerName NVARCHAR(255)
@@

ALTER TABLE event_trigger_instance DROP COLUMN timerType
@@
ALTER TABLE event_trigger_instance DROP COLUMN timerValue
@@