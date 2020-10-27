ALTER TABLE cs_ep_demand 
ADD COLUMN IF NOT EXISTS remaining_rent_penalty numeric(13,6);

ALTER TABLE cs_ep_demand 
ADD COLUMN IF NOT EXISTS remaining_gst_penalty numeric(13,6);

ALTER TABLE cs_ep_demand 
ADD COLUMN IF NOT EXISTS remaining_rent numeric(13,6);

ALTER TABLE cs_ep_demand 
ADD COLUMN IF NOT EXISTS collected_gst_penalty numeric(13,6);

ALTER TABLE cs_ep_demand 
ADD COLUMN IF NOT EXISTS collected_rent_penalty numeric(13,6);

ALTER TABLE cs_ep_demand 
ADD COLUMN IF NOT EXISTS interest_since numeric(13,6);

--Audit table

ALTER TABLE cs_ep_demand_audit 
ADD COLUMN IF NOT EXISTS remaining_rent_penalty numeric(13,6);

ALTER TABLE cs_ep_demand_audit 
ADD COLUMN IF NOT EXISTS remaining_gst_penalty numeric(13,6);

ALTER TABLE cs_ep_demand_audit 
ADD COLUMN IF NOT EXISTS remaining_rent numeric(13,6);

ALTER TABLE cs_ep_demand_audit 
ADD COLUMN IF NOT EXISTS collected_gst_penalty numeric(13,6);

ALTER TABLE cs_ep_demand_audit 
ADD COLUMN IF NOT EXISTS collected_rent_penalty numeric(13,6);

ALTER TABLE cs_ep_demand_audit 
ADD COLUMN IF NOT EXISTS interest_since numeric(13,6);