CREATE INDEX idx1_arch_transition_instance ON arch_transition_instance (tenantid, rootcontainerid)
@@
CREATE INDEX idx1_arch_process_comment ON arch_process_comment (tenantid, sourceobjectid)
@@
CREATE INDEX idx2_arch_process_comment ON arch_process_comment (tenantid, processinstanceid, archivedate)
@@
CREATE INDEX idx2_arch_data_mapping ON arch_data_mapping (tenantid, containerId, containerType)
@@
