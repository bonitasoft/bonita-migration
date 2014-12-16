--
-- ACTOR
-- 

ALTER TABLE "ACTOR" ADD "name_temp" VARCHAR2(50 CHAR) NOT NULL @@
UPDATE "ACTOR" SET "name_temp" = "name" @@
ALTER TABLE "ACTOR" DROP COLUMN "name" @@
ALTER TABLE "ACTOR" RENAME COLUMN "name_temp" TO "NAME" @@


ALTER TABLE "ACTOR" ADD "displayName_temp" VARCHAR2(75 CHAR) @@
UPDATE "ACTOR" SET "displayName_temp" = "displayName" @@
ALTER TABLE "ACTOR" DROP COLUMN "displayName" @@
ALTER TABLE "ACTOR" RENAME COLUMN "displayName_temp" TO "displayName" @@


ALTER TABLE "ACTOR" ADD "description_temp" VARCHAR2(1024 CHAR) @@
UPDATE "ACTOR" SET "description_temp" = "description" @@
ALTER TABLE "ACTOR" DROP COLUMN "description" @@
ALTER TABLE "ACTOR" RENAME COLUMN "description_temp" TO "description" @@