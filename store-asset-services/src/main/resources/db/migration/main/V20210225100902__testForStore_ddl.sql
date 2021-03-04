Create table testForStore(
	id varchar(50),
	tenantId varchar(128) NOT NULL 
);
alter table testForStore add constraint pk_testForStore primary key (id,tenantId);