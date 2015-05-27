ALTER TABLE process_definition ALTER COLUMN content_tenantid NUMERIC(19, 0) NOT NULL
@@
ALTER TABLE process_definition ALTER COLUMN content_id NUMERIC(19, 0) NOT NULL
@@
ALTER TABLE process_definition ADD CONSTRAINT fk_process_definition_content FOREIGN KEY (content_tenantid, content_id) REFERENCES process_content(tenantid, id)
@@