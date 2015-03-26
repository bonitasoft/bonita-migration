DECLARE theme_id_max INT;

BEGIN
	SELECT NVL(MAX(id), 0) + 1 INTO theme_id_max FROM theme;
	INSERT INTO theme (tenantId, id, isDefault, content, cssContent, type, lastUpdateDate)
	VALUES (:tenantId, theme_id_max, 1, utl_raw.cast_to_raw(':content'), utl_raw.cast_to_raw(':cssContent'), ':type', :lastUpdateDate);
END;