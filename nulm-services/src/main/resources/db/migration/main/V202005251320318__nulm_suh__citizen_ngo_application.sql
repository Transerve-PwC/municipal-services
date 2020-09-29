
CREATE TABLE public.nulm_suh_citizenngo_application
(
  suh_citizen_ngo_uuid character varying(256) NOT NULL,
  shelter_requested_for_person character varying(256),
  address character varying(256),
  gender character varying(10),
  age integer,
  reason_for_staying character varying(256),
  is_disabled boolean,
  nominated_by character varying(256),
  name_of_nominated_person character varying(256),
  contact_no character varying(256),
  is_active boolean,
  created_by character varying(64),
  created_time bigint,
  last_modified_by character varying(64),
  last_modified_time bigint,
  CONSTRAINT nulm_suh_citizenngo_application_pkey PRIMARY KEY (suh_citizen_ngo_uuid)
)