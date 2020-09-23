CREATE TABLE eg_ws_property
(
	id character varying(64) NOT NULL,
	tenantid character varying(64) NOT NULL,
	property_id character varying(64) NOT NULL,
	wsid character varying(64) NOT NULL,
	usagecategory character varying(64) NOT NULL,
	usagesubcategory character varying(64),
	createdby character varying(64),
	lastmodifiedby character varying(64),
	createdtime bigint,
	lastmodifiedtime bigint,
	CONSTRAINT eg_ws_property_pkey PRIMARY KEY (id),
	CONSTRAINT fk_eg_ws_property_connection_id FOREIGN KEY (wsid)
		REFERENCES eg_ws_connection (id) ON UPDATE CASCADE ON DELETE CASCADE
)
