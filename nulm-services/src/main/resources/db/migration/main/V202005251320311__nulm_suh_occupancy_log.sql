CREATE TABLE nulm_suh_occupancy_log (
	log_uuid varchar(256) NOT NULL,
	name_of_shelter varchar(256) NULL,
	"date" timestamp NULL,
	"name" varchar(256) NULL,
	qualification varchar(256) NULL,
	gender varchar(256) NULL,
	age varchar(256) NULL,
	address varchar(256) NULL,
	adhar_no varchar(256) NULL,
	reason_for_staying varchar(256) NULL,
	tenant_id varchar(256) NULL,
	is_active bool NULL,
	created_by varchar(64) NULL,
	created_time int8 NULL,
	last_modified_by varchar(64) NULL,
	last_modified_time int8 NULL,
	CONSTRAINT nulm_suh_occupancy_log_pkey PRIMARY KEY (log_uuid)
);