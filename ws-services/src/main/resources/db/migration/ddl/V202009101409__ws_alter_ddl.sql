ALTER TABLE eg_ws_connection ADD COLUMN securityCharge decimal;
ALTER TABLE eg_ws_connection_audit ADD COLUMN securityCharge decimal;
ALTER TABLE eg_ws_service ALTER COLUMN proposedpipesize TYPE varchar (50);
ALTER TABLE eg_ws_service_audit ALTER COLUMN proposedpipesize TYPE varchar (50);
ALTER TABLE eg_ws_service ALTER COLUMN pipeSize TYPE varchar (50);
ALTER TABLE eg_ws_service_audit ALTER COLUMN pipeSize TYPE varchar (50);
