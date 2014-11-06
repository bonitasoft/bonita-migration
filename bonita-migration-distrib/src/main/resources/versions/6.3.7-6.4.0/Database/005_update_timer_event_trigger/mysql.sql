ALTER TABLE event_trigger_instance ADD eventInstanceName VARCHAR(50);
ALTER TABLE event_trigger_instance ADD executionDate BIGINT;
ALTER TABLE event_trigger_instance ADD jobTriggerName VARCHAR(255);

ALTER TABLE event_trigger_instance DROP COLUMN timerType;
ALTER TABLE event_trigger_instance DROP COLUMN timerValue;