ALTER TABLE cs_ep_account DROP CONSTRAINT fk_cs_ep_account_property;


ALTER TABLE cs_ep_account 
DROP COLUMN IF EXISTS property_id;

ALTER TABLE cs_ep_account 
ADD COLUMN property_details_id  CHARACTER VARYING (256) NOT NULL;


ALTER TABLE cs_ep_account ADD CONSTRAINT fk_cs_ep_account_property_details
FOREIGN KEY (property_details_id) REFERENCES cs_ep_property_details_v1 (id);