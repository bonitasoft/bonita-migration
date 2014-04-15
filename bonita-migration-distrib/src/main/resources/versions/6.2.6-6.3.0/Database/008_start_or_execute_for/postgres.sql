ALTER TABLE process_instance RENAME COLUMN startedBy TO startedFor;
ALTER TABLE process_instance RENAME COLUMN startedByDelegate TO startedBy;

ALTER TABLE flownode_instance RENAME COLUMN executedBy TO executedFor;
ALTER TABLE flownode_instance RENAME COLUMN executedByDelegate TO executedBy;