ALTER TABLE process_instance CHANGE startedBy startedFor AFTER startedByDelegate;
ALTER TABLE process_instance CHANGE startedByDelegate startedBy BEFORE startedFor;

ALTER TABLE flownode_instance CHANGE executedBy executedFor AFTER executedByDelegate;
ALTER TABLE flownode_instance CHANGE executedByDelegate executedBy BEFORE executedFor;