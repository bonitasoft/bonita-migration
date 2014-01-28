CREATE TABLE theme (
  tenantId NUMBER(19, 0) NOT NULL,
  id NUMBER(19, 0) NOT NULL,
  isDefault NUMBER(1) NOT NULL,
  content BLOB NOT NULL,
  cssContent BLOB,
  type VARCHAR2(50) NOT NULL,
  lastUpdateDate NUMBER(19, 0) NOT NULL,
  CONSTRAINT "UK_Theme" UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
) @@
			  
INSERT INTO "SEQUENCE" ("TENANTID", "ID", "NEXTID")
	SELECT "ID", 9890, 3 FROM "TENANT"
	ORDER BY "ID" ASC @@
	
	
--
-- FOREIGN KEYS [CREATE]
-- 

ALTER TABLE "THEME" ADD CONSTRAINT "FK_THEME_TENANTID" FOREIGN KEY ("TENANTID") REFERENCES "TENANT" ("ID") @@	
