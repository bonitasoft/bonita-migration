CREATE INDEX idx1_arch_process_instance ON arch_process_instance (tenantId,sourceObjectId, rootProcessInstanceId, callerId) @@ 
CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId,containerId, containerType) @@
CREATE INDEX idx1_arch_flownode_instance ON arch_flownode_instance (tenantId,rootContainerId, parentContainerId) @@
CREATE INDEX idx1_arch_data_instance ON arch_data_instance (tenantId,containerId, sourceObjectId) @@
CREATE INDEX idx1_arch_data_mapping ON arch_data_mapping (tenantId,containerId, dataInstanceId, sourceObjectId) @@ 
