ALTER TABLE process_instance RENAME COLUMN startedByDelegate TO startedBySubstitute;

ALTER TABLE flownode_instance RENAME COLUMN executedByDelegate TO executedBySubstitute;