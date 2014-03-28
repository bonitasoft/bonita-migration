INSERT INTO profileentry (tenantId, id, profileId, name, description, index_, type, page)
VALUES  (
	:tenantId,
	(SELECT MAX(id) + 1 FROM profileentry),
	:process_manager_profile_id, 	
	'Tasks', 'My tasks', 
	0, 'link', 'tasklistingpm');
	
INSERT INTO profileentry (tenantId, id, profileId, name, description, index_, type, page)
VALUES  (
	:tenantId,
	(SELECT MAX(id) + 1 FROM profileentry),
	:process_manager_profile_id, 	
	'Cases', 'My cases', 
	2, 'link', 'caselistingpm');
	
INSERT INTO profileentry (tenantId, id, profileId, name, description, index_, type, page)
VALUES  (
	:tenantId,
	(SELECT MAX(id) + 1 FROM profileentry),
	:process_manager_profile_id, 	
	'Apps', 'My processes', 
	4, 'link', 'processlistingpm');