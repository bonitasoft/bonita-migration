INSERT INTO profileentry (tenantId, id, profileId, name, description, parentId, index_, type, page)
VALUES  (
	:tenantId, 
	(SELECT MAX(id) + 1 FROM profileentry), 
	(SELECT id FROM profile WHERE name = 'Administrator' AND tenantId = :tenantId), 
	'Import / Export', 'Import / Export an final organization', 
	(SELECT id FROM profileentry WHERE name = 'Directory' AND tenantId = :tenantId), 
	6, 'link', 'importexportorganization')
GO