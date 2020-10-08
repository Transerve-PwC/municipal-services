CREATE TABLE nulm_suh_staff_maintenance (
	suh_uuid varchar(256) NOT NULL,
	staff_uuid varchar(256) NOT NULL,
	is_manager bool NULL,
	manager_remark varchar(256) NULL,
	is_security_staff bool NULL,
	security_staff_remark varchar(256) NULL,
	is_cleaner bool NULL,
	cleaner_remark varchar(256) NULL,
	tenant_id varchar(256) NOT NULL,
	is_active bool NULL,
	created_by varchar(64) NULL,
	created_time int8 NULL,
	last_modified_by varchar(64) NULL,
	last_modified_time int8 NULL,
	CONSTRAINT nulm_suh_staff_maintenance_pkey PRIMARY KEY (staff_uuid),
	CONSTRAINT nulm_suh_staff_maintenance_uk UNIQUE (staff_uuid, tenant_id)
);