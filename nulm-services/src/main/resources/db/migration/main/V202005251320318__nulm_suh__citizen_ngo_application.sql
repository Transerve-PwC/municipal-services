CREATE TABLE nulm_suh_citizenngo_application (
	suh_citizen_ngo_uuid varchar(256) NOT NULL,
	shelter_requested_for_person varchar(256) NULL,
	address varchar(256) NULL,
	gender varchar(10) NULL,
	age int4 NULL,
	reason_for_staying varchar(256) NULL,
	is_disabled bool NULL,
	nominated_by varchar(256) NULL,
	name_of_nominated_person varchar(256) NULL,
	contact_no varchar(256) NULL,
	is_active bool NULL,
	created_by varchar(64) NULL,
	created_time int8 NULL,
	last_modified_by varchar(64) NULL,
	last_modified_time int8 NULL,
	CONSTRAINT nulm_suh_citizenngo_application_pkey PRIMARY KEY (suh_citizen_ngo_uuid)
);