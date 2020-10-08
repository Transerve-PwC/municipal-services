CREATE TABLE nulm_susv_familiy_detail (
	uuid varchar(64) NOT NULL,
	application_uuid varchar(64) NOT NULL,
	"name" varchar(64) NULL,
	age varchar(64) NULL,
	relation varchar(64) NULL,
	tenant_id varchar(256) NULL,
	is_active bool NULL,
	created_by varchar(64) NULL,
	created_time int8 NULL,
	last_modified_by varchar(64) NULL,
	last_modified_time int8 NULL,
	CONSTRAINT nulm_susv_familiy_detail_pkey PRIMARY KEY (uuid),
	CONSTRAINT "FK_nulm_susv_familiy_detail_application_uuid" FOREIGN KEY (application_uuid) REFERENCES nulm_susv_application_detail(application_uuid)
);