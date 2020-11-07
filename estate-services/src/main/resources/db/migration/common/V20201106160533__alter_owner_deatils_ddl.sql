--> Owner Details table
ALTER TABLE cs_ep_owner_details_v1
ADD COLUMN is_previous_owner_required BOOLEAN;

--> Owner Details Audit table
ALTER TABLE cs_ep_owner_details_audit_v1
ADD COLUMN is_previous_owner_required BOOLEAN;