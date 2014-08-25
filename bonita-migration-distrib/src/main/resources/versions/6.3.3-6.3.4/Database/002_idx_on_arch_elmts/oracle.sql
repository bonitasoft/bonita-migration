CREATE INDEX idx1_arch_transition_instance_on_tenant_and_rootContainer ON arch_transition_instance (tenantid, rootcontainerid) @@
CREATE INDEX idx1_arch_process_comment_on_tenant_and_sourceObjectId ON arch_process_comment (tenantid, sourceobjectid) @@
CREATE INDEX idx2_arch_process_comment_on_tenant_and_procInst_and_archDate ON arch_process_comment (tenantid, processinstanceid, archivedate) @@
CREATE INDEX idx2_arch_data_mapping_on_tenant_and_contId_and_contType ON arch_data_mapping (tenantid, containerId, containerType) @@
CREATE INDEX idx2_arch_connector_instance_on_tenant_and_contId_and_contType ON arch_connector_instance (tenantid, containerId, containerType) @@