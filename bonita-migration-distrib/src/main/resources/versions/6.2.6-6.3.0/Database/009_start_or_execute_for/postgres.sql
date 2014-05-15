ALTER TABLE process_instance RENAME COLUMN startedByDelegate TO startedBySubstitute;
ALTER TABLE arch_process_instance RENAME COLUMN startedByDelegate TO startedBySubstitute;

ALTER TABLE flownode_instance RENAME COLUMN executedByDelegate TO executedBySubstitute;
ALTER TABLE arch_flownode_instance RENAME COLUMN executedByDelegate TO executedBySubstitute;