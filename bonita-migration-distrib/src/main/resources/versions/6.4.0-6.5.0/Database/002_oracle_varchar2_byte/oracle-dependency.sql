--
-- pdependency
-- 

ALTER TABLE "pdependency" ADD "name_temp" VARCHAR2(50 CHAR) NOT NULL UNIQUE @@
UPDATE "pdependency" SET "name_temp" = "name" @@
ALTER TABLE "pdependency" DROP COLUMN "name" @@
ALTER TABLE "pdependency" RENAME COLUMN "name_temp" TO "name" @@


ALTER TABLE "pdependency" ADD "description_temp" VARCHAR2(1024 CHAR) @@
UPDATE "pdependency" SET "description_temp" = "description" @@
ALTER TABLE "pdependency" DROP COLUMN "description" @@
ALTER TABLE "pdependency" RENAME COLUMN "description_temp" TO "description" @@


ALTER TABLE "pdependency" ADD "filename_temp" VARCHAR2(255 CHAR) NOT NULL  @@
UPDATE "pdependency" SET "filename_temp" = "filename" @@
ALTER TABLE "pdependency" DROP COLUMN "filename" @@
ALTER TABLE "pdependency" RENAME COLUMN "filename_temp" TO "filename" @@


--
-- pdependencymapping
-- 

ALTER TABLE "pdependencymapping" ADD "artifacttype_temp" VARCHAR2(50 CHAR) NOT NULL @@
UPDATE "pdependencymapping" SET "artifacttype_temp" = "artifacttype" @@
ALTER TABLE "pdependencymapping" DROP COLUMN "artifacttype" @@
ALTER TABLE "pdependencymapping" RENAME COLUMN "artifacttype_temp" TO "artifacttype" @@


--
-- dependency
-- 

ALTER TABLE "dependency" ADD "name_temp" VARCHAR2(150 CHAR) NOT NULL @@
UPDATE "dependency" SET "name_temp" = "name" @@
ALTER TABLE "dependency" DROP COLUMN "name" @@
ALTER TABLE "dependency" RENAME COLUMN "name_temp" TO "name" @@


ALTER TABLE "dependency" ADD "description_temp" VARCHAR2(1024 CHAR) @@
UPDATE "dependency" SET "description_temp" = "description" @@
ALTER TABLE "dependency" DROP COLUMN "description" @@
ALTER TABLE "dependency" RENAME COLUMN "description_temp" TO "description" @@


ALTER TABLE "dependency" ADD "filename_temp" VARCHAR2(255 CHAR) NOT NULL  @@
UPDATE "dependency" SET "filename_temp" = "filename" @@
ALTER TABLE "dependency" DROP COLUMN "filename" @@
ALTER TABLE "dependency" RENAME COLUMN "filename_temp" TO "filename" @@


--
-- dependencymapping
-- 

ALTER TABLE "dependencymapping" ADD "artifacttype_temp" VARCHAR2(50 CHAR) NOT NULL @@
UPDATE "dependencymapping" SET "artifacttype_temp" = "artifacttype" @@
ALTER TABLE "dependencymapping" DROP COLUMN "artifacttype" @@
ALTER TABLE "dependencymapping" RENAME COLUMN "artifacttype_temp" TO "artifacttype" @@