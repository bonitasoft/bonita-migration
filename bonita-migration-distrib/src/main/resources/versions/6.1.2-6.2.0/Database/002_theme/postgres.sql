CREATE TABLE theme (
  tenantId INT8 NOT NULL,
  id INT8 NOT NULL,
  isDefault BOOLEAN NOT NULL,
  content BYTEA NOT NULL,
  cssContent BYTEA,
  type VARCHAR(50) NOT NULL,
  lastUpdateDate INT8 NOT NULL,
  CONSTRAINT "UK_Theme" UNIQUE (tenantId, isDefault, type),
  PRIMARY KEY (tenantId, id)
);
						
INSERT INTO sequence (tenantid, id, nextid)
	SELECT ID, 9890, 1 FROM tenant
	ORDER BY id ASC;
	

--
-- FOREIGN KEYS [CREATE]
-- 

ALTER TABLE theme ADD CONSTRAINT fk_theme_tenantid FOREIGN KEY (tenantid) REFERENCES tenant (id)
                                                             ON DELETE NO ACTION
                                                             ON UPDATE NO ACTION;