DROP TABLE IF EXISTS cs_ep_auction;

CREATE TABLE cs_ep_auction
(
  id CHARACTER VARYING (256) NOT NULL,
  property_details_id CHARACTER VARYING (256) NOT NULL,
  auction_id CHARACTER VARYING (256) NOT NULL,
  description character varying(250),
  bidder_name character varying(15),
  deposited_emd_amount numeric(12,2),
  deposit_date bigint,
  emd_validity_date bigint,
  refund_status VARCHAR(256)NOT NULL,
  state VARCHAR(256),
  action VARCHAR(256), 
  comments character varying(250),
  created_by VARCHAR(256)NOT NULL,
  last_modified_by VARCHAR(256)NOT NULL,
  created_time bigint,
  last_modified_time bigint,
  
  CONSTRAINT pk_cs_ep_auction PRIMARY KEY (id),
  CONSTRAINT fk_cs_ep_auction FOREIGN KEY (property_details_id) REFERENCES cs_ep_property_details_v1 (id)
  ON UPDATE CASCADE
  ON DELETE CASCADE	 
 );