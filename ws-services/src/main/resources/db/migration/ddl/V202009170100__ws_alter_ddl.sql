ALTER TABLE eg_ws_connection ADD COLUMN connectionusagestype character varying(50);
ALTER TABLE eg_ws_connection_audit ADD COLUMN connectionusagestype character varying(50);
ALTER TABLE eg_ws_connection ADD COLUMN inworkflow boolean DEFAULT false;
ALTER TABLE eg_ws_connection_audit ADD COLUMN inworkflow boolean DEFAULT false;
ALTER TABLE eg_ws_applicationdocument RENAME COLUMN wsid to applicationid;
ALTER TABLE eg_ws_applicationdocument DROP CONSTRAINT fk_eg_ws_applicationdocument_connection_id, 
ADD CONSTRAINT fk_eg_ws_applicationdocument_application_id FOREIGN KEY (applicationid)
REFERENCES eg_ws_application (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
