ALTER TABLE process_instance ADD startedBySubstitute NUMBER(19,0) DEFAULT 0 NOT NULL @@
UPDATE process_instance SET startedBySubstitute = startedByDelegate @@
ALTER TABLE process_instance DROP COLUMN startedByDelegate @@

ALTER TABLE arch_process_instance ADD startedBySubstitute NUMBER(19,0) DEFAULT 0 NOT NULL @@
UPDATE arch_process_instance SET startedBySubstitute = startedByDelegate @@
ALTER TABLE arch_process_instance DROP COLUMN startedByDelegate @@

ALTER TABLE flownode_instance ADD executedBySubstitute NUMBER(19,0) DEFAULT 0 NOT NULL @@
UPDATE flownode_instance SET executedBySubstitute = executedByDelegate @@
ALTER TABLE flownode_instance DROP COLUMN executedByDelegate @@

ALTER TABLE arch_flownode_instance ADD executedBySubstitute NUMBER(19,0) DEFAULT 0 NOT NULL @@
UPDATE arch_flownode_instance SET executedBySubstitute = executedByDelegate @@
ALTER TABLE arch_flownode_instance DROP COLUMN executedByDelegate @@