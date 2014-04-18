CREATE INDEX idx1_arch_process_instance ON arch_process_instance (sourceObjectId, rootProcessInstanceId, callerId);
CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (containerId, containerType);
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (rootContainerId, parentContainerId);
CREATE INDEX idx1_arch_data_instance ON arch_data_instance (containerId, sourceObjectId);
CREATE INDEX idx1_arch_data_mapping ON arch_data_mapping (containerId, dataInstanceId, sourceObjectId); 
