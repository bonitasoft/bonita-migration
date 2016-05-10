CREATE TABLE process_content (
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  content NVARCHAR(MAX) NOT NULL,
  PRIMARY KEY (tenantid, id)
)