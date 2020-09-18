CREATE TABLE eg_ws_property
(
	id character varying(64) NOT NULL,
	tenantid character varying(64) NOT NULL,
	property_id character varying(64) NOT NULL,
	usagecategory character varying(64) NOT NULL,
	usagesubcategory character varying(64),
	createdby character varying(64),
	lastmodifiedby character varying(64),
	createdtime bigint,
	lastmodifiedtime bigint,
	CONSTRAINT eg_ws_application_pkey PRIMARY KEY (id)
)