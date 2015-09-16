--
-- Job_desc
-- 
ALTER TABLE job_desc MODIFY jobclassname VARCHAR2(100 CHAR) @@
ALTER TABLE job_desc MODIFY jobname VARCHAR2(100 CHAR) @@
ALTER TABLE job_desc MODIFY description VARCHAR2(50 CHAR) @@


--
-- Job_param
-- 
ALTER TABLE job_param MODIFY key_ VARCHAR2(50 CHAR) @@
