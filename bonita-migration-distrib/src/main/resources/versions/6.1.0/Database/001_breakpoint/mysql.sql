CREATE TABLE breakpoint (tenantid     BIGINT(20) NOT NULL,
                         id           BIGINT(20) NOT NULL,
                         state_id     INT(11) NOT NULL,
                         int_state_id INT(11) NOT NULL,
                         elem_name    VARCHAR(255) NOT NULL,
                         inst_scope   TINYINT(1) NOT NULL,
                         inst_id      BIGINT(20) NOT NULL,
                         def_id       BIGINT(20) NOT NULL,
						 PRIMARY KEY (tenantid, id)
						)
    ENGINE = InnoDB;
CREATE INDEX fk_breakpoint_tenantId_idx ON breakpoint (tenantid ASC);
CREATE INDEX fk_breakpoint_process_definitionId_idx ON breakpoint (def_id ASC, tenantid ASC);
CREATE INDEX fk_breakpoint_process_instanceId_idx ON breakpoint (inst_id ASC, tenantid ASC);

INSERT INTO sequence (tenantid, id, nextid)
SELECT ID, 10019, 1 FROM tenant
ORDER BY id ASC;
ALTER TABLE breakpoint ADD CONSTRAINT fk_breakpoint_tenantId FOREIGN KEY (tenantid) REFERENCES tenant (id)
                                                             ON DELETE NO ACTION
                                                             ON UPDATE NO ACTION;