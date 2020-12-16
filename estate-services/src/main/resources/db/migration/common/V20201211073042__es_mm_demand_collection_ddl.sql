--> Demand table
CREATE TABLE cs_ep_mm_demand (
   id           			    CHARACTER VARYING (256) NOT NULL,
   property_details_id        	CHARACTER VARYING (256) NOT NULL,
   demand_date   			    bigint,
   paid 					    numeric(13,6),
   rent  					    numeric(13,6),
   gst 						    numeric(13,6),
   collected_rent 			   	numeric(13,6),
   collected_gst			    numeric(13,6),
   comment						CHARACTER VARYING (256),
   
   created_by           		CHARACTER VARYING (128) NOT NULL,
   last_modified_by     		CHARACTER VARYING (128),
   created_time         		bigint NOT NULL,
   last_modified_time   		bigint,
  
  CONSTRAINT pk_cs_ep_mm_demand PRIMARY KEY (id),
  CONSTRAINT fk_cs_ep_mm_demand FOREIGN KEY (property_details_id) REFERENCES cs_ep_property_details_v1 (id)
);

--> Payment Table
CREATE TABLE cs_ep_mm_payment (
   id           		   	CHARACTER VARYING (256) NOT NULL,
   property_details_id     	CHARACTER VARYING (256) NOT NULL,
   receipt_date	    	   	bigint,
   rent_received   		   	numeric(13,6),
   receipt_no   			CHARACTER VARYING (256),
   payment_date	    	   	bigint,
   processed   		   		boolean,
  
   created_by           	CHARACTER VARYING (128) NOT NULL,
   last_modified_by     	CHARACTER VARYING (128),
   created_time         	bigint NOT NULL,
   last_modified_time   	bigint,
	
  CONSTRAINT pk_cs_ep_mm_payment PRIMARY KEY (id), 
  CONSTRAINT fk_cs_ep_mm_payment FOREIGN KEY (property_details_id) REFERENCES cs_ep_property_details_v1 (id)
);

--> Collection Table
CREATE TABLE cs_ep_mm_collection (
   id           			CHARACTER VARYING (256) NOT NULL,
   demand_id            	CHARACTER VARYING (256) NOT NULL,
   payment_id            	CHARACTER VARYING (256),
   rent_collected		   	numeric(13,6),
   gst_collected    		numeric(13,6),
   collected_at				bigint,	
  
   created_by           	CHARACTER VARYING (128) NOT NULL,
   last_modified_by     	CHARACTER VARYING (128),
   created_time         	bigint NOT NULL,
   last_modified_time   	bigint,

  CONSTRAINT pk_cs_ep_mm_collection PRIMARY KEY (id), 
  CONSTRAINT fk_cs_ep_mm_collection_demand FOREIGN KEY (demand_id) REFERENCES cs_ep_mm_demand (id) ON DELETE CASCADE
);

--> Audit Tables
CREATE TABLE cs_ep_mm_demand_audit(
   id           			    CHARACTER VARYING (256) NOT NULL,
   property_details_id        	CHARACTER VARYING (256) NOT NULL,
   demand_date   			    bigint,
   paid 					    numeric(13,6),
   rent  					    numeric(13,6),
   gst 						    numeric(13,6),
   collected_rent 			   	numeric(13,6),
   collected_gst			    numeric(13,6),
   
   created_by           		CHARACTER VARYING (128) NOT NULL,
   last_modified_by     		CHARACTER VARYING (128),
   created_time         		bigint NOT NULL,
   last_modified_time   		bigint
);
