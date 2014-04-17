INSERT INTO profileentry (tenantId, id, profileId, name, description, parentId, index_, type, page)
VALUES  (
	:tenantId, 
	(SELECT MAX(id) + 1 FROM profileentry), 
	:admin_profile_id, 
	'Import / Export', 'Import / Export an final organization', 
	:dir_profile_entry_id, 
	6, 'link', 'importexportorganization');
	
UPDATE sequence SET nextId = (SELECT MAX(id) + 1 FROM profileentry WHERE tenantId = :tenantId)
WHERE tenantId = :tenantId
AND id = 9991;