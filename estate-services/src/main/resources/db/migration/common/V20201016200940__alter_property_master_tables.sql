ALTER TABLE cs_ep_property_details_v1
ADD COLUMN annual_license_renewal_charges numeric(12,2);
ALTER TABLE cs_ep_property_details_v1
ADD COLUMN monthly_rent_amount numeric(12,2);
ALTER TABLE cs_ep_property_details_v1
ADD COLUMN due_amount numeric(12,2);
ALTER TABLE cs_ep_property_details_v1
ADD COLUMN registration_number character varying(64);
ALTER TABLE cs_ep_property_details_v1
ADD COLUMN registration_date bigint;

ALTER TABLE cs_ep_property_details_audit_v1
ADD COLUMN annual_license_renewal_charges numeric(12,2);
ALTER TABLE cs_ep_property_details_audit_v1
ADD COLUMN monthly_rent_amount numeric(12,2);
ALTER TABLE cs_ep_property_details_audit_v1
ADD COLUMN due_amount numeric(12,2);
ALTER TABLE cs_ep_property_details_audit_v1
ADD COLUMN registration_number character varying(64);
ALTER TABLE cs_ep_property_details_audit_v1
ADD COLUMN registration_date bigint;
