SELECT IFNULL(MAX(id) + 1, 1) INTO @theme_id_max FROM theme FOR UPDATE;

INSERT INTO theme (tenantId, id, isDefault, content, cssContent, type, lastUpdateDate)
VALUES (:tenantId, @theme_id_max, true, ':content', ':cssContent', ':type', :lastUpdateDate);