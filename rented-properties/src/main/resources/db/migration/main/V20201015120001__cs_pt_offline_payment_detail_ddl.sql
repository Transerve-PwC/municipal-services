DROP TABLE IF EXISTS cs_pt_offline_payment_detail;

--> Offline Payment Details Property Table

CREATE TABLE cs_pt_offline_payment_detail (
   id           				CHARACTER VARYING (256) NOT NULL,
   property_id       			CHARACTER VARYING (256),
   demand_id                    CHARACTER VARYING (256),
   amount                       numeric(13,6),
   bankname                     CHARACTER VARYING (100),
   transactionnumber	        CHARACTER VARYING (100),

  CONSTRAINT pk_cs_pt_offline_payment_detail PRIMARY KEY (id),
  CONSTRAINT fk_cs_pt_offline_payment_detail FOREIGN KEY (property_id) REFERENCES cs_pt_property_v1 (id)
  ON UPDATE CASCADE
  ON DELETE CASCADE
);