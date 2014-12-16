--
-- Profile
-- 

ALTER TABLE "profile" ADD "name_temp" VARCHAR2(50 CHAR) @@
UPDATE "profile" SET "name_temp" = "name" @@
ALTER TABLE "profile" DROP COLUMN "name" @@
ALTER TABLE "profile" RENAME COLUMN "name_temp" TO "name" @@
ALTER TABLE "profile" MODIFY "name" NOT NULL @@


ALTER TABLE "profile" ADD "description_temp" VARCHAR2(1024 CHAR) @@
UPDATE "profile" SET "description_temp" = "description" @@
ALTER TABLE "profile" DROP COLUMN "description" @@
ALTER TABLE "profile" RENAME COLUMN "description_temp" TO "description" @@



--
-- Profile_entry
-- 

ALTER TABLE "profileentry" ADD "name_temp" VARCHAR2(50 CHAR) @@
UPDATE "profileentry" SET "name_temp" = "name" @@
ALTER TABLE "profileentry" DROP COLUMN "name" @@
ALTER TABLE "profileentry" RENAME COLUMN "name_temp" TO "name" @@


ALTER TABLE "profileentry" ADD "description_temp" VARCHAR2(1024 CHAR) @@
UPDATE "profileentry" SET "description_temp" = "description" @@
ALTER TABLE "profileentry" DROP COLUMN "description" @@
ALTER TABLE "profileentry" RENAME COLUMN "description_temp" TO "description" @@


ALTER TABLE "profileentry" ADD "type_temp" VARCHAR2(50 CHAR) @@
UPDATE "profileentry" SET "type_temp" = "type" @@
ALTER TABLE "profileentry" DROP COLUMN "type" @@
ALTER TABLE "profileentry" RENAME COLUMN "type_temp" TO "type" @@


ALTER TABLE "profileentry" ADD "page_temp" VARCHAR2(50 CHAR) @@
UPDATE "profileentry" SET "page_temp" = "page" @@
ALTER TABLE "profileentry" DROP COLUMN "page" @@
ALTER TABLE "profileentry" RENAME COLUMN "page_temp" TO "page" @@
