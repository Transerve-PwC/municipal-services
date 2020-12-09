CREATE TABLE public.rti_ministry_officer_mapping (
	username varchar(50) NULL,
	user_id int8 NULL,
	ministry_code varchar(5) NULL,
	effective_from date NULL,
	effective_to date NULL,
	user_tenantid varchar(50) NULL
);

ALTER TABLE public.rti_ministry_officer_mapping ADD CONSTRAINT fk_rti_ministry_officer_mapping1 FOREIGN KEY (user_id, user_tenantid) REFERENCES eg_user(id, tenantid);
ALTER TABLE public.rti_ministry_officer_mapping ADD CONSTRAINT fk_rti_ministry_officer_mapping2 FOREIGN KEY (ministry_code) REFERENCES rti_ministry_master(ministry_code);
