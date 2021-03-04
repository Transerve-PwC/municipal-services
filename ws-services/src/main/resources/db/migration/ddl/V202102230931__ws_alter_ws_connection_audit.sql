ALTER TABLE eg_ws_connection_audit
add COLUMN proposedUsage_category character varying(64);

ALTER TABLE eg_ws_savebilling_audit 
add COLUMN receiptDate bigint;
