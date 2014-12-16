--
-- Migration_plan
-- 

ALTER TABLE "migration_plan" ADD "description_temp" VARCHAR2(255 CHAR) @@
UPDATE "migration_plan" SET "description_temp" = "description" @@
ALTER TABLE "migration_plan" DROP COLUMN "description" @@
ALTER TABLE "migration_plan" RENAME COLUMN "description_temp" TO "description" @@
ALTER TABLE "migration_plan" MODIFY "description" NOT NULL @@


ALTER TABLE "migration_plan" ADD "source_name_temp" VARCHAR2(50 CHAR) @@
UPDATE "migration_plan" SET "source_name_temp" = "source_name" @@
ALTER TABLE "migration_plan" DROP COLUMN "source_name" @@
ALTER TABLE "migration_plan" RENAME COLUMN "source_name_temp" TO "source_name" @@
ALTER TABLE "migration_plan" MODIFY "source_name" NOT NULL @@


ALTER TABLE "migration_plan" ADD "source_version_temp" VARCHAR2(50 CHAR) @@
UPDATE "migration_plan" SET "source_version_temp" = "source_version" @@
ALTER TABLE "migration_plan" DROP COLUMN "source_version" @@
ALTER TABLE "migration_plan" RENAME COLUMN "source_version_temp" TO "source_version" @@
ALTER TABLE "migration_plan" MODIFY "source_version" NOT NULL @@


ALTER TABLE "migration_plan" ADD "target_name_temp" VARCHAR2(50 CHAR) @@
UPDATE "migration_plan" SET "target_name_temp" = "target_name" @@
ALTER TABLE "migration_plan" DROP COLUMN "target_name" @@
ALTER TABLE "migration_plan" RENAME COLUMN "target_name_temp" TO "target_name" @@
ALTER TABLE "migration_plan" MODIFY "target_name" NOT NULL @@


ALTER TABLE "migration_plan" ADD "target_version_temp" VARCHAR2(50 CHAR) @@
UPDATE "migration_plan" SET "target_version_temp" = "target_version" @@
ALTER TABLE "migration_plan" DROP COLUMN "target_version" @@
ALTER TABLE "migration_plan" RENAME COLUMN "target_version_temp" TO "target_version" @@
ALTER TABLE "migration_plan" MODIFY "target_version" NOT NULL @@
