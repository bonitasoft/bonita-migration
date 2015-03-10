CREATE TABLE "BREAKPOINT" ("TENANTID"     NUMBER(19,0) NOT NULL,
                           "ID"           NUMBER(19,0) NOT NULL,
                           "STATE_ID"     INT NOT NULL,
                           "INT_STATE_ID" INT NOT NULL,
                           "ELEM_NAME"    VARCHAR2(255) NOT NULL,
                           "INST_SCOPE"   NUMBER(1) NOT NULL,
                           "INST_ID"      NUMBER(19,0) NOT NULL,
                           "DEF_ID"       NUMBER(19,0) NOT NULL,
			   				PRIMARY KEY (tenantid, id)) @@
			  
INSERT INTO "SEQUENCE" ("TENANTID", "ID", "NEXTID")
	SELECT "ID", 10019, 1 FROM "TENANT"
	ORDER BY "ID" ASC @@
	
	
--
-- FOREIGN KEYS [CREATE]
-- 

ALTER TABLE "BREAKPOINT" ADD CONSTRAINT "FK_BREAKPOINT_TENANTID" FOREIGN KEY ("TENANTID") REFERENCES "TENANT" ("ID") @@	
