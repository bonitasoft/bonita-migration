CREATE TABLE arch_process_comment(
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  userId BIGINT,
  processInstanceId BIGINT NOT NULL,
  postDate BIGINT NOT NULL,
  content VARCHAR(512) NOT NULL,
  archiveDate BIGINT NOT NULL,
  sourceObjectId BIGINT NOT NULL,
  PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
@@
CREATE INDEX idx1_arch_process_comment on arch_process_comment (sourceobjectid, tenantid);
@@
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate, tenantid);
