--
-- job_log
-- 
CREATE TABLE job_log (tenantid        BIGINT(20) NOT NULL,
                      id              BIGINT(20) NOT NULL,
                      jobDescriptorId BIGINT(20) NOT NULL,
                      retryNumber     BIGINT(20) NULL,
                      lastUpdateDate  BIGINT(20) NULL,
                      lastMessage     TEXT NULL)
    ENGINE = InnoDB;
CREATE INDEX fk_job_log_jobId_idx ON job_log (jobDescriptorId ASC, tenantid ASC);
ALTER TABLE job_log ADD PRIMARY KEY (tenantid,id);
ALTER TABLE job_log ADD CONSTRAINT tenantid UNIQUE (tenantid,jobDescriptorId);
INSERT INTO sequence (tenantid, id, nextid)
SELECT ID, 72, 1 FROM tenant
ORDER BY id ASC;
--
-- job_param
-- 
ALTER TABLE job_param DROP FOREIGN KEY fk_job_param_jobid;
ALTER TABLE job_param MODIFY COLUMN value_ MEDIUMBLOB NOT NULL;
CREATE INDEX fk_job_param_jobid ON job_param (tenantid ASC, jobDescriptorId ASC);
ALTER TABLE job_log ADD CONSTRAINT fk_job_log_jobid FOREIGN KEY (tenantid,jobDescriptorId) REFERENCES job_desc (tenantid, id)
                                                    ON DELETE CASCADE
                                                    ON UPDATE NO ACTION;
ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid,jobDescriptorId) REFERENCES job_desc (tenantid, id)
                                                        ON DELETE CASCADE
                                                        ON UPDATE NO ACTION;
--
-- qrtz_fired_triggers
-- 
ALTER TABLE qrtz_fired_triggers ADD SCHED_TIME BIGINT(13) NOT NULL;
