ALTER TABLE waiting_event ADD progress INT2 DEFAULT 0;
UPDATE waiting_event SET parentProcessInstanceId = rootProcessInstanceId WHERE parentProcessInstanceId = -1;
DELETE FROM message_instance WHERE handled = true;