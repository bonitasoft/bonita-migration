ALTER TABLE process_instance ADD startedFor NUMBER(19,0) DEFAULT 0 NOT NULL @@
UPDATE process_instance SET startedFor = startedBy @@
UPDATE process_instance SET startedBy = startedByDelegate @@
ALTER TABLE process_instance DROP COLUMN startedByDelegate @@

ALTER TABLE flownode_instance ADD executedFor NUMBER(19,0) DEFAULT 0 NOT NULL @@
UPDATE flownode_instance SET executedFor = executedBy @@
UPDATE flownode_instance SET executedBy = executedByDelegate @@
ALTER TABLE flownode_instance DROP COLUMN executedByDelegate @@