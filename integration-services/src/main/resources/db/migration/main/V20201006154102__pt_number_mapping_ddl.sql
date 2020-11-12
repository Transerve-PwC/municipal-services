CREATE TABLE public.pt_citizen_mapping (
	uuid varchar(64) NOT NULL,
	user_id int8 NULL,
	property_tax_id varchar(100) NULL,
	tenant_id varchar(50) NULL,
	is_active bool NOT NULL,
	created_by varchar(64) NULL,
	created_time int8 NULL,
	last_modified_by varchar(64) NULL,
	last_modified_time int8 NULL,
	CONSTRAINT pt_citizen_mapping_pkey PRIMARY KEY (uuid),
	CONSTRAINT fk_pt_citizen_mapping_userid FOREIGN KEY (user_id,tenant_id) REFERENCES eg_user(id,tenantid)
);

ALTER TABLE public.pt_citizen_mapping ADD CONSTRAINT pt_citizen_mapping_Unique UNIQUE (user_id, property_tax_id);