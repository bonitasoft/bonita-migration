ALTER TABLE process_instance CHANGE startedByDelegate startedBySubstitute BIGINT NOT NUL AFTER startedBy;
ALTER TABLE arch_process_instance CHANGE startedByDelegate startedBySubstitute BIGINT NOT NUL AFTER startedBy;

ALTER TABLE flownode_instance CHANGE executedByDelegate executedBySubstitute BIGINT AFTER executedBy;
ALTER TABLE arch_flownode_instance CHANGE executedByDelegate executedBySubstitute BIGINT AFTER executedBy;