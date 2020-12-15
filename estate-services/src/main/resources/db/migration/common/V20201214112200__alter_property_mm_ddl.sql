--> Property Details table
ALTER TABLE cs_ep_property_details_v1
ADD COLUMN demand_type CHARACTER VARYING (256);

--> Property Details Audit table
ALTER TABLE cs_ep_property_details_audit_v1
ADD COLUMN demand_type CHARACTER VARYING (256);
