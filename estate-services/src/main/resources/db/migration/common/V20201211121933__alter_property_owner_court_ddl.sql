--> Property Details table
ALTER TABLE cs_ep_property_details_v1
ADD COLUMN street CHARACTER VARYING (256);

--> Property Details Audit table
ALTER TABLE cs_ep_property_details_audit_v1
ADD COLUMN street CHARACTER VARYING (256);

--> Owner Details table
ALTER TABLE cs_ep_owner_details_v1
ADD COLUMN is_deleted BOOLEAN;

--> Owner Details Audit table
ALTER TABLE cs_ep_owner_details_audit_v1
ADD COLUMN is_deleted BOOLEAN;

--> Court Cases table
ALTER TABLE cs_ep_court_case_v1
ADD COLUMN is_deleted BOOLEAN;