UPDATE theme 
SET content = CONVERT(VARBINARY(MAX), ':content'),
	cssContent = CONVERT(VARBINARY(MAX), ':cssContent'), 
	lastUpdateDate = :lastUpdateDate
WHERE isDefault = 1
	AND type = ':type' @@

UPDATE theme 
SET lastUpdateDate = :lastUpdateDate + 1
WHERE isDefault = 0
	AND type = ':type' @@
