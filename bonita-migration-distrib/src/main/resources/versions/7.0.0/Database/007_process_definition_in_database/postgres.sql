ALTER TABLE process_definition ADD COLUMN content_tenantid INT8;
ALTER TABLE process_definition ADD COLUMN content_id INT8;
CREATE TABLE process_content (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  content TEXT NOT NULL,
  PRIMARY KEY (tenantid, id)
);