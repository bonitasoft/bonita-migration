--
-- Datas
--

UPDATE profileentry 
	SET name = 'Organization'
	WHERE name in ('Directory');
	
UPDATE profileentry 
	SET name = 'Profiles'
	WHERE name in ('User rights');