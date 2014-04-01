INSERT INTO profileentry (tenantId, id, profileId, parentId, name, description, index_, type, page)
VALUES  (
	:tenantId,
	(SELECT MAX(id) + 1 FROM profileentry),
	:process_manager_profile_id, 0,
	'Tasks', 'My tasks', 
	0, 'link', 'tasklistingpm')
@@
	
INSERT INTO profileentry (tenantId, id, profileId, parentId, name, description, index_, type, page)
VALUES  (
	:tenantId,
	(SELECT MAX(id) + 1 FROM profileentry),
	:process_manager_profile_id, 0,
	'Cases', 'My cases', 
	2, 'link', 'caselistingpm')
@@
	
INSERT INTO profileentry (tenantId, id, profileId, parentId, name, description, index_, type, page)
VALUES  (
	:tenantId,
	(SELECT MAX(id) + 1 FROM profileentry),
	:process_manager_profile_id, 0,
	'Apps', 'My processes', 
	4, 'link', 'processlistingpm')
@@