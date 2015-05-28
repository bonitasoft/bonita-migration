ALTER TABLE process_definition ALTER COLUMN content_tenantid SET NOT NULL;
ALTER TABLE process_definition ALTER COLUMN content_id SET NOT NULL;
ALTER TABLE process_definition ADD CONSTRAINT fk_process_definition_content FOREIGN KEY (content_tenantid, content_id) REFERENCES process_content(tenantid, id);