--> Property table
ALTER TABLE cs_ep_property_v1
ADD COLUMN house_number CHARACTER VARYING (256);

--> Property Audit table
ALTER TABLE cs_ep_property_audit_v1
ADD COLUMN house_number CHARACTER VARYING (256);