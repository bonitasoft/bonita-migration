DECLARE
	admin_profile_id profile.id%type;
	config_profile_entry_id profileentry.id%type;
	profile_entry_id_max INT;
	dir_profile_entry_id profileentry.id%type;

BEGIN
	SELECT id INTO admin_profile_id FROM profile WHERE name = 'Administrator' AND tenantId = :tenantId;
	SELECT id INTO config_profile_entry_id FROM profileentry WHERE name = 'Configuration' AND tenantId = :tenantId;
	SELECT MAX(id) + 1 INTO profile_entry_id_max FROM profileentry;
	INSERT INTO profileentry (tenantId, id, profileId, name, description, parentId, index_, type, page)
	VALUES  (
		:tenantId, 
		profile_entry_id_max, 
		admin_profile_id, 
		'User rights', 'All profiles', 
		config_profile_entry_id, 
		0, 'link', 'profilelisting');
		
	SELECT MAX(id) + 1 INTO profile_entry_id_max FROM profileentry;
	SELECT id INTO dir_profile_entry_id FROM profileentry WHERE name = 'Directory' AND tenantId = :tenantId;
	INSERT INTO profileentry (tenantId, id, profileId, name, description, parentId, index_, type, page)
	VALUES  (
		:tenantId, 
		profile_entry_id_max,
		admin_profile_id, 	
		'Import / Export', 'Import / Export an final organization', 
		dir_profile_entry_id, 
		6, 'link', 'importexportorganization');
END;
/