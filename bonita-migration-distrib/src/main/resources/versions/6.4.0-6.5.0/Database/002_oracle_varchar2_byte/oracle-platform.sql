--
-- Platform
-- 

ALTER TABLE "platform" ADD "version_temp" VARCHAR2(50 CHAR) NOT NULL @@
UPDATE "platform" SET "version_temp" = "version" @@
ALTER TABLE "platform" DROP COLUMN "version" @@
ALTER TABLE "platform" RENAME COLUMN "version_temp" TO "version" @@


ALTER TABLE "platform" ADD "previousVersion_temp" VARCHAR2(50 CHAR) @@
UPDATE "platform" SET "previousVersion_temp" = "previousVersion" @@
ALTER TABLE "platform" DROP COLUMN "previousVersion" @@
ALTER TABLE "platform" RENAME COLUMN "previousVersion_temp" TO "previousVersion" @@


ALTER TABLE "platform" ADD "initialVersion_temp" VARCHAR2(50 CHAR) NOT NULL @@
UPDATE "platform" SET "initialVersion_temp" = "initialVersion" @@
ALTER TABLE "platform" DROP COLUMN "initialVersion" @@
ALTER TABLE "platform" RENAME COLUMN "initialVersion_temp" TO "initialVersion" @@


ALTER TABLE "platform" ADD "createdBy_temp" VARCHAR2(50 CHAR) NOT NULL @@
UPDATE "platform" SET "createdBy_temp" = "createdBy" @@
ALTER TABLE "platform" DROP COLUMN "createdBy" @@
ALTER TABLE "platform" RENAME COLUMN "createdBy_temp" TO "createdBy" @@



--
-- Tenant
-- 

ALTER TABLE "tenant" ADD "createdBy_temp" VARCHAR2(50 CHAR) NOT NULL @@
UPDATE "tenant" SET "createdBy_temp" = "createdBy" @@
ALTER TABLE "tenant" DROP COLUMN "createdBy" @@
ALTER TABLE "tenant" RENAME COLUMN "createdBy_temp" TO "createdBy" @@


ALTER TABLE "tenant" ADD "description_temp" VARCHAR2(255 CHAR) @@
UPDATE "tenant" SET "description_temp" = "description" @@
ALTER TABLE "tenant" DROP COLUMN "description" @@
ALTER TABLE "tenant" RENAME COLUMN "description_temp" TO "description" @@


ALTER TABLE "tenant" ADD "iconname_temp" VARCHAR2(50 CHAR) @@
UPDATE "tenant" SET "iconname_temp" = "iconname" @@
ALTER TABLE "tenant" DROP COLUMN "iconname" @@
ALTER TABLE "tenant" RENAME COLUMN "iconname_temp" TO "iconname" @@


ALTER TABLE "tenant" ADD "iconpath_temp" VARCHAR2(255 CHAR) @@
UPDATE "tenant" SET "iconpath_temp" = "iconpath" @@
ALTER TABLE "tenant" DROP COLUMN "iconpath" @@
ALTER TABLE "tenant" RENAME COLUMN "iconpath_temp" TO "iconpath" @@


ALTER TABLE "tenant" ADD "name_temp" VARCHAR2(50 CHAR) NOT NULL @@
UPDATE "tenant" SET "name_temp" = "name" @@
ALTER TABLE "tenant" DROP COLUMN "name" @@
ALTER TABLE "tenant" RENAME COLUMN "name_temp" TO "name" @@


ALTER TABLE "tenant" ADD "status_temp" VARCHAR2(15 CHAR) NOT NULL @@
UPDATE "tenant" SET "status_temp" = "status" @@
ALTER TABLE "tenant" DROP COLUMN "status" @@
ALTER TABLE "tenant" RENAME COLUMN "status_temp" TO "status" @@
