CREATE TABLE public.rti_ministry_officer_mapping (
	username varchar(50) NULL,
	user_id int8 NULL,
	ministry_code varchar(5) NULL,
	effective_from date NULL,
	effective_to date NULL,
	user_tenantid varchar(50) NULL
);

ALTER TABLE public.rti_ministry_officer_mapping ADD CONSTRAINT fk_rti_ministry_officer_mapping1 FOREIGN KEY (user_id,user_tenantid) REFERENCES public.eg_user(id,tenantid);
ALTER TABLE public.rti_ministry_officer_mapping ADD CONSTRAINT fk_rti_ministry_officer_mapping2 FOREIGN KEY (ministry_code) REFERENCES public.rti_ministry_master(ministry_code);


INSERT INTO public.rti_ministry_officer_mapping (username,user_id,ministry_code,effective_from,effective_to,user_tenantid) VALUES 
('2004010002M',638,'2140','2022-06-08',NULL,NULL)
,('2004010002M',638,'1585','2022-06-08',NULL,NULL)
,('2004010002M',638,'1546','2022-06-08',NULL,NULL)
,('2003010003Q',637,'1535','2022-06-08',NULL,NULL)
,('2003010003Q',637,'1540','2022-06-08',NULL,NULL)
,('2003010003Q',637,'1565','2022-06-08',NULL,NULL)
,('2003010003Q',637,'3421','2022-06-08',NULL,NULL)
,('2003010003Q',637,'3418','2022-06-08',NULL,NULL)
;