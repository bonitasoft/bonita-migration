SELECT MAX(id) + 1 INTO @profile_id_max FROM profile FOR UPDATE;
INSERT INTO profileentry (id, name, description, isDefault, iconPath, creationDate, createdBy, lastUpdateDate, lastUpdatedBy)
VALUES  (
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