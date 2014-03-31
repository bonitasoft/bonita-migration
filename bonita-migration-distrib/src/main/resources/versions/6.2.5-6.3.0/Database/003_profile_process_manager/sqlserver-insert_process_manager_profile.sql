INSERT INTO profile (tenantId, id, name, description, isDefault, iconPath, creationDate, createdBy, lastUpdateDate, lastUpdatedBy)
VALUES  (
	:tenantId,
	(SELECT MAX(id) + 1 FROM profile), 
	'Process manager', 	
	'The process manager can manage (not install/delete) his apps, view and export his reports.', 
	'True', 
	'/profiles/profilePM.png',
	:creationDate,
	-1, 
	:creationDate,
	-1)
@@