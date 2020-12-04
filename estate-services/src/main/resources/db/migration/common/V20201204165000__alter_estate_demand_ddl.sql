--> Estate Demand table is_adjustment
ALTER TABLE cs_ep_demand
ADD COLUMN comment CHARACTER VARYING (256);

ALTER TABLE cs_ep_demand_audit
ADD COLUMN comment CHARACTER VARYING (256);