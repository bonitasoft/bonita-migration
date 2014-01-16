UPDATE theme 
SET content = utl_raw.cast_to_raw(':content'),
	cssContent = utl_raw.cast_to_raw(':cssContent'), 
	lastUpdateDate = :lastUpdateDate
WHERE isDefault = 1
	AND type = ':type' @@

UPDATE theme 
SET lastUpdateDate = :lastUpdateDate + 1
WHERE isDefault = 0
	AND type = ':type' @@
