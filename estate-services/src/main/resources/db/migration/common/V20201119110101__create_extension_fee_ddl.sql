--> Extension Fee Table

CREATE TABLE cs_ep_extension_fee_v1 (
   id           		CHARACTER VARYING (256) NOT NULL,
   tenantid    			CHARACTER VARYING (256),
   property_id         	CHARACTER VARYING (256),
   branch_type		    CHARACTER VARYING (256),
   amount              	numeric(13,6),
   remaining_due        numeric(13,6),
   paid              	boolean,
   status               CHARACTER VARYING (100),
   generation_date      bigint,
   
   created_by           CHARACTER VARYING (128) NOT NULL,
   last_modified_by     CHARACTER VARYING (128),
   created_time         bigint NOT NULL,
   last_modified_time   bigint,
   

  CONSTRAINT pk_cs_ep_extension_fee_v1 PRIMARY KEY (id),
  CONSTRAINT fk_cs_ep_extension_fee_v1 FOREIGN KEY (property_id) REFERENCES cs_ep_property_v1 (id)
);