MERGE INTO flownode_instance 
	USING arch_flownode_instance 
	ON (flownode_instance.id = arch_flownode_instance.sourceObjectId
	AND arch_flownode_instance.sourceObjectId = :flowNodeInstanceId
	AND arch_flownode_instance.stateName = 'executing'
	AND flownode_instance.tenantId = :tenantId
	AND flownode_instance.kind = 'gate')
WHEN MATCHED THEN UPDATE 
SET 
    flownode_instance.terminal = arch_flownode_instance.terminal,
    flownode_instance.stable = arch_flownode_instance.stable,
    flownode_instance.stateId = arch_flownode_instance.stateId,
    flownode_instance.stateName = arch_flownode_instance.stateName,
    flownode_instance.hitBys = arch_flownode_instance.hitBys,
    flownode_instance.prev_state_id = 0