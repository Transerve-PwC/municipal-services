DROP TABLE IF EXISTS cs_pt_dueamount;

--> Due amount tables

CREATE TABLE cs_pt_dueamount (
   property_id          CHARACTER VARYING (256),
   transit_number       CHARACTER VARYING (256),
   tenantid			    CHARACTER VARYING (256),
   colony           	CHARACTER VARYING (256),
   owner_name			CHARACTER VARYING (256),
   mobile_number		CHARACTER VARYING (15),
   remaining_principal   numeric(13,6),
   balance_interest      numeric(13,6),
   balance_amount        numeric(13,6),
   CONSTRAINT uk_cs_pt_dueamount UNIQUE (property_id),
   CONSTRAINT fk_cs_pt_dueamount FOREIGN KEY (property_id) REFERENCES cs_pt_property_v1 (id)
);
