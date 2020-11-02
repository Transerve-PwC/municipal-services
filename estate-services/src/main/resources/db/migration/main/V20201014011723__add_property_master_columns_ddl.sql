ALTER TABLE cs_ep_owner_details_v1 
ADD COLUMN IF NOT EXISTS dob bigint;

--Audit table

ALTER TABLE cs_ep_owner_details_audit_v1 
ADD COLUMN IF NOT EXISTS dob bigint;
