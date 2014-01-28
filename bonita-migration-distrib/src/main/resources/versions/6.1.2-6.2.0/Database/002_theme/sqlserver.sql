CREATE TABLE theme (
  tenantId NUMERIC(19, 0) NOT NULL,
  id NUMERIC(19, 0) NOT NULL,
  isDefault BIT NOT NULL,
  content VARBINARY(MAX) NOT NULL,
  cssContent VARBINARY(MAX),
  type NVARCHAR(50) NOT NULL,
  lastUpdateDate NUMERIC(19, 0) NOT NULL,
  CONSTRAINT UK_Theme UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
)
@@

INSERT INTO sequence (tenantid, id, nextid)
	SELECT id, 9890, 3 FROM tenant
	ORDER BY id ASC
@@


--
-- FOREIGN KEYS [CREATE]
-- 

ALTER TABLE theme WITH CHECK ADD CONSTRAINT fk_theme_tenantId FOREIGN KEY (tenantid) REFERENCES tenant (id)
                                                                        ON DELETE NO ACTION
                                                                        ON UPDATE NO ACTION
@@
ALTER TABLE theme CHECK CONSTRAINT fk_theme_tenantId
@@