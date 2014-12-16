--
-- Report
-- 

ALTER TABLE "report" ADD "name_temp" VARCHAR2(50 CHAR) @@
UPDATE "report" SET "name_temp" = "name" @@
ALTER TABLE "report" DROP COLUMN "name" @@
ALTER TABLE "report" RENAME COLUMN "name_temp" TO "name" @@
ALTER TABLE "report" MODIFY "name" NOT NULL @@


ALTER TABLE "report" ADD "description_temp" VARCHAR2(1024 CHAR) @@
UPDATE "report" SET "description_temp" = "description" @@
ALTER TABLE "report" DROP COLUMN "description" @@
ALTER TABLE "report" RENAME COLUMN "description_temp" TO "description" @@
