DELETE FROM flownode_instance WHERE deleted = TRUE@@
DROP INDEX idx_fn_lg2_state_tenant_del@@
CREATE INDEX idx_fn_lg2_state_tenant_del ON flownode_instance (logicalGroup2, stateName, tenantid)@@
ALTER TABLE flownode_instance DROP COLUMN deleted@@