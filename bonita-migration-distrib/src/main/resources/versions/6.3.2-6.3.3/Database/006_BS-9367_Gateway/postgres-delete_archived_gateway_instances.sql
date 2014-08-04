DELETE FROM arch_flownode_instance 
WHERE sourceObjectId = :flowNodeInstanceId
AND tenantId = :tenantId
AND kind = 'gate'
AND state IN (:states)