--
-- FOREIGN KEYS [DROP]
-- 

ALTER TABLE job_param DROP CONSTRAINT fk_job_param_jobid
@@


--
-- job_log
-- 

CREATE TABLE job_log (tenantid        NUMERIC(19,0) NOT NULL,
                      id              NUMERIC(19,0) NOT NULL,
                      jobDescriptorId NUMERIC(19,0) NOT NULL,
                      retryNumber     NUMERIC(19,0) NULL,
                      lastUpdateDate  NUMERIC(19,0) NULL,
                      lastMessage     NVARCHAR(MAX) NULL,
					  UNIQUE (tenantId, jobDescriptorId),
					  PRIMARY KEY (tenantid, id))
@@

INSERT INTO sequence (tenantid, id, nextid)
	SELECT ID, 72, 1 FROM tenant
	ORDER BY id ASC
@@


--
-- FOREIGN KEYS [CREATE]
-- 

ALTER TABLE job_log WITH CHECK ADD CONSTRAINT fk_job_log_jobid FOREIGN KEY (tenantid,jobDescriptorId) REFERENCES job_desc (tenantid, id)
                                                               ON DELETE CASCADE
                                                               ON UPDATE NO ACTION
@@
ALTER TABLE job_log CHECK CONSTRAINT fk_job_log_jobid
@@
ALTER TABLE job_param WITH CHECK ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid,jobDescriptorId) REFERENCES job_desc (tenantid, id)
                                                                   ON DELETE CASCADE
                                                                   ON UPDATE NO ACTION
@@
ALTER TABLE job_param CHECK CONSTRAINT fk_job_param_jobid
@@