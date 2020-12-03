ALTER TABLE cs_ep_offline_payment_detail
ADD COLUMN created_by CHARACTER VARYING (128);

ALTER TABLE cs_ep_offline_payment_detail
ADD COLUMN last_modified_by CHARACTER VARYING (128);

ALTER TABLE cs_ep_offline_payment_detail
ADD COLUMN created_time bigint;

ALTER TABLE cs_ep_offline_payment_detail
ADD COLUMN last_modified_time bigint;
