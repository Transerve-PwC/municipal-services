CREATE TABLE public.nulm_susv_renew_application_detail
(
    application_uuid character varying(64) COLLATE pg_catalog."default" NOT NULL,
    application_id character varying(64) COLLATE pg_catalog."default" NOT NULL,
    application_status character varying(64) COLLATE pg_catalog."default",
    looking_for character varying(255) COLLATE pg_catalog."default",
    name_of_street_vendor character varying(255) COLLATE pg_catalog."default",
    cov_no character varying(255) COLLATE pg_catalog."default",
    residential_address character varying(255) COLLATE pg_catalog."default",
    change_of_location boolean,
    proposed_address character varying(255) COLLATE pg_catalog."default",
    name_of_proposed_new_street_vendor character varying(255) COLLATE pg_catalog."default",
    tenant_id character varying(256) COLLATE pg_catalog."default",
    is_active boolean,
    created_by character varying(64) COLLATE pg_catalog."default",
    created_time bigint,
    last_modified_by character varying(64) COLLATE pg_catalog."default",
    last_modified_time bigint,
    CONSTRAINT nulm_susv_renew_application_detail_pkey PRIMARY KEY (application_uuid),
    CONSTRAINT nulm_susv_renew_application_detail_application_id_key UNIQUE (application_id)

);

CREATE TABLE public.nulm_susv_renew_application_document
(
    document_uuid character varying(64) COLLATE pg_catalog."default" NOT NULL,
    filestore_id character varying COLLATE pg_catalog."default" NOT NULL,
    application_uuid character varying(64) COLLATE pg_catalog."default" NOT NULL,
    document_type character varying(256) COLLATE pg_catalog."default" NOT NULL,
    tenant_id character varying(256) COLLATE pg_catalog."default",
    is_active boolean NOT NULL,
    created_by character varying(64) COLLATE pg_catalog."default",
    created_time bigint,
    last_modified_by character varying(256) COLLATE pg_catalog."default",
    last_modified_time bigint,
    CONSTRAINT nulm_susv_renew_application_document_pkey PRIMARY KEY (document_uuid),
    CONSTRAINT "FK_nulm_susv_renew_application_document_application_uuid" FOREIGN KEY (application_uuid)
        REFERENCES public.nulm_susv_renew_application_detail (application_uuid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE public.nulm_susv_renew_familiy_detail
(
    uuid character varying(64) COLLATE pg_catalog."default" NOT NULL,
    application_uuid character varying(64) COLLATE pg_catalog."default" NOT NULL,
    name character varying(64) COLLATE pg_catalog."default",
    age character varying(64) COLLATE pg_catalog."default",
    relation character varying(64) COLLATE pg_catalog."default",
    tenant_id character varying(256) COLLATE pg_catalog."default",
    is_active boolean,
    created_by character varying(64) COLLATE pg_catalog."default",
    created_time bigint,
    last_modified_by character varying(64) COLLATE pg_catalog."default",
    last_modified_time bigint,
    CONSTRAINT nulm_susv_renew_familiy_detail_pkey PRIMARY KEY (uuid),
    CONSTRAINT "FK_nulm_susv_renew_familiy_detail_application_uuid" FOREIGN KEY (application_uuid)
        REFERENCES public.nulm_susv_renew_application_detail (application_uuid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
