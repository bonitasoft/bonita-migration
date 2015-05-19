ALTER TABLE process_definition ADD content_tenantid NUMERIC(19, 0)
@@
ALTER TABLE process_definition ADD content_id NUMERIC(19, 0)
@@
CREATE TABLE process_content (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  content NVARCHAR(MAX) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
GO