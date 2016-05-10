CREATE TABLE process_content (
  tenantid INT8 NOT NULL,
  id INT8 NOT NULL,
  content TEXT NOT NULL,
  PRIMARY KEY (tenantid, id)
)