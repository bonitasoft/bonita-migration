UPDATE page SET	processdefinitionid = 0 WHERE processdefinitionid IS NULL
@@
ALTER TABLE page MODIFY processDefinitionId NUMBER(19, 0) NOT NULL
