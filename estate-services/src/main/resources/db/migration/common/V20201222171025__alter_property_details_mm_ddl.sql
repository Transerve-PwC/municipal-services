--> Property Details table
ALTER TABLE cs_ep_property_details_v1
DROP COLUMN demand_type;

--> Property Details Audit table
ALTER TABLE cs_ep_property_details_audit_v1
DROP COLUMN demand_type;
