--> Offline Payment Details Property Table

CREATE TABLE cs_ep_offline_payment_detail (
   id           				  CHARACTER VARYING (256) NOT NULL,
   property_details_id    CHARACTER VARYING (256),
   demand_id              CHARACTER VARYING (256),
   amount                 numeric(13,6),
   bank_name              CHARACTER VARYING (100),
   transaction_number	    CHARACTER VARYING (100),
   date_of_payment				bigint,
   type						CHARACTER VARYING (30),

  CONSTRAINT pk_cs_ep_offline_payment_detail PRIMARY KEY (id),
  CONSTRAINT fk_cs_ep_offline_payment_detail FOREIGN KEY (property_details_id) REFERENCES cs_ep_property_details_v1 (id)
);