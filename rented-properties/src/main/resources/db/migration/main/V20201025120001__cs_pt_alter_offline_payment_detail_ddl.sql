ALTER TABLE cs_pt_offline_payment_detail 
ADD COLUMN IF NOT EXISTS created_by CHARACTER VARYING (128) NOT NULL DEFAULT '0';

ALTER TABLE cs_pt_offline_payment_detail 
ADD COLUMN IF NOT EXISTS created_date CHARACTER VARYING NOT NULL DEFAULT '0';

ALTER TABLE cs_pt_offline_payment_detail 
ADD COLUMN IF NOT EXISTS modified_by CHARACTER VARYING (128);

ALTER TABLE cs_pt_offline_payment_detail 
ADD COLUMN IF NOT EXISTS modified_date CHARACTER VARYING;