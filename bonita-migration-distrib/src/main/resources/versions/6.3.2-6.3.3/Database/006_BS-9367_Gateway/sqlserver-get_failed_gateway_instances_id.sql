SELECT id 
FROM flownode_instance
WHERE stateName = 'failed'
AND kind = 'gate'
AND tenantId = :tenantId 