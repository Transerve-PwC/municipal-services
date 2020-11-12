--> AOS Payment Table
CREATE TABLE cs_ep_payment_v1 (
   id           		   	   CHARACTER VARYING (256) NOT NULL,
   tenant_id     	            CHARACTER VARYING (256),
   property_details_id     	CHARACTER VARYING (256) NOT NULL,
   is_intrest_applicable	   BOOLEAN,
   due_date_of_payment   		bigint,
   no_of_months   			   bigint,
   rate_of_interest   			numeric(13,6),
   securityAmount   			   numeric(13,6),
   totalAmount   			      numeric(13,6),
   isGrOrLi   			         BOOLEAN,
  
   created_by           	   CHARACTER VARYING (128) NOT NULL,
   last_modified_by     	   CHARACTER VARYING (128),
   created_time         	   bigint NOT NULL,
   last_modified_time   	   bigint,
	
  CONSTRAINT pk_cs_ep_payment_v1 PRIMARY KEY (id), 
  CONSTRAINT fk_cs_ep_payment_v1 FOREIGN KEY (property_details_id) REFERENCES cs_ep_property_details_v1 (id)
);


--> Audit Tables
CREATE TABLE cs_ep_payment_audit_v1(
   id           		   	   CHARACTER VARYING (256) NOT NULL,
   tenant_id     	            CHARACTER VARYING (256),
   property_details_id     	CHARACTER VARYING (256) NOT NULL,
   is_intrest_applicable	   BOOLEAN,
   due_date_of_payment   		bigint,
   no_of_months   			   bigint,
   rate_of_interest   			numeric(13,6),
   securityAmount   			   numeric(13,6),
   totalAmount   			      numeric(13,6),
   isGrOrLi   			         BOOLEAN,
  
   created_by           	   CHARACTER VARYING (128) NOT NULL,
   last_modified_by     	   CHARACTER VARYING (128),
   created_time         	   bigint NOT NULL,
   last_modified_time   	   bigint
);

--> Ground Rent Payment Table
CREATE TABLE cs_ep_ground_rent_licence_v1 (
   id           		   	   CHARACTER VARYING (256) NOT NULL,
   tenant_id     	            CHARACTER VARYING (256),
   payment_id     	         CHARACTER VARYING (256) NOT NULL,
   gr_or_li_generation_type	CHARACTER VARYING (256),
   gr_or_li_advance_rent	   numeric(13,6),
   gr_or_li_bill_start_date	bigint,
   gr_or_li_advance_rent_date bigint,
   gr_or_li_amount            numeric(13,6),
   gr_or_li_start_month	      bigint,
   gr_or_li_end_month	      bigint,
	
  CONSTRAINT pk_cs_ep_ground_rent_licence_v1 PRIMARY KEY (id), 
  CONSTRAINT fk_cs_ep_ground_rent_licence_v1 FOREIGN KEY (payment_id) REFERENCES cs_ep_payment_v1 (id)
);

--> Premium Amount Payment Table
CREATE TABLE cs_ep_premium_amount_v1 (
   id           		   	   CHARACTER VARYING (256) NOT NULL,
   tenant_id     	            CHARACTER VARYING (256),
   payment_id     	         CHARACTER VARYING (256) NOT NULL,
   gr_or_li_generation_type	CHARACTER VARYING (256),
   gr_or_li_advance_rent	   numeric(13,6),
   gr_or_li_bill_start_date	bigint,
   gr_or_li_advance_rent_date bigint,
   gr_or_li_amount            numeric(13,6),
   gr_or_li_start_month	      bigint,
   gr_or_li_end_month	      bigint,
	
  CONSTRAINT pk_cs_ep_premium_amoun_v1 PRIMARY KEY (id), 
  CONSTRAINT fk_cs_ep_premium_amoun_v1 FOREIGN KEY (payment_id) REFERENCES cs_ep_payment_v1 (id)
);