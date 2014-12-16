--
-- Comment
-- 

ALTER TABLE "process_comment" ADD "kind_temp" VARCHAR2(25 CHAR) NOT NULL @@
UPDATE "process_comment" SET "kind_temp" = "kind" @@
ALTER TABLE "process_comment" DROP COLUMN "kind" @@
ALTER TABLE "process_comment" RENAME COLUMN "kind_temp" TO "kind" @@


ALTER TABLE "process_comment" ADD "content_temp" VARCHAR2(255 CHAR) NOT NULL @@
UPDATE "process_comment" SET "content_temp" = "content" @@
ALTER TABLE "process_comment" DROP COLUMN "content" @@
ALTER TABLE "process_comment" RENAME COLUMN "content_temp" TO "content" @@



--
-- Archived_comment
-- 

ALTER TABLE "arch_process_comment" ADD "content_temp" VARCHAR2(255 CHAR) NOT NULL @@
UPDATE "arch_process_comment" SET "content_temp" = "content" @@
ALTER TABLE "arch_process_comment" DROP COLUMN "content" @@
ALTER TABLE "arch_process_comment" RENAME COLUMN "content_temp" TO "content" @@