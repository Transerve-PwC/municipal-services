--> AOS Payment Table
CREATE TABLE cs_ep_payment_config_v1 (
   id           		   	      CHARACTER VARYING (256) NOT NULL,
   tenant_id     	               CHARACTER VARYING (256),
   property_details_id     	   CHARACTER VARYING (256) NOT NULL,
   is_intrest_applicable	      BOOLEAN,
   due_date_of_payment   		   bigint,
   no_of_months   			      bigint,
   rate_of_interest   			   numeric(13,6),
   security_amount   			   numeric(13,6),
   total_amount   			      numeric(13,6),
   is_ground_rent   			      BOOLEAN,
   ground_rent_generation_type	CHARACTER VARYING (256),
   ground_rent_generate_demand	bigint,
   ground_rent_advance_rent	   numeric(13,6),
   ground_rent_bill_start_date	bigint,
   ground_rent_advance_rent_date bigint,
  
   created_by           	      CHARACTER VARYING (128) NOT NULL,
   last_modified_by     	      CHARACTER VARYING (128),
   created_time         	      bigint NOT NULL,
   last_modified_time   	      bigint,
	
  CONSTRAINT pk_cs_ep_payment_config_v1 PRIMARY KEY (id), 
  CONSTRAINT fk_cs_ep_payment_config_v1 FOREIGN KEY (property_details_id) REFERENCES cs_ep_property_details_v1 (id)
);


--> Audit Tables
CREATE TABLE cs_ep_payment_config_audit_v1(
   id           		   	      CHARACTER VARYING (256) NOT NULL,
   tenant_id     	               CHARACTER VARYING (256),
   property_details_id     	   CHARACTER VARYING (256) NOT NULL,
   is_intrest_applicable	      BOOLEAN,
   due_date_of_payment   		   bigint,
   no_of_months   			      bigint,
   rate_of_interest   			   numeric(13,6),
   security_amount   			   numeric(13,6),
   total_amount   			      numeric(13,6),
   is_ground_rent   			      BOOLEAN,
   ground_rent_generation_type	CHARACTER VARYING (256),
   ground_rent_advance_rent	   numeric(13,6),
   ground_rent_bill_start_date	bigint,
   ground_rent_advance_rent_date bigint,
  
   created_by           	      CHARACTER VARYING (128) NOT NULL,
   last_modified_by     	      CHARACTER VARYING (128),
   created_time         	      bigint NOT NULL,
   last_modified_time   	      bigint
);

--> Ground Rent Config Table
CREATE TABLE cs_ep_payment_config_items_v1 (
   id           		   	      CHARACTER VARYING (256) NOT NULL,
   tenant_id     	               CHARACTER VARYING (256),
   payment_config_id     	      CHARACTER VARYING (256) NOT NULL,
   ground_rent_amount            numeric(13,6),
   ground_rent_start_month	      bigint,
   ground_rent_end_month	      bigint,
	
  CONSTRAINT pk_cs_ep_payment_config_items_v1 PRIMARY KEY (id), 
  CONSTRAINT fk_cs_ep_payment_config_items_v1 FOREIGN KEY (payment_config_id) REFERENCES cs_ep_payment_config_v1 (id)
);

--> Premium Amount Config Table
CREATE TABLE cs_ep_premium_amount_config_items_v1 (
   id           		   	   CHARACTER VARYING (256) NOT NULL,
   tenant_id     	            CHARACTER VARYING (256),
   payment_config_id     	   CHARACTER VARYING (256) NOT NULL,
   premium_amount           	numeric(13,6),
   premiumAmountDate       	bigint,
	
  CONSTRAINT pk_cs_ep_premium_amount_config_items_v1 PRIMARY KEY (id), 
  CONSTRAINT fk_cs_ep_premium_amount_config_items_v1 FOREIGN KEY (payment_config_id) REFERENCES cs_ep_payment_config_v1 (id)
);