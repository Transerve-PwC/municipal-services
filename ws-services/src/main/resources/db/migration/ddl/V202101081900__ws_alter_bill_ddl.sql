DROP TABLE IF EXISTS eg_ws_billfile_history;
CREATE TABLE eg_ws_billfile_history
(
  id character varying(64) NOT NULL,
  filestore_url character varying(250) NOT NULL,
  filestore_id character varying(64) NOT NULL,
  filegeneration_time bigint,
  CONSTRAINT eg_ws_billfile_history_pkey PRIMARY KEY (id)
);


ALTER TABLE eg_ws_connection  
ADD COLUMN cccode varchar,
ADD COLUMN div varchar,
ADD COLUMN subdiv varchar,
ADD COLUMN ledger_no varchar;


ALTER TABLE eg_ws_savebilling
ADD COLUMN totalAmount_paid varchar,
add column paymentMode varchar,
add column isFileGenerated boolean;

DROP TABLE IF EXISTS eg_ws_savebilling_audit;
CREATE TABLE eg_ws_savebilling_audit
(
  id character varying(64) not null,
 	ccCode          character varying    ,
	divSdiv          character varying ,
	consumerCode     character varying ,
	billCycle        character varying ,
	billGroup        character varying ,
	subGroup         character varying ,
	billType         character varying ,
	name             character varying ,
	address          character varying ,
	add1             character varying ,
	add2	         character varying ,
	add3	         character varying ,
	add4	         character varying ,
	add5	         character varying ,
	cessCharge	     character varying ,
	netAmount	     character varying ,
	grossAmount      character varying ,
	surcharge	     character varying ,
	totalNetAmount   character varying ,
	totalSurcharge   character varying ,
	totalGrossAmount character varying ,
	fixChargeCode    character varying ,
	fixCharge        character varying ,
	dueDateCash      character varying ,
	dueDateCheque    character varying ,
	status           character varying ,
	billId           character varying ,
	paymentId        character varying ,
	paymentStatus    character varying ,
  createdby character varying(64),
  lastmodifiedby character varying(64),
  createdtime bigint,
  lastmodifiedtime bigint,
  totalAmount_paid character varying,
  paymentMode varchar,
  isFileGenerated boolean
);

CREATE OR REPLACE FUNCTION insert_ws_billing_audit_data()
  RETURNS trigger
  LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO eg_ws_savebilling_audit ( id, cccode, divsdiv, consumercode, billcycle, billgroup, subgroup, billtype, name, address, cesscharge, netamount, grossamount, surcharge, totalnetamount, totalsurcharge, totalgrossamount, fixchargecode, fixcharge, duedatecash, duedatecheque, status, billid, paymentid, paymentstatus,totalAmount_paid,paymentMode,isFileGenerated, createdby, createdtime,lastmodifiedby,lastmodifiedtime )
         VALUES(OLD.id,OLD.cccode,OLD.divsdiv,OLD.consumercode,OLD.billcycle,OLD.billgroup,OLD.subgroup,OLD.billtype,OLD.name,OLD.address,OLD.cesscharge,OLD.netamount,OLD.grossamount,OLD.surcharge,OLD.totalnetamount,OLD.totalsurcharge,OLD.totalgrossamount,OLD.fixchargecode,OLD.fixcharge,OLD.duedatecash,OLD.duedatecheque,OLD.status,OLD.billid,OLD.paymentid,OLD.paymentstatus,old.totalAmount_paid,old.paymentMode,old.isFileGenerated, old.createdby,old.createdtime,old.lastmodifiedby,old.lastmodifiedtime);

RETURN OLD;
END;
$$;

DROP TRIGGER IF EXISTS update_ws_bill_trigger ON eg_ws_savebilling;
CREATE TRIGGER update_ws_bill_trigger 
BEFORE UPDATE ON eg_ws_savebilling
FOR EACH ROW 
EXECUTE PROCEDURE insert_ws_billing_audit_data();
