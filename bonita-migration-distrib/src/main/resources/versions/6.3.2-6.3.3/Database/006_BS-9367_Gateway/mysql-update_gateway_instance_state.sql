UPDATE flownode_Instance f
	LEFT JOIN arch_flownode_instance af 
	ON f.id = af.sourceObjectId
SET
    f.terminal = af.terminal,
    f.stable = af.stable,
    f.stateId = af.stateId,
    f.stateName = af.stateName,
    f.hitBys = af.hitBys,
    f.prev_state_id = 0
WHERE af.sourceObjectId = :flowNodeInstanceId
AND af.stateName = 'executing'
AND f.tenantId = :tenantId
AND f.kind = 'gate'