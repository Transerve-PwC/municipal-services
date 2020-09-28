
alter table nulm_sep_application_detail
DROP COLUMN bank_details,
DROP COLUMN recommended_amount;

alter table nulm_sep_application_detail
ADD COLUMN account_name character varying(255),
ADD COLUMN bank_name character varying(255),
ADD COLUMN branch_name character varying(255),
ADD COLUMN is_disability_certificate_available Boolean,
ADD COLUMN task_committee_approved_amount character varying(255),
ADD COLUMN task_committee_remark character varying(255),
ADD COLUMN task_committee_action_date timestamp without time zone,
ADD COLUMN task_committee_status character varying(255),
ADD COLUMN committee_bank_name character varying(255),
ADD COLUMN committee_branch_name character varying(255),
ADD COLUMN application_forwarded_on_date timestamp without time zone,,
ADD COLUMN sanction_date timestamp without time zone,
ADD COLUMN sanction_remarks character varying(255);


 
alter table nulm_suh_application_detail
ADD COLUMN assigned_to character varying(64) ;



ALTER TABLE public.nulm_organization ADD CONSTRAINT  nulm_organization_uuidt UNIQUE (organization_uuid);
ALTER TABLE public.nulm_suh_application_detail ADD CONSTRAINT fk_nulm_suh_application_detail FOREIGN KEY (assigned_to) REFERENCES public.nulm_organization(organization_uuid);


alter table public.nulm_smid_application_detail
DROP COLUMN document_attachemnt;

alter table public.nulm_smid_application_detail
ADD COLUMN document_attachemnt jsonb;

alter table public.nulm_smid_application_detail
ADD COLUMN is_registered boolean;

alter table public.nulm_smid_application_detail
ADD COLUMN cob_number character varying(255);