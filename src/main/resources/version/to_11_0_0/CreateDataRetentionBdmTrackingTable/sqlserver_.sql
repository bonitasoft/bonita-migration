CREATE TABLE data_retention_bdm_tracking (
    id                  NUMERIC(19, 0) NOT NULL,
    data_id             NUMERIC(19, 0) NOT NULL,
    data_classname      NVARCHAR(255) NOT NULL,
    created_at          NUMERIC(19, 0) NOT NULL,
    last_modified_at    NUMERIC(19, 0) NOT NULL,
    CONSTRAINT pk_data_retention_bdm_tracking PRIMARY KEY (id),
    CONSTRAINT uk_data_retention_bdm_tracking_data_id_data_classname UNIQUE (data_id, data_classname)
)
@@
CREATE INDEX idx_data_retention_bdm_tracking_data_classname ON data_retention_bdm_tracking (data_classname)
