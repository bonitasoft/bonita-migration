ALTER TABLE process_instance CHANGE startedByDelegate startedBySubstitute;
ALTER TABLE arch_process_instance CHANGE startedByDelegate startedBySubstitute;

ALTER TABLE flownode_instance CHANGE executedByDelegate executedBySubstitute;
ALTER TABLE arch_flownode_instance CHANGE executedByDelegate executedBySubstitute;