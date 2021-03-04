ALTER TABLE eg_ws_connection
add COLUMN proposedUsage_category character varying(64);

ALTER TABLE eg_ws_savebilling 
add COLUMN receiptDate bigint;
