CREATE TABLE arch_process_comment(
  tenantid NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  userId NUMERIC(19, 0),
  processInstanceId NUMERIC(19, 0) NOT NULL,
  postDate NUMERIC(19, 0) NOT NULL,
  content NVARCHAR(512) NOT NULL,
  archiveDate NUMERIC(19, 0) NOT NULL,
  sourceObjectId NUMERIC(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
@@
CREATE INDEX idx1_arch_process_comment on arch_process_comment (tenantid, sourceobjectid)
@@
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate, tenantid)
