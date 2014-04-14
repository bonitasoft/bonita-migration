CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance (kind, logicalGroup2, executedBy) @@
CREATE INDEX idx_afi_sourceId_tenantid_kind ON arch_flownode_instance (sourceObjectId, tenantid, kind) @@

CREATE UNIQUE INDEX idx_UQ_pending_mapping ON pending_mapping (tenantid, activityId, userId, actorId) @@