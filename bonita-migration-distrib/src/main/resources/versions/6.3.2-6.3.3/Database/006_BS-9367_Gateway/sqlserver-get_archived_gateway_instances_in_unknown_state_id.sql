SELECT id FROM arch_flownode_instance
WHERE sourceObjectId = :flowNodeInstanceId
AND stateName NOT IN ('executing', 'failed', 'completed')
AND tenantId = :tenantId 
AND kind = 'gate'