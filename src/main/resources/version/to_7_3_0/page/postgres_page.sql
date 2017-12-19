UPDATE page SET	processdefinitionid = 0 WHERE processdefinitionid IS NULL
@@
ALTER TABLE page ALTER COLUMN processDefinitionId SET NOT NULL
