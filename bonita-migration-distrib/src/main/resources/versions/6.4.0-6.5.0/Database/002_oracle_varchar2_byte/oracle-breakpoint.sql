--
-- Breakpoint
-- 

ALTER TABLE "breakpoint" ADD "elem_name_temp" VARCHAR2(255 CHAR) NOT NULL @@
UPDATE "breakpoint" SET "elem_name_temp" = "elem_name" @@
ALTER TABLE "breakpoint" DROP COLUMN "elem_name" @@
ALTER TABLE "breakpoint" RENAME COLUMN "elem_name_temp" TO "elem_name" @@
