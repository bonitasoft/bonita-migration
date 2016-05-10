UPDATE page SET	processdefinitionid = 0 WHERE processdefinitionid IS NULL
@@
ALTER TABLE page MODIFY processDefinitionId BIGINT NOT NULL
