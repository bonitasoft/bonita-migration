CREATE TABLE data_retention_bdm_tracking (
    id                  NUMBER(19, 0) NOT NULL,
    data_id             NUMBER(19, 0) NOT NULL,
    data_classname      VARCHAR2(255 CHAR) NOT NULL,
    created_at          NUMBER(19, 0) NOT NULL,
    last_modified_at    NUMBER(19, 0) NOT NULL,
    CONSTRAINT pk_data_retention_bdm_tracking PRIMARY KEY (id),
    CONSTRAINT uk_data_retention_bdm_tracking_data_id_data_classname UNIQUE (data_id, data_classname)
)
@@
CREATE INDEX idx_data_retention_bdm_tracking_data_classname ON data_retention_bdm_tracking (data_classname)
