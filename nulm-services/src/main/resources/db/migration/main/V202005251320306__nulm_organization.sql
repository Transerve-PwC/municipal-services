CREATE TABLE nulm_organization (
	organization_uuid varchar(64) NOT NULL,
	user_id int8 NOT NULL,
	organization_name varchar(64) NULL,
	address varchar(64) NULL,
	email_id varchar(64) NULL,
	representative_name varchar(64) NULL,
	mobile_no varchar(64) NULL,
	registration_no varchar(64) NULL,
	tenant_id varchar(256) NOT NULL,
	is_active bool NULL,
	created_by varchar(64) NULL,
	created_time int8 NULL,
	last_modified_by varchar(64) NULL,
	last_modified_time int8 NULL,
	CONSTRAINT nulm_organization_name_tenant UNIQUE (organization_name, tenant_id),
	CONSTRAINT nulm_organization_pkey PRIMARY KEY (organization_uuid, tenant_id),
	CONSTRAINT nulm_organization_uuidt UNIQUE (organization_uuid)
);