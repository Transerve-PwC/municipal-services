CREATE TABLE public.nulm_susv_application_document (
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
	CONSTRAINT nulm_usv_application_document_pkey PRIMARY KEY (document_uuid),
	CONSTRAINT susv_application_uuid_document_type UNIQUE (application_uuid, document_type, tenant_id)
);