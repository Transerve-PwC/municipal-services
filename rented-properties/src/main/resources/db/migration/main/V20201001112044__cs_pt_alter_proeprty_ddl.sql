ALTER TABLE cs_pt_property_v1 
ADD COLUMN IF NOT EXISTS bank_name CHARACTER VARYING (256);

--Audit table

ALTER TABLE cs_pt_property_audit_v1 
ADD COLUMN IF NOT EXISTS bank_name CHARACTER VARYING (256);
