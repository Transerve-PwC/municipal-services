ALTER TABLE cs_ep_demand 
ADD COLUMN IF NOT EXISTS remaining_gst numeric(13,6);

--Audit table
ALTER TABLE cs_ep_demand_audit 
ADD COLUMN IF NOT EXISTS remaining_gst numeric(13,6);