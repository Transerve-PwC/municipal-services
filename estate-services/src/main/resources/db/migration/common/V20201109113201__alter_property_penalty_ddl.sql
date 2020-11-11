--> Alter penalty demand
ALTER TABLE cs_ep_property_penalty_v1
ADD COLUMN generation_date bigint;

ALTER TABLE cs_ep_property_penalty_v1
ADD COLUMN remaining_penalty_due numeric(13,6);

ALTER TABLE cs_ep_property_penalty_v1
ADD COLUMN status CHARACTER VARYING (100);
