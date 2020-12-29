--> MM Demand table
ALTER TABLE cs_ep_mm_demand
ADD COLUMN type_of_demand CHARACTER VARYING (256);

--> MM Demand audit table
ALTER TABLE cs_ep_mm_demand_audit
ADD COLUMN type_of_demand CHARACTER VARYING (256);
