CREATE TABLE arch_process_comment(
  tenantid NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  userId NUMBER(19, 0),
  processInstanceId NUMBER(19, 0) NOT NULL,
  postDate NUMBER(19, 0) NOT NULL,
  content VARCHAR2(512 CHAR) NOT NULL,
  archiveDate NUMBER(19, 0) NOT NULL,
  sourceObjectId NUMBER(19, 0) NOT NULL,
  PRIMARY KEY (tenantid, id)
)
@@
CREATE INDEX idx1_arch_process_comment on arch_process_comment (tenantid, sourceobjectid)
@@
CREATE INDEX idx2_arch_process_comment on arch_process_comment (processInstanceId, archivedate, tenantid)
