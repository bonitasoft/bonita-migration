UPDATE flownode_Instance
SET 
    f.terminal = af.terminal,
    f.stable = af.stable,
    f.stateId = af.stateId,
    f.stateName = af.stateName,
    f.hitBys = af.hitBys,
    f.prev_state_id = 0
FROM flownode_Instance f LEFT JOIN arch_flownode_instance af
WHERE f.id = af.sourceObjectId
AND af.sourceObjectId = :flowNodeInstanceId
AND af.stateName = 'executing'
AND af.tenantId = :tenantId
AND af.kind = 'gate'