--
-- Job_desc
-- 

ALTER TABLE job_desc ADD jobclassname_temp VARCHAR2(100 CHAR) @@
UPDATE job_desc SET jobclassname_temp = jobclassname @@
ALTER TABLE job_desc DROP COLUMN jobclassname @@
ALTER TABLE job_desc RENAME COLUMN jobclassname_temp TO jobclassname @@
ALTER TABLE job_desc MODIFY jobclassname NOT NULL @@


ALTER TABLE job_desc ADD jobname_temp VARCHAR2(100 CHAR) @@
UPDATE job_desc SET jobname_temp = jobname @@
ALTER TABLE job_desc DROP COLUMN jobname @@
ALTER TABLE job_desc RENAME COLUMN jobname_temp TO jobname @@
ALTER TABLE job_desc MODIFY jobname NOT NULL @@


ALTER TABLE job_desc ADD description_temp VARCHAR2(50 CHAR) @@
UPDATE job_desc SET description_temp = description @@
ALTER TABLE job_desc DROP COLUMN description @@
ALTER TABLE job_desc RENAME COLUMN description_temp TO description @@



--
-- Job_param
-- 

ALTER TABLE job_param ADD key__temp VARCHAR2(50 CHAR) @@
UPDATE job_param SET key__temp = key_ @@
ALTER TABLE job_param DROP COLUMN key_ @@
ALTER TABLE job_param RENAME COLUMN key__temp TO key_ @@
ALTER TABLE job_param MODIFY key_ NOT NULL @@