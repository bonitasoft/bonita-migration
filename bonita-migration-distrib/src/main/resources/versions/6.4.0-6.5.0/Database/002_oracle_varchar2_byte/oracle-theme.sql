--
-- theme
-- 

ALTER TABLE "theme" ADD "type_temp" VARCHAR2(50 CHAR) NOT NULL @@
UPDATE "theme" SET "type_temp" = "type" @@
ALTER TABLE "theme" DROP COLUMN "type" @@
ALTER TABLE "theme" RENAME COLUMN "type_temp" TO "type" @@
