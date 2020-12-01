CREATE TABLE eg_ws_connection_mapping
(
  user_Id character varying    ,
  wsid character varying,
  createdby character varying(64),
  lastmodifiedby character varying(64),
  createdtime bigint,
  lastmodifiedtime bigint,
  CONSTRAINT fk_eg_ws_connection_mappingconnection_id FOREIGN KEY (wsid)
		REFERENCES eg_ws_connection (id) ON UPDATE CASCADE ON DELETE CASCADE
);