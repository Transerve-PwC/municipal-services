DROP TABLE IF EXISTS cs_ep_account;
DROP TABLE IF EXISTS cs_ep_collection;

--> Account Table
CREATE TABLE cs_ep_account (
   id           		CHARACTER VARYING (256) NOT NULL,
   property_details_id  		CHARACTER VARYING (256) NOT NULL,
   remainingamount	    numeric(13,6),
   remaining_since      bigint,
  
   created_by           CHARACTER VARYING (128),
   created_date         bigint,
   modified_by     		CHARACTER VARYING (128),
   modified_date       	bigint,
	
  CONSTRAINT pk_cs_ep_account PRIMARY KEY (id), 
  CONSTRAINT fk_cs_ep_account_property_details FOREIGN KEY (property_details_id) REFERENCES cs_ep_property_details_v1 (id)
);

--> Collection Table
CREATE TABLE cs_ep_collection (
   id           		CHARACTER VARYING (256) NOT NULL,
   demand_id            CHARACTER VARYING (256) NOT NULL,
   interestCollected    numeric(13,6),
   principalCollected   numeric(13,6),
  
   created_by           CHARACTER VARYING (128),
   created_date         bigint,
   collectedAt         bigint,
   modified_by     		CHARACTER VARYING (128),
   modified_date       	bigint,

  CONSTRAINT pk_cs_ep_collection PRIMARY KEY (id), 
  CONSTRAINT fk_cs_ep_collection_demand FOREIGN KEY (demand_id) REFERENCES cs_ep_demand (id) ON DELETE CASCADE
);