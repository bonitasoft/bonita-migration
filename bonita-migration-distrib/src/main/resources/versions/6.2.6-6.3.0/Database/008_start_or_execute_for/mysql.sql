ALTER TABLE process_instance CHANGE startedByDelegate BySubstitute AFTER startedBy;

ALTER TABLE flownode_instance CHANGE executedByDelegate BySubstitute AFTER executedBy;