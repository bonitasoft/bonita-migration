DECLARE profile_entry_id_max INT;

BEGIN
	SELECT MAX(id) + 1 INTO profile_id_max FROM profile;
	INSERT INTO profile (id, name, description, isDefault, iconPath, creationDate, createdBy, lastUpdateDate, lastUpdatedBy)
	VALUES  (
		profile_id_max,
		'Process manager', 	
		'The process manager can manage (not install/delete) his apps, view and export his reports.', 
		1, 
		'/profiles/profilePM.png',
		:creationDate,
		-1, 
		:creationDate,
		-1);
END;