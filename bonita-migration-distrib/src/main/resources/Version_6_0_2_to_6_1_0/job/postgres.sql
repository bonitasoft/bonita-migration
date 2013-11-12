--
-- FOREIGN KEYS [DROP]
-- 

ALTER TABLE job_param DROP CONSTRAINT fk_job_param_jobid;


--
-- job_log
-- 

CREATE TABLE job_log (tenantid        INT8 NOT NULL,
                      id              INT8 NOT NULL,
                      jobdescriptorid INT8 NOT NULL,
                      retrynumber     INT8 NULL,
                      lastupdatedate  INT8 NULL,
                      lastmessage     TEXT NULL);
ALTER TABLE job_log ADD CONSTRAINT job_log_pkey PRIMARY KEY (tenantid,id,jobdescriptorid);

INSERT INTO sequence (tenantid, id, nextid)
	SELECT ID, 72, 1 FROM tenant
	ORDER BY id ASC;
	
	
--
-- FOREIGN KEYS [CREATE]
-- 

ALTER TABLE job_log ADD CONSTRAINT fk_job_log_jobid FOREIGN KEY (tenantid,jobdescriptorid) REFERENCES job_desc (tenantid, id)
                                                    ON DELETE CASCADE
                                                    ON UPDATE NO ACTION;
ALTER TABLE job_param ADD CONSTRAINT fk_job_param_jobid FOREIGN KEY (tenantid,jobdescriptorid) REFERENCES job_desc (tenantid, id)
                                                        ON DELETE CASCADE
                                                        ON UPDATE NO ACTION;