ALTER TABLE waiting_event ADD progress TINYINT DEFAULT 0
@@
UPDATE waiting_event SET progress = 0
@@
UPDATE waiting_event SET parentProcessInstanceId = rootProcessInstanceId WHERE parentProcessInstanceId = -1
@@
DELETE FROM message_instance WHERE handled = 1
@@