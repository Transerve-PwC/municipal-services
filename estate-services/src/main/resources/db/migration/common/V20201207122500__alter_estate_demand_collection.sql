--> Estate Demand table is_bifurcate
ALTER TABLE cs_ep_demand
ADD COLUMN is_bifurcate boolean;

ALTER TABLE cs_ep_demand_audit
ADD COLUMN is_bifurcate boolean;


--> Estate Rent Collection  

ALTER TABLE cs_ep_collection
ADD COLUMN gst_collected numeric(13,6);

ALTER TABLE cs_ep_collection
ADD COLUMN gst_penalty_collected numeric(13,6);

ALTER TABLE cs_ep_collection
ADD COLUMN rent_penalty_collected numeric(13,6);