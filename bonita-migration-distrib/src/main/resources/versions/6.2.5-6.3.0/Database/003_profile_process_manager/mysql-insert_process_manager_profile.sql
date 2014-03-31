SELECT MAX(id) + 1 INTO @profile_id_max FROM profile FOR UPDATE;
INSERT INTO profile (tenantId, id, name, description, isDefault, iconPath, creationDate, createdBy, lastUpdateDate, lastUpdatedBy)
VALUES  (
	:tenantId,
	@profile_id_max,
	'Process manager', 	
	'The process manager can manage (not install/delete) his apps, view and export his reports.', 
	1, 
	'/profiles/profilePM.png',
	:creationDate,
	-1, 
	:creationDate,
	-1);