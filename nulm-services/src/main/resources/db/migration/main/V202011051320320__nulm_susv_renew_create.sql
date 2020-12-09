CREATE TABLE public.nulm_susv_renew_application_detail (
	application_uuid varchar(64) NOT NULL,
	application_id varchar(64) NOT NULL,
	application_status varchar(64) NULL,
	looking_for varchar(255) NULL,
	name_of_street_vendor varchar(255) NULL,
	cov_no varchar(255) NULL,
	residential_address varchar(255) NULL,
	change_of_location bool NULL,
	proposed_address varchar(255) NULL,
	tenant_id varchar(256) NULL,
	is_active bool NULL,
	created_by varchar(64) NULL,
	created_time int8 NULL,
	last_modified_by varchar(64) NULL,
	last_modified_time int8 NULL,
	name_of_proposed_new_street_vendor varchar(255) NULL,
	CONSTRAINT nulm_susv_renew_application_detail_application_id_key UNIQUE (application_id),
	CONSTRAINT nulm_susv_renew_application_detail_pkey PRIMARY KEY (application_uuid)
);

CREATE TABLE public.nulm_susv_renew_application_document (
	document_uuid varchar(64) NOT NULL,
	filestore_id varchar NOT NULL,
	application_uuid varchar(64) NOT NULL,
	document_type varchar(256) NOT NULL,
	tenant_id varchar(256) NULL,
	is_active bool NOT NULL,
	created_by varchar(64) NULL,
	created_time int8 NULL,
	last_modified_by varchar(256) NULL,
	last_modified_time int8 NULL,
	CONSTRAINT nulm_susv_renew_application_document_pkey PRIMARY KEY (document_uuid),
	CONSTRAINT "FK_nulm_susv_renew_application_document_application_uuid" FOREIGN KEY (application_uuid) REFERENCES nulm_susv_renew_application_detail(application_uuid)
);

CREATE TABLE public.nulm_susv_renew_familiy_detail (
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
	CONSTRAINT nulm_susv_renew_familiy_detail_pkey PRIMARY KEY (uuid),
	CONSTRAINT "FK_nulm_susv_renew_familiy_detail_application_uuid" FOREIGN KEY (application_uuid) REFERENCES nulm_susv_renew_application_detail(application_uuid)
);