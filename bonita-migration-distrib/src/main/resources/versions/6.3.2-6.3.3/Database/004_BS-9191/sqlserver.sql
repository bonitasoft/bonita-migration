--
-- arch_flownode_instance
-- 

CREATE INDEX idx_workedon_kind_lg2_executedBy ON arch_flownode_instance(kind, logicalGroup2, executedBy)
@@
DROP INDEX arch_flownode_instance.idx_afi_kind_lg2_executedBy
@@