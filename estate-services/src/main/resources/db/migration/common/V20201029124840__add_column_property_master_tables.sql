ALTER TABLE cs_ep_property_v1
ADD COLUMN bank_name character varying(64);
ALTER TABLE cs_ep_property_v1
ADD COLUMN transaction_number character varying(64);
ALTER TABLE cs_ep_property_v1
ADD COLUMN amount numeric(12,2);
ALTER TABLE cs_ep_property_v1
ADD COLUMN date_of_payment bigint;

ALTER TABLE cs_ep_property_audit_v1
ADD COLUMN bank_name character varying(64);
ALTER TABLE cs_ep_property_audit_v1
ADD COLUMN transaction_number character varying(64);
ALTER TABLE cs_ep_property_audit_v1
ADD COLUMN amount numeric(12,2);
ALTER TABLE cs_ep_property_audit_v1
ADD COLUMN date_of_payment bigint;
