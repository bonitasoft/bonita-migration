--
-- Category
-- 

ALTER TABLE "category" ADD "name_temp" VARCHAR2(50 CHAR) @@
UPDATE "category" SET "name_temp" = "name" @@
ALTER TABLE "category" DROP COLUMN "name" @@
ALTER TABLE "category" RENAME COLUMN "name_temp" TO "name" @@
ALTER TABLE "category" MODIFY "name" NOT NULL @@


ALTER TABLE "category" ADD "description_temp" VARCHAR2(1024 CHAR) @@
UPDATE "category" SET "description_temp" = "description" @@
ALTER TABLE "category" DROP COLUMN "description" @@
ALTER TABLE "category" RENAME COLUMN "description_temp" TO "description" @@