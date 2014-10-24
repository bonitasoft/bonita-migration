CREATE INDEX idx1_arch_transition_instance ON arch_transition_instance (rootContainerId, tenantid);
CREATE INDEX idx1_arch_process_comment ON arch_process_comment (sourceObjectId, tenantid);
CREATE INDEX idx2_arch_process_comment ON arch_process_comment (processInstanceId, archivedate, tenantid);
CREATE INDEX idx2_arch_data_mapping ON arch_data_mapping (containerId, containerType, tenantid);
