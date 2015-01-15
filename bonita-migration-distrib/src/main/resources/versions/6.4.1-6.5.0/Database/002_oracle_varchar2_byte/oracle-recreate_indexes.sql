--
-- Recreate all indexes deleted by the migration (except the data mapping because the table will be deleted
-- 
CREATE INDEX idx_app_token ON business_app (token, tenantid)@@
CREATE INDEX idx_app_page_token ON business_app_page (applicationId, token, tenantid)@@
CREATE INDEX idx_dependency_name ON dependency (name)@@
CREATE INDEX idx_fn_lg2_state_tenant_del ON flownode_instance (logicalGroup2, stateName, tenantid, deleted)@@
CREATE INDEX idx_ci_container_activation ON connector_instance (tenantid, containerId, containerType, activationEvent)@@
CREATE INDEX idx_waiting_event ON waiting_event (progress, tenantid, kind, locked, active)@@
CREATE INDEX idx_message_instance ON message_instance (messageName, targetProcess, correlation1, correlation2, correlation3)@@
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(kind, logicalGroup2, executedBy)@@
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind)@@
CREATE INDEX idx1_arch_connector_instance ON arch_connector_instance (tenantId, containerId, containerType)@@