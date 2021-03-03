--> NP Number in Owner Table

ALTER TABLE cs_ep_owner_v1
ADD COLUMN np_number CHARACTER VARYING (256);

ALTER TABLE cs_ep_owner_audit_v1
ADD COLUMN np_number CHARACTER VARYING (256);
