--> Demand table
CREATE TABLE cs_ep_demand (
   id           			      CHARACTER VARYING (256) NOT NULL,
   property_details_id        CHARACTER VARYING (256) NOT NULL,
   demand_date   			      bigint,
   is_previous       		   boolean,
   rent  					      numeric(13,6),
   penalty_interest   		   numeric(13,6),
   gst_interest				   numeric(13,6),
   gst 						      numeric(13,6),
   collected_rent 			   numeric(13,6),
   collected_gst			      numeric(13,6),
   no_of_days 				      numeric(13,6),
   paid 					         numeric(13,6),
   remaining_rent_penalty     numeric(13,6),
   remaining_gst_penalty      numeric(13,6),
   remaining_rent             numeric(13,6),
   collected_gst_penalty      numeric(13,6),
   collected_rent_penalty     numeric(13,6),
   interest_since             bigint,
   remaining_gst              numeric(13,6),
   
   created_by           	CHARACTER VARYING (128) NOT NULL,
   last_modified_by     	CHARACTER VARYING (128),
   created_time         	bigint NOT NULL,
   last_modified_time   	bigint,
  
  CONSTRAINT pk_cs_ep_demand PRIMARY KEY (id),
  CONSTRAINT fk_cs_ep_demand FOREIGN KEY (property_details_id) REFERENCES cs_ep_property_details_v1 (id)
);

--> Payment Table
CREATE TABLE cs_ep_payment (
   id           		   	CHARACTER VARYING (256) NOT NULL,
   property_details_id     	CHARACTER VARYING (256) NOT NULL,
   receipt_date	    	   	bigint,
   rent_received   		   	numeric(13,6),
   receipt_no   			CHARACTER VARYING (256),
  
   created_by           	CHARACTER VARYING (128) NOT NULL,
   last_modified_by     	CHARACTER VARYING (128),
   created_time         	bigint NOT NULL,
   last_modified_time   	bigint,
	
  CONSTRAINT pk_cs_ep_payment PRIMARY KEY (id), 
  CONSTRAINT fk_cs_ep_payment FOREIGN KEY (property_details_id) REFERENCES cs_ep_property_details_v1 (id)
);


--> Audit Tables
CREATE TABLE cs_ep_demand_audit(
   id           			      CHARACTER VARYING (256) NOT NULL,
   property_details_id        CHARACTER VARYING (256) NOT NULL,
   demand_date   		         bigint,
   is_previous       		   boolean,
   rent  					      numeric(13,6),
   penalty_interest   		   numeric(13,6),
   gst_interest				   numeric(13,6),
   gst 						      numeric(13,6),
   collected_rent 			   numeric(13,6),
   collected_gst			      numeric(13,6),
   no_of_days 				      numeric(13,6),
   paid 					         numeric(13,6),
   remaining_rent_penalty     numeric(13,6),
   remaining_gst_penalty      numeric(13,6),
   remaining_rent             numeric(13,6),
   collected_gst_penalty      numeric(13,6),
   collected_rent_penalty     numeric(13,6),
   interest_since             bigint,
   remaining_gst              numeric(13,6),
   
   created_by           	CHARACTER VARYING (128) NOT NULL,
   last_modified_by     	CHARACTER VARYING (128),
   created_time         	bigint NOT NULL,
   last_modified_time   	bigint
);
