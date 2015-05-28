ALTER TABLE process_definition ADD content_tenantid BIGINT;
ALTER TABLE process_definition ADD content_id BIGINT;
CREATE TABLE process_content (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  content MEDIUMTEXT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
