
CREATE TABLE token (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processInstanceId BIGINT NOT NULL,
  ref_id BIGINT NOT NULL,
  parent_ref_id BIGINT NULL,
  PRIMARY KEY (tenantid, id)
);

