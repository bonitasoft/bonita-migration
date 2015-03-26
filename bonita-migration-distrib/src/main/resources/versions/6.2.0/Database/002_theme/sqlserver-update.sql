INSERT INTO theme (tenantId, id, isDefault, content, cssContent, type, lastUpdateDate)
VALUES ( :tenantId, 
		(SELECT Coalesce(MAX(id) + 1, 1) FROM theme), 1, 
		CONVERT(VARBINARY(MAX), ':content'), 
		CONVERT(VARBINARY(MAX), ':cssContent'), 
		':type', :lastUpdateDate)
@@
