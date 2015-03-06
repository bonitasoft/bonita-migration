ALTER TABLE waiting_event ADD progress TINYINT(4) DEFAULT 0;
UPDATE waiting_event SET parentProcessInstanceId = rootProcessInstanceId WHERE parentProcessInstanceId = -1;
DELETE FROM message_instance WHERE handled = 1;