--> Property Details table
ALTER TABLE cs_ep_property_details_v1
ADD COLUMN demand_type CHARACTER VARYING (256);

ALTER TABLE cs_ep_property_details_v1
ADD COLUMN mm_demand_start_year bigint;

ALTER TABLE cs_ep_property_details_v1
ADD COLUMN mm_demand_start_month bigint;

--> Property Details Audit table
ALTER TABLE cs_ep_property_details_audit_v1
ADD COLUMN demand_type CHARACTER VARYING (256);

ALTER TABLE cs_ep_property_details_audit_v1
ADD COLUMN mm_demand_start_year bigint;

ALTER TABLE cs_ep_property_details_audit_v1
ADD COLUMN mm_demand_start_month bigint;
