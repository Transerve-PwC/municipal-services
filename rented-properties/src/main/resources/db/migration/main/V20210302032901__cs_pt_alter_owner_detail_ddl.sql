--Adding column to check apro chanrges paid or not

ALTER TABLE cs_pt_ownershipdetails_v1 
ADD COLUMN IF NOT EXISTS isapro_charge_paid BOOLEAN;

ALTER TABLE cs_pt_ownershipdetails_audit_v1 
ADD COLUMN IF NOT EXISTS isapro_charge_paid BOOLEAN;