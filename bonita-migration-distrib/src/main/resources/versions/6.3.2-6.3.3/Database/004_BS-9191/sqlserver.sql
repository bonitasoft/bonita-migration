--
-- arch_flownode_instance
-- 

DROP INDEX arch_flownode_instance.idx_afi_kind_lg2_executedBy
@@
CREATE INDEX idx_afi_kind_lg2_executedBy ON arch_flownode_instance(kind, logicalGroup2, executedBy)
@@
