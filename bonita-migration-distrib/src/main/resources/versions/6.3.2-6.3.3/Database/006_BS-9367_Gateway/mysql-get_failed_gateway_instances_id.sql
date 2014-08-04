SELECT id 
FROM flownode_instance
WHERE (stateName = 'failed'
	OR (stateName = 'completed' && hitBys LIKE 'FINISH:%')
) 
AND kind = 'gate'
AND tenantId = :tenantId