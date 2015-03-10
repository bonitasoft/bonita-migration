CREATE TABLE breakpoint (tenantid     INT8 NOT NULL,
                         id           INT8 NOT NULL,
                         state_id     INT4 NOT NULL,
                         int_state_id INT4 NOT NULL,
                         elem_name    VARCHAR(255) NOT NULL,
                         inst_scope   BOOL NOT NULL,
                         inst_id      INT8 NOT NULL,
                         def_id       INT8 NOT NULL,
						 PRIMARY KEY (tenantid, id)
						);
						
INSERT INTO sequence (tenantid, id, nextid)
	SELECT ID, 10019, 1 FROM tenant
	ORDER BY id ASC;
	

--
-- FOREIGN KEYS [CREATE]
-- 

ALTER TABLE breakpoint ADD CONSTRAINT fk_breakpoint_tenantid FOREIGN KEY (tenantid) REFERENCES tenant (id)
                                                             ON DELETE NO ACTION
                                                             ON UPDATE NO ACTION;