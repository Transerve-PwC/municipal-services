

CREATE TABLE public.eg_sw_connectionholder (
	tenantid varchar(256) ,
	connectionid varchar(128) NOT NULL,
	status varchar(128),
	userid varchar(128) ,
	isprimaryholder bool ,
	connectionholdertype varchar(256) ,
	holdershippercentage varchar(128) ,
	relationship varchar(128) ,
	createdby varchar(128) ,
	createdtime int8 ,
	lastmodifiedby varchar(128) ,
	lastmodifiedtime int8 ,
	mobile_no varchar(15) ,
	name varchar(50) ,
	gender varchar(6) ,
	guardian_name varchar(50) ,
	correspondance_address varchar(100) ,
	CONSTRAINT eg_sw_connectionholder_connectionid_key UNIQUE (connectionid)
);


-- public.eg_ws_connectionholder foreign keys

ALTER TABLE public.eg_sw_connectionholder ADD CONSTRAINT fk_eg_sw_connectionholder FOREIGN KEY (connectionid) REFERENCES eg_sw_connection(id);

alter table eg_sw_connection
add column	cccode varchar(8) ,
add column	div varchar(8) ,
add column	subdiv varchar(8) ,
add column	ledger_no varchar(8) ,
add column	ledgergroup varchar(8) ,
add column	billgroup varchar(32) ,
add column	contract_value varchar(64) ;


alter table eg_sw_connection_audit
add column	cccode varchar(8) ,
add column	div varchar(8) ,
add column	subdiv varchar(8) ,
add column	ledger_no varchar(8) ,
add column	ledgergroup varchar(8) ,
add column	billgroup varchar(32) ,
add column	contract_value varchar(64) ;

alter table eg_sw_service
add column	metercount varchar(64) ,
add column	meterrentcode varchar ,
add column	mfrcode varchar(32) ,
add column	meterdigits varchar(64) ,
add column	meterunit varchar(64) ,
add column	sanctionedcapacity varchar(128) ;


alter table eg_sw_service_audit
add column	metercount varchar(64) ,
add column	meterrentcode varchar ,
add column	mfrcode varchar(32) ,
add column	meterdigits varchar(64) ,
add column	meterunit varchar(64) ,
add column	sanctionedcapacity varchar(128) ;