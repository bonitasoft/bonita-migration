ALTER TABLE page DROP CONSTRAINT uk_page
@@
UPDATE page SET	processdefinitionid = 0 WHERE processdefinitionid IS NULL
@@
ALTER TABLE page ALTER COLUMN processDefinitionId NUMERIC(19,0) NOT NULL
@@
ALTER TABLE page ADD CONSTRAINT  uk_page UNIQUE  (tenantId, name, processDefinitionId)