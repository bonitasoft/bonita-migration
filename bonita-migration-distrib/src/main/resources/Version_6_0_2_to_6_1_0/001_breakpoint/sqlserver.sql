CREATE TABLE breakpoint (tenantid     NUMERIC(19,0) NOT NULL,
                         id           NUMERIC(19,0) NOT NULL,
                         state_id     INT NOT NULL,
                         int_state_id INT NOT NULL,
                         elem_name    NVARCHAR(255) NOT NULL,
                         inst_scope   BIT NOT NULL,
                         inst_id      NUMERIC(19,0) NOT NULL,
                         def_id       NUMERIC(19,0) NOT NULL,
						 PRIMARY KEY (tenantid, id)
						)
GO

INSERT INTO sequence (tenantid, id, nextid)
SELECT ID, 10019, 1 FROM tenant
ORDER BY id ASC
GO


--
-- FOREIGN KEYS [CREATE]
-- 

ALTER TABLE breakpoint WITH CHECK ADD CONSTRAINT fk_breakpoint_tenantId FOREIGN KEY (tenantid) REFERENCES tenant (id)
                                                                        ON DELETE NO ACTION
                                                                        ON UPDATE NO ACTION
GO
ALTER TABLE breakpoint CHECK CONSTRAINT fk_breakpoint_tenantId
GO