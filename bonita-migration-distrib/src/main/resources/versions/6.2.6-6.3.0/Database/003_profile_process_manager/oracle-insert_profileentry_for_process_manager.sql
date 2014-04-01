DECLARE profile_entry_id_max INT;

BEGIN
	SELECT MAX(id) + 1 INTO profile_entry_id_max FROM profileentry;
	INSERT INTO profileentry (tenantId, id, profileId, parentId, name, description, index_, type, page)
	VALUES  (
		:tenantId,
		profile_entry_id_max,
		:process_manager_profile_id, 0,
		'Tasks', 'My tasks', 
		0, 'link', 'tasklistingpm');
		
	INSERT INTO profileentry (tenantId, id, profileId, parentId, name, description, index_, type, page)
	VALUES  (
		:tenantId,
		profile_entry_id_max + 1,
		:process_manager_profile_id, 0,
		'Cases', 'My cases', 
		2, 'link', 'caselistingpm');
		
	INSERT INTO profileentry (tenantId, id, profileId, parentId, name, description, index_, type, page)
	VALUES  (
		:tenantId,
		profile_entry_id_max + 2,
		:process_manager_profile_id, 0,
		'Apps', 'My processes', 
		4, 'link', 'processlistingpm');
END;