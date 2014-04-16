ALTER TABLE process_instance ADD startedBySubstitute NUMBER(19,0) DEFAULT 0 NOT NULL @@
UPDATE process_instance SET startedBySubstitute = startedByDelegate @@
ALTER TABLE process_instance DROP COLUMN startedByDelegate @@

ALTER TABLE flownode_instance ADD executedBySubstitute NUMBER(19,0) DEFAULT 0 NOT NULL @@
UPDATE flownode_instance SET executedBySubstitute = executedByDelegate @@
ALTER TABLE flownode_instance DROP COLUMN executedByDelegate @@