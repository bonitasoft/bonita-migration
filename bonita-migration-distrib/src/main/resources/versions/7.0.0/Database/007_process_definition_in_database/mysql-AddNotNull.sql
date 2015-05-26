ALTER TABLE process_definition MODIFY content_tenantid BIGINT NOT NULL;
ALTER TABLE process_definition MODIFY content_id BIGINT NOT NULL;
ALTER TABLE process_definition ADD CONSTRAINT fk_process_definition_content FOREIGN KEY (content_tenantid, content_id) REFERENCES process_content(tenantid, id);