--
-- Datas
--

UPDATE profile 
	SET name = 'Organization'
	WHERE name in ('Directory')
@@
	
UPDATE profile 
	SET name = 'Profiles'
	WHERE name in ('User rights')
@@