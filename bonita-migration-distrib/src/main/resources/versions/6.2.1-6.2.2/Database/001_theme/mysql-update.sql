UPDATE theme 
SET content = ':content',
	cssContent = ':cssContent', 
	lastUpdateDate = :lastUpdateDate
WHERE isDefault = true
	AND type = ':type';

UPDATE theme 
SET lastUpdateDate = :lastUpdateDate + 1
WHERE isDefault = false
	AND type = ':type';
