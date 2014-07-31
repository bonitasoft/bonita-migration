UPDATE flownode_instance
SET 
    terminal = arch_flownode_instance.terminal,
    stable = arch_flownode_instance.stable,
    stateId = arch_flownode_instance.stateId,
    stateName = arch_flownode_instance.stateName,
    hitBys = arch_flownode_instance.hitBys,
    prev_state_id = 0
FROM
    arch_flownode_instance
WHERE
    flownode_instance.id = arch_flownode_instance.sourceObjectId
    AND arch_flownode_instance.sourceObjectId = :flowNodeInstanceId
	AND arch_flownode_instance.stateName = 'executing'
	AND arch_flownode_instance.tenantId = :tenantId
	AND arch_flownode_instance.kind = 'gate'