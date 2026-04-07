CREATE TABLE data_retention_config (
    id                  NUMERIC(19, 0) NOT NULL,
    data_classname      NVARCHAR(255) NOT NULL,
    reference_date      NVARCHAR(20) NOT NULL,
    retention_days      INT NOT NULL,
    created_at          NUMERIC(19, 0) NOT NULL,
    updated_at          NUMERIC(19, 0) NOT NULL,
    CONSTRAINT pk_data_retention_config PRIMARY KEY (id),
    CONSTRAINT uk_data_retention_config_classname UNIQUE (data_classname)
)
