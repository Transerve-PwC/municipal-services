DROP TABLE IF EXISTS cs_ep_dueamount;

--> Due amount tables

CREATE TABLE cs_ep_dueamount (
   property_id           CHARACTER VARYING (256),
   file_number       	 CHARACTER VARYING (128),
   tenantid			     CHARACTER VARYING (128),
   branch_type           CHARACTER VARYING (128),
   property_type         CHARACTER VARYING (128),
   sectorNumber          CHARACTER VARYING (128),
   owner_name			 CHARACTER VARYING (256),
   mobile_number		 CHARACTER VARYING (15),
   balance_rent   			numeric(13,6),
   balance_rent_penalty   	numeric(13,6),
   balance_gst				numeric(13,6),
   balance_gst_penalty		numeric(13,6),
   balance_interest      	numeric(13,6),
   balance_amount        	numeric(13,6),
   CONSTRAINT uk_cs_ep_dueamount UNIQUE (property_id),
   CONSTRAINT fk_cs_ep_dueamount FOREIGN KEY (property_id) REFERENCES cs_ep_property_v1 (id)
);
