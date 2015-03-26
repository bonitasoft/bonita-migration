--
-- Comment
-- 

ALTER TABLE process_comment MODIFY kind VARCHAR2(25 CHAR) @@
ALTER TABLE process_comment MODIFY content VARCHAR2(255 CHAR) @@


--
-- Archived_comment
-- 

ALTER TABLE arch_process_comment MODIFY content VARCHAR2(255 CHAR) @@
