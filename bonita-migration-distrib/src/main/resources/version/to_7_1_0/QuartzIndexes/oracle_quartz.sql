create index idx_qrtz_t_nf_time on qrtz_triggers(NEXT_FIRE_TIME)
@@
create index idx_qrtz_t_state on qrtz_triggers(TRIGGER_STATE)
@@
create index idx_qrtz_t_nf_st on qrtz_triggers(TRIGGER_STATE,NEXT_FIRE_TIME)
@@
create index idx_qrtz_ft_trig_name on qrtz_fired_triggers(TRIGGER_NAME)
@@
create index idx_qrtz_ft_trig_group on qrtz_fired_triggers(TRIGGER_GROUP)
@@
create index idx_qrtz_ft_trig_n_g on qrtz_fired_triggers(TRIGGER_NAME,TRIGGER_GROUP)
@@
create index idx_qrtz_ft_trig_inst_name on qrtz_fired_triggers(INSTANCE_NAME)
@@
create index idx_qrtz_ft_job_name on qrtz_fired_triggers(JOB_NAME)
@@
create index idx_qrtz_ft_job_group on qrtz_fired_triggers(JOB_GROUP)
@@
create index idx_qrtz_t_nf_time_misfire on qrtz_triggers(MISFIRE_INSTR,NEXT_FIRE_TIME)
@@
create index idx_qrtz_t_nf_st_misfire on qrtz_triggers(MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE)
@@
create index idx_qrtz_t_nf_st_misfire_grp on qrtz_triggers(MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE)
@@