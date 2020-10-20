DROP TABLE IF EXISTS cs_ep_application_v1;

CREATE TABLE cs_ep_application_v1 (
   id           			CHARACTER VARYING (256) NOT NULL,
   tenantid			    	CHARACTER VARYING (256),
   property_id       		CHARACTER VARYING (256) NOT NULL,
   application_number   	CHARACTER VARYING (256) NOT NULL,
   branch_type				CHARACTER VARYING (256),
   module_type        		CHARACTER VARYING (256),
   application_type			CHARACTER VARYING (256),
   comments   				CHARACTER VARYING (256),
   hardcopy_received_date 	bigint,
   state   					CHARACTER VARYING (256),
   action    				CHARACTER VARYING (256),
  
   created_by           	CHARACTER VARYING (128) NOT NULL,
   last_modified_by     	CHARACTER VARYING (128),
   created_time         	bigint NOT NULL,
   last_modified_time   	bigint,

  CONSTRAINT pk_cs_ep_application_v1 PRIMARY KEY (id, property_id),
  CONSTRAINT fk_cs_ep_application_v1 FOREIGN KEY (property_id) REFERENCES cs_ep_property_v1 (id)
);
CREATE INDEX IF NOT EXISTS application_index ON cs_ep_application_v1 (application_number, state);
