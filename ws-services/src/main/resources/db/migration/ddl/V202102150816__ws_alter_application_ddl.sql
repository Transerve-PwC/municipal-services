ALTER TABLE eg_ws_connection 
add COLUMN billGroup character varying(32),
add COLUMN contract_value character varying(64);

ALTER TABLE eg_ws_connection_audit 
add COLUMN billGroup character varying(32),
add COLUMN contract_value character varying(64);

ALTER TABLE eg_ws_application 
add COLUMN is_ferrule_applicable boolean,
add COLUMN security_charges numeric;