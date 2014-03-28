SELECT MAX(id) + 1 INTO @profile_entry_id_max FROM profileentry FOR UPDATE;
INSERT INTO profileentry (tenantId, id, profileId, name, description, index_, type, page)
VALUES  (
	:tenantId,
	@profile_entry_id_max,
	:process_manager_profile_id, 	
	'Tasks', 'My tasks', 
	0, 'link', 'tasklistingpm');
	
INSERT INTO profileentry (tenantId, id, profileId, name, description, index_, type, page)
VALUES  (
	:tenantId,
	@profile_entry_id_max + 1,
	:process_manager_profile_id, 	
	'Cases', 'My cases', 
	2, 'link', 'caselistingpm');
	
INSERT INTO profileentry (tenantId, id, profileId, name, description, index_, type, page)
VALUES  (
	:tenantId,
	@profile_entry_id_max + 2,
	:process_manager_profile_id, 	
	'Apps', 'My processes', 
	4, 'link', 'processlistingpm');