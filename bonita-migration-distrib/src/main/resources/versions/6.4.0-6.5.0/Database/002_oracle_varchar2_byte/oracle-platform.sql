--
-- Platform
-- 

ALTER TABLE platform ADD version_temp VARCHAR2(50 CHAR) @@
UPDATE platform SET version_temp = version @@
ALTER TABLE platform DROP COLUMN version @@
ALTER TABLE platform RENAME COLUMN version_temp TO version @@
ALTER TABLE platform MODIFY version NOT NULL @@


ALTER TABLE platform ADD previousVersion_temp VARCHAR2(50 CHAR) @@
UPDATE platform SET previousVersion_temp = previousVersion @@
ALTER TABLE platform DROP COLUMN previousVersion @@
ALTER TABLE platform RENAME COLUMN previousVersion_temp TO previousVersion @@


ALTER TABLE platform ADD initialVersion_temp VARCHAR2(50 CHAR) @@
UPDATE platform SET initialVersion_temp = initialVersion @@
ALTER TABLE platform DROP COLUMN initialVersion @@
ALTER TABLE platform RENAME COLUMN initialVersion_temp TO initialVersion @@
ALTER TABLE platform MODIFY initialVersion NOT NULL @@


ALTER TABLE platform ADD createdBy_temp VARCHAR2(50 CHAR) @@
UPDATE platform SET createdBy_temp = createdBy @@
ALTER TABLE platform DROP COLUMN createdBy @@
ALTER TABLE platform RENAME COLUMN createdBy_temp TO createdBy @@
ALTER TABLE platform MODIFY createdBy NOT NULL @@



--
-- Tenant
-- 

ALTER TABLE tenant ADD createdBy_temp VARCHAR2(50 CHAR) @@
UPDATE tenant SET createdBy_temp = createdBy @@
ALTER TABLE tenant DROP COLUMN createdBy @@
ALTER TABLE tenant RENAME COLUMN createdBy_temp TO createdBy @@
ALTER TABLE tenant MODIFY createdBy NOT NULL @@


ALTER TABLE tenant ADD description_temp VARCHAR2(255 CHAR) @@
UPDATE tenant SET description_temp = description @@
ALTER TABLE tenant DROP COLUMN description @@
ALTER TABLE tenant RENAME COLUMN description_temp TO description @@


ALTER TABLE tenant ADD iconName_temp VARCHAR2(50 CHAR) @@
UPDATE tenant SET iconName_temp = iconName @@
ALTER TABLE tenant DROP COLUMN iconName @@
ALTER TABLE tenant RENAME COLUMN iconName_temp TO iconName @@


ALTER TABLE tenant ADD iconPath_temp VARCHAR2(255 CHAR) @@
UPDATE tenant SET iconPath_temp = iconPath @@
ALTER TABLE tenant DROP COLUMN iconPath @@
ALTER TABLE tenant RENAME COLUMN iconPath_temp TO iconPath @@


ALTER TABLE tenant ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE tenant SET name_temp = name @@
ALTER TABLE tenant DROP COLUMN name @@
ALTER TABLE tenant RENAME COLUMN name_temp TO name @@
ALTER TABLE tenant MODIFY name NOT NULL @@


ALTER TABLE tenant ADD status_temp VARCHAR2(15 CHAR) @@
UPDATE tenant SET status_temp = status @@
ALTER TABLE tenant DROP COLUMN status @@
ALTER TABLE tenant RENAME COLUMN status_temp TO status @@
ALTER TABLE tenant MODIFY status NOT NULL @@
