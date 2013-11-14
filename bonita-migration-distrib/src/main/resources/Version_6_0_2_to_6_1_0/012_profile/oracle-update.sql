DECLARE
	profile_entry_id_max INT;

BEGIN
	SELECT MAX(id) + 1 INTO profile_entry_id_max FROM profileentry;
	INSERT INTO profileentry (tenantId, id, profileId, name, description, parentId, index_, type, page)
	VALUES  (
		:tenantId, 
		profile_entry_id_max,
		:admin_profile_id, 	
		'Import / Export', 'Import / Export an final organization', 
		:dir_profile_entry_id, 
		6, 'link', 'importexportorganization');
END;
/