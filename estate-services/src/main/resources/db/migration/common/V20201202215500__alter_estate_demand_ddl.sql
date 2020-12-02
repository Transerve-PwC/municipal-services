--> Estate Demand table is_adjustment
ALTER TABLE cs_ep_demand
ADD COLUMN is_adjustment BOOLEAN;

ALTER TABLE cs_ep_demand_audit
ADD COLUMN is_adjustment BOOLEAN;

--> Estate Demand table adjustment_date
ALTER TABLE cs_ep_demand
ADD COLUMN adjustment_date bigint;

ALTER TABLE cs_ep_demand_audit
ADD COLUMN adjustment_date bigint;