CREATE TABLE eg_ws_application
(
	id character varying(64) NOT NULL,
	wsid character varying(64) NOT NULL,
	tenantid character varying(64) NOT NULL,
	applicationno character varying(64) NOT NULL,
	activitytype character varying(64),
	applicationstatus character varying(64),
	action character varying(64),
	comments character varying(500),
	createdby character varying(64),
	lastmodifiedby character varying(64),
	createdtime bigint,
	lastmodifiedtime bigint,
	CONSTRAINT eg_ws_application_pkey PRIMARY KEY (id),
	CONSTRAINT fk_eg_ws_application_connection_id FOREIGN KEY (wsid)
		REFERENCES eg_ws_connection (id) ON UPDATE CASCADE ON DELETE CASCADE
)