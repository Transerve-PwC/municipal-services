package org.egov.ec.repository.builder;

import org.springframework.stereotype.Component;

@Component
public class EcQueryBuilder {

	public static final String GET_ITEM_MASTER = "select * from egec_item_master where tenant_id=? and is_active = 'TRUE'::boolean order by last_modified_time desc";
	public static final String GET_ITEM_MASTER_PENDING = "select * from egec_item_master where approval_status='PENDING' and tenant_id=?";
	public static final String GET_FINE_MASTER = "select * from egec_fine_master where tenant_id=? and is_active = 'TRUE'::boolean order by last_modified_time desc";
	public static final String GET_FINE_MASTER_PENDING = "select * from egec_fine_master where tenant_id=? and approval_status='PENDING'  and is_active = 'TRUE'::boolean";
	public static final String GET_VIOLATION_MASTER = "select (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end )as challan_status,*,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount, \n"
			+ "(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount \n"
			+ "  from public.egec_violation_master violation\n" + 
			"	JOIN public.egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n" + 
			"	JOIN public.egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \n" + 
			"	JOIN public.egec_payment payment on violation.violation_uuid = payment.violation_uuid\n" + 
			"	LEFT JOIN public.egec_document doc on violation.violation_uuid = doc.violation_uuid\n" + 			
			"	where violation.tenant_id=?  order by violation.last_modified_time desc";

	public static final String GET_VIOLATION_MASTER_AUTION = "select (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status,\n"
			+"*,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount,\n" + 
			"(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount\n" 
			+" from public.egec_violation_master violation \n"
			+" JOIN public.egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n"
			 +" JOIN public.egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \n"
			 +" JOIN public.egec_payment payment on violation.violation_uuid = payment.violation_uuid \n"
			 +" JOIN public.egec_store_item_register storeItem on violation.violation_uuid = storeItem.violation_uuid \n"
			 +" LEFT JOIN public.egec_document doc on violation.violation_uuid = doc.violation_uuid \n"
			 +" where violation.tenant_id=? and challan.challan_status='PENDING FOR AUCTION' order by violation.last_modified_time desc";
	
	public static final String GET_VIOLATION_MASTER_SM = "select (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status,\n"
			+ "	*,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount,\n" + 
			"	(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount\n" + 
			" from public.egec_violation_master violation \n" +
			" JOIN public.egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n" +
			" JOIN public.egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \n" + 
			" JOIN public.egec_payment payment on violation.violation_uuid = payment.violation_uuid \n" +
			" LEFT JOIN public.egec_document doc on violation.violation_uuid = doc.violation_uuid \n" +			
			" where violation.tenant_id='ch.chandigarh' and challan.challan_status not in  ('CHALLAN ISSUED', 'CLOSED') order by violation.last_modified_time desc";

	public static final String GET_FINE_PENALTY_AMOUNT = "select penalty_amount from egec_fine_master where encroachment_type = ?::varchar and number_of_violation=?::varchar  and is_active = 'TRUE'::boolean and now()::date BETWEEN effective_start_date \n" + 
			"                 AND effective_end_date::date and approval_status = 'APPROVED'" ;
	public static final String GET_VIOLATION_MASTER_SEARCH = "select (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status,\n"
			+ "			payment.last_modified_time as last_modified_time,*,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount,\n"
			+ "			(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount \n"
			+ "			 from public.egec_violation_master violation \n"			
			+ "			join egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \n"
			+ "			join egec_payment payment on violation.violation_uuid = payment.violation_uuid \n"
			+ "    		left join public.egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n"
			+ "    		left join public.egec_document doc on violation.violation_uuid = doc.violation_uuid \n"
			+ "    		where violation.contact_number ilike ? or challan.challan_id ilike ? or challan.challan_uuid ilike ? \n"
			+ "    		or violation.violator_name ilike ? or violation.license_no_cov ilike ? or violation.si_name ilike ? \n"
			+ "    		or violation.sector ilike ? or violation.encroachment_type ilike ? and violation.tenant_id=? order by violation.last_modified_time desc";


	public static final String GET_PENALTY_VIOLATIONS = "select vm.violation_uuid as violationUuid,cm.challan_Uuid as challanUuid,fm.storage_charges as storageCharges from egec_store_item_register  vs inner join egec_violation_master vm on vm.violation_uuid = vs.violation_uuid inner join egec_violation_detail vd on vm.violation_uuid = vd.violation_uuid inner join egec_fine_master fm on vd.item_type = fm.number_of_violation  inner join egec_payment ep on ep.violation_uuid = vm.violation_uuid and vd.violation_uuid = ep.violation_uuid  inner join egec_challan_master cm on cm.violation_uuid = vm.violation_uuid where vs.item_store_deposit_date < now()- interval '7 days' and vm.encroachment_type = 'Seizure of Vehicles' and ep.payment_status = 'PENDING'  and cm.challan_status !='CLOSED' and now()::date between fm.effective_start_date and fm.effective_end_date and vm.tenant_id = ?";

	public static final String GET_STORE_ITEM_REGISTER = "select * from public.egec_store_item_register item \n" + 
			//"join  public.egec_document doc on doc.violation_uuid = item.violation_uuid\n" + 
			"where item.tenant_id=?";
	public static final String GET_STORE_ITEM_REGISTER_SEARCH = "select * from public.egec_store_item_register item\n" + 
			//"join  public.egec_document doc on doc.violation_uuid = item.violation_uuid\n" + 
			" where item.challan_uuid like ? or item.item_name like ? and item.tenant_id=?";

	public static final String GET_VENDOR_DETAIL = "select *,(select case when count(*)<5 then (count(*)+1) else 5 end from egec_violation_master v where v.license_no_cov=egec_vendor_registration_master.cov_no ) as numberOfViolation from public.egec_vendor_registration_master order by last_modified_time desc ";
	public static final String GET_VENDOR_DETAIL_SEARCH = "select distinct on (cov_no) * from public.egec_vendor_registration_master \n"
			+ "where cov_no like ? or contact_number ilike ? or name ilike ?";

	public static final String GET_PAYMENT_REPORT = "select *,(select case when (select store.item_store_deposit_date from egec_store_item_register store where store.challan_uuid=challan.challan_uuid limit 1)< now()- interval '30 days' and challan.challan_status <> 'CLOSED' and master.encroachment_type <> 'Seizure of Vehicles' then 'PENDING FOR AUCTION' when (select store.item_store_deposit_date from egec_store_item_register store where store.challan_uuid=challan.challan_uuid limit 1) < now()- interval '365 days' and challan.challan_status <> 'CLOSED' and master.encroachment_type = 'Seizure of Vehicles' then 'PENDING FOR AUCTION' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) > 0) then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status from egec_violation_master master, egec_challan_master challan, egec_payment payment where master.violation_uuid=challan.violation_uuid and challan.violation_uuid=payment.violation_uuid and master.violation_date between ?  and  ? and (? = '' or payment_status= ?) and master.tenant_id=? order by master.created_time";
	public static final String GET_SEIZURE_REPORT = "select *,(select case when (select store.item_store_deposit_date from egec_store_item_register store where store.challan_uuid=challan.challan_uuid limit 1)< now()- interval '30 days' and challan.challan_status <> 'CLOSED' and master.encroachment_type <> 'Seizure of Vehicles' then 'PENDING FOR AUCTION' when (select store.item_store_deposit_date from egec_store_item_register store where store.challan_uuid=challan.challan_uuid limit 1) < now()- interval '365 days' and challan.challan_status <> 'CLOSED' and master.encroachment_type = 'Seizure of Vehicles' then 'PENDING FOR AUCTION' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) > 0) then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status \n"
			+ " from egec_violation_master master, egec_challan_master challan, egec_payment payment where master.violation_uuid=challan.violation_uuid and challan.violation_uuid=payment.violation_uuid and master.violation_date between ?  and  ? and (?  ilike '' or master.si_name ilike ?)  and(? ilike '' or master.encroachment_type ilike ?) and (? ilike '' or master.sector ilike ?) \n"
			+ " and(? ilike '' or ((select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )) ilike ?) \n"
			+ " and master.tenant_id=? order by master.created_time";
	public static final String GET_ITEM_AGING_REPORT1 = "select item_name, (quantity -auctioned_quantity)as item_quantity,challan.challan_id,item_store_deposit_date, (now()::date - item_store_deposit_date::date)::int as age, challan.challan_status from egec_store_item_register store, egec_violation_master master, egec_challan_master challan where master.violation_uuid=store.violation_uuid and challan.violation_uuid=store.violation_uuid and item_store_deposit_date < now()- interval '0 days'  and item_store_deposit_date > now()- interval '11 days'  and challan.challan_status <> 'CLOSED' and master.tenant_id=? order by master.created_time";
	public static final String GET_ITEM_AGING_REPORT2 = "select item_name, (quantity -auctioned_quantity)as item_quantity,challan.challan_id,item_store_deposit_date, (now()::date - item_store_deposit_date::date)::int as age, challan.challan_status from egec_store_item_register store, egec_violation_master master, egec_challan_master challan where master.violation_uuid=store.violation_uuid and challan.violation_uuid=store.violation_uuid and item_store_deposit_date < now()- interval '11 days'  and item_store_deposit_date > now()- interval '21 days'  and challan.challan_status <> 'CLOSED' and master.tenant_id=? order by master.created_time";
	public static final String GET_ITEM_AGING_REPORT3 = "select item_name, (quantity -auctioned_quantity)as item_quantity,challan.challan_id,item_store_deposit_date, (now()::date - item_store_deposit_date::date)::int as age, challan.challan_status from egec_store_item_register store, egec_violation_master master, egec_challan_master challan where master.violation_uuid=store.violation_uuid and challan.violation_uuid=store.violation_uuid and item_store_deposit_date < now()- interval '21 days'  and item_store_deposit_date > now()- interval '31 days'  and challan.challan_status <> 'CLOSED' and master.tenant_id=? order by master.created_time";
	public static final String GET_ITEM_AGING_REPORT4 = "select item_name, (quantity -auctioned_quantity)as item_quantity,challan.challan_id,item_store_deposit_date, (now()::date - item_store_deposit_date::date)::int as age, (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status from egec_store_item_register store, egec_violation_master master, egec_challan_master challan where master.violation_uuid=store.violation_uuid and challan.violation_uuid=store.violation_uuid and item_store_deposit_date < now()- interval '31 days'  and item_store_deposit_date > now()- interval '10000 days'  and challan.challan_status <> 'CLOSED' and master.tenant_id=? order by master.created_time";
	public static final String GET_ITEM_AGING_REPORT5 = "select item_name, (quantity -auctioned_quantity)as item_quantity,challan.challan_id,item_store_deposit_date, (now()::date - item_store_deposit_date::date)::int as age, (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=master.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status from egec_store_item_register store, egec_violation_master master, egec_challan_master challan where master.violation_uuid=store.violation_uuid and challan.violation_uuid=store.violation_uuid and challan.challan_status <> 'CLOSED' and master.tenant_id=? order by master.created_time";
	
	public static final String GET_AUCTION_MASTER = "select *,(select store.item_name from egec_store_item_register store where store.store_item_uuid=detail.store_item_uuid) from egec_auction_master auction join egec_auction_detail detail on auction.auction_uuid=detail.auction_uuid where auction.challan_uuid=? and auction.tenant_id=?";

	public static final String GET_STORE_AUCTION_ITEM = "select item_name, (quantity -auctioned_quantity)as item_quantity,challan.challan_id,item_store_deposit_date, (now()::date - item_store_deposit_date::date)::int as age from egec_store_item_register store, egec_violation_master master, egec_challan_master challan where master.violation_uuid=store.violation_uuid and challan.violation_uuid=store.violation_uuid and ((item_store_deposit_date < now()- interval '30 days' and master.encroachment_type <> 'Seizure of Vehicles') OR(item_store_deposit_date < now()- interval '365 days' and master.encroachment_type = 'Seizure of Vehicles'))  and item_store_deposit_date > now()- interval '10000 days'  and challan.challan_status <> 'CLOSED' and challan.tenant_id = ?";
	public static final String GET_STORE_ITEM_REGISTER_HOD = "select * from egec_store_item_register where challan_uuid in (\n" + 
			"	select challan_uuid from egec_store_item_register  where isverified in (false)) and tenant_id = ?";
	
	public static final String GET_VIOLATION_MASTER_HOD = "select (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status,\n"
			+ " *,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount, \n" + 
			"			(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount \n" + 
			"	from public.egec_violation_master violation \n" +
			"	 JOIN public.egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n" +
			"	 JOIN public.egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \n" + 
			"	 JOIN public.egec_payment payment on violation.violation_uuid = payment.violation_uuid \n" +
			"	 LEFT JOIN public.egec_document doc on violation.violation_uuid = doc.violation_uuid \n" +			
			"	 where challan.challan_uuid in (select challan_uuid from egec_store_item_register  where isverified in (false)) \n" +
			"	 and violation.tenant_id=? and challan.challan_status <> 'CLOSED' order by violation.last_modified_time desc";
	
	public static final String GET_VIOLATION_MASTER_AUCTION_HOD="select *,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount,  \n"
			+ "    		(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount \n"
			+ " from public.egec_violation_master violation \n"
			+ " JOIN public.egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n"
			+ "  JOIN public.egec_challan_master challan on violation.violation_uuid=challan.violation_uuid  \n"
			+ " JOIN public.egec_payment payment on violation.violation_uuid = payment.violation_uuid \n"
			+ " JOIN public.egec_auction_master auction on violation.violation_uuid = auction.violation_uuid \n"
			+ " LEFT JOIN public.egec_document doc on violation.violation_uuid = doc.violation_uuid \n"
			+ " where auction.status='PENDING' and violation.tenant_id=? order by violation.last_modified_time desc	";
	
	public static final String GET_AUCTION_CHALLAN_MASTER="\n" + 
			"		select auction.*,violation.si_name,violation.violator_name,violation.encroachment_type,violation.sector,challan.challan_id,violation.violation_date,violation.contact_number from egec_auction_master auction \n" + 
			"		JOIN egec_violation_master violation ON auction.violation_uuid=violation.violation_uuid \n" + 
			"		JOIN egec_challan_master challan ON auction.violation_uuid=challan.violation_uuid\n" + 
			"		where auction.tenant_id=? and auction.status='PENDING' order by violation.last_modified_time desc";
	public static final String GET_AUCTIONED_AVAILABLE_COUNT ="select b.challan_uuid,b.challan_id from (select ( (select count(*) from egec_store_item_register sr where sr.quantity::int != auctioned_quantity::int  and sr.challan_uuid = a.challan_uuid ) +(select count(*) from egec_auction_master am where am.status = 'PENDING' and am.challan_uuid = a.challan_uuid )) as quantityCount ,a.challan_uuid,a.challan_id from (select * from egec_challan_master where challan_status = 'PENDING FOR AUCTION' and tenant_id = ?) a group by a.challan_uuid,a.challan_uuid,a.challan_id) b where b.quantityCount = 0 ";
	public static final String GET_FINE_VALIDATION_DATE = "select count(*) as  from egec_fine_master where encroachment_type = ?::varchar and number_of_violation=?::varchar and is_active = 'TRUE'::boolean  and approval_status = 'APPROVED' and fine_Uuid != ? and ( effective_start_date BETWEEN ? AND ?::date  or  effective_end_date BETWEEN ? AND ?::date  )" ;
	public static final String GET_DASHBOARD_DETAILS_SI = "select count(distinct challan.challan_id) as challanCount from public.egec_violation_master violation, public.egec_violation_detail item, public.egec_document doc, egec_challan_master challan, egec_payment payment \n" +
			 " where violation.violation_uuid = item.violation_uuid and violation.violation_uuid = doc.violation_uuid and violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid and violation.tenant_id=?";
	public static final String GET_DASHBOARD_DETAILS_SM = "select (select count(distinct challan.challan_id) from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_payment payment \n" +
			 " where violation.violation_uuid = item.violation_uuid and violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid and violation.tenant_id=? and challan.challan_status not in ('CLOSED' ,'CHALLAN ISSUED')) as challanCount,\n" +
			 "(select count(distinct challan.challan_id)\n" + 
			 "			from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_payment payment,public.egec_store_item_register storeItem\n" + 
			 "			where violation.violation_uuid = item.violation_uuid and storeItem.violation_uuid = violation.violation_uuid and\n" + 
			 "			violation.violation_uuid=challan.violation_uuid  and violation.tenant_id=?\n" + 
			 "			and violation.violation_uuid = payment.violation_uuid\n" + 
			 "			and challan.challan_status='PENDING FOR AUCTION' as auctionCount";
	
	public static final String GET_DASHBOARD_DETAILS_HOD = "select (select count(*) from egec_fine_master where tenant_id=? and approval_status='PENDING'  and is_active = 'TRUE'::boolean) as fineCount, \n" 
			+ " 			(select count(distinct auction.auction_uuid)  \n"
			+ "    		from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_auction_master auction, egec_payment payment\n"
			+ "        	where violation.violation_uuid = item.violation_uuid and\n"
			+ "    		violation.violation_uuid=challan.violation_uuid and violation.tenant_id=? \n"
			+ "			and violation.violation_uuid = auction.violation_uuid and violation.violation_uuid = payment.violation_uuid and auction.status='PENDING') as auctionCount, \n"
			+"			(select count(distinct challan.challan_id)	\n" + 
			"			 from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_payment payment\n" + 
			"			where violation.violation_uuid = item.violation_uuid and\n" + 
			"			violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid \n" + 
			"			and challan.challan_uuid in (select challan_uuid from egec_store_item_register  where isverified in (false))\n" + 
			"			and violation.tenant_id=? and challan.challan_status <> 'CLOSED' ) as challanCount";
	
	public static final String GET_DASHBOARD_DETAILS_SI_SM="select(select count(distinct challan.challan_id) as challanCount from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_payment payment \n" +
			 " where violation.violation_uuid = item.violation_uuid and violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid and violation.tenant_id=?) as challanCount, \n" +
			 "(select count(distinct challan.challan_id)\n" + 
			 "			from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_payment payment,public.egec_store_item_register storeItem\n" + 
			 "			where violation.violation_uuid = item.violation_uuid and storeItem.violation_uuid = violation.violation_uuid and\n" + 
			 "			violation.violation_uuid=challan.violation_uuid  and violation.tenant_id=?\n" + 
			 "			and violation.violation_uuid = payment.violation_uuid\n" + 
			 "			and challan.challan_status='PENDING FOR AUCTION' as auctionCount";
	
	public static final String GET_DASHBOARD_DETAILS_SI_HOD="select(select count(distinct challan.challan_id) as challanCount from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_payment payment \n" +
			 " where violation.violation_uuid = item.violation_uuid and violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid and violation.tenant_id=?) as challanCount, \n" +
			 "(select count(*) from egec_fine_master where tenant_id=? and approval_status='PENDING'  and is_active = 'TRUE'::boolean) as fineCount, \n" 
				+ " 			(select count(distinct auction.auction_uuid)  \n"
				+ "    		from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_auction_master auction, egec_payment payment\n"
				+ "        	where violation.violation_uuid = item.violation_uuid and\n"
				+ "    		violation.violation_uuid=challan.violation_uuid and violation.tenant_id=? \n"
				+ "			and violation.violation_uuid = auction.violation_uuid and violation.violation_uuid = payment.violation_uuid and auction.status='PENDING') as auctionCount";
	
	
	public static final String GET_DASHBOARD_DETAILS_SM_HOD="select (select count(distinct challan.challan_id) from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_payment payment \n" +
			 " where violation.violation_uuid = item.violation_uuid and violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid and violation.tenant_id=? and challan.challan_status not in ('CLOSED' ,'CHALLAN ISSUED')) as challanCount,\n" +
			 "(select count(*) from egec_fine_master where tenant_id=? and approval_status='PENDING'  and is_active = 'TRUE'::boolean) as fineCount, \n" 
				+ " 			(select (select count(distinct auction.auction_uuid)  \n"
				+ "    		from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_auction_master auction, egec_payment payment\n"
				+ "        	where violation.violation_uuid = item.violation_uuid and\n"
				+ "    		violation.violation_uuid=challan.violation_uuid and violation.tenant_id=? \n"
				+ "			and violation.violation_uuid = auction.violation_uuid and violation.violation_uuid = payment.violation_uuid and auction.status='PENDING') + \n"
				+"			(select count(distinct challan.challan_id)\n" + 
				 "			from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_payment payment,public.egec_store_item_register storeItem\n" + 
				 "			where violation.violation_uuid = item.violation_uuid and storeItem.violation_uuid = violation.violation_uuid and\n" + 
				 "			violation.violation_uuid=challan.violation_uuid  and violation.tenant_id=?\n" + 
				 "			and violation.violation_uuid = payment.violation_uuid\n" + 
				 "			and challan.challan_status='PENDING FOR AUCTION' as auctionCount";
	
	public static final String GET_DASHBOARD_DETAILS_SI_SM_HOD="select (select count(distinct challan.challan_id) from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_payment payment \n" + 
			" where violation.violation_uuid = item.violation_uuid and violation.violation_uuid=challan.violation_uuid and violation.violation_uuid = payment.violation_uuid and violation.tenant_id=?) as challanCount,\n" +
			 "(select count(*) from egec_fine_master where tenant_id=? and approval_status='PENDING'  and is_active = 'TRUE'::boolean) as fineCount, \n" 
			 + " 			(select (select count(distinct auction.auction_uuid)  \n"
				+ "    		from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_auction_master auction, egec_payment payment\n"
				+ "        	where violation.violation_uuid = item.violation_uuid and\n"
				+ "    		violation.violation_uuid=challan.violation_uuid and violation.tenant_id=? \n"
				+ "			and violation.violation_uuid = auction.violation_uuid and violation.violation_uuid = payment.violation_uuid and auction.status='PENDING') + \n"
				+"			(select count(distinct challan.challan_id)\n" + 
				 "			from public.egec_violation_master violation, public.egec_violation_detail item, egec_challan_master challan, egec_payment payment,public.egec_store_item_register storeItem\n" + 
				 "			where violation.violation_uuid = item.violation_uuid and storeItem.violation_uuid = violation.violation_uuid and\n" + 
				 "			violation.violation_uuid=challan.violation_uuid  and violation.tenant_id=?\n" + 
				 "			and violation.violation_uuid = payment.violation_uuid\n" + 
				 "			and challan.challan_status='PENDING FOR AUCTION' as auctionCount";
	
	public static final String SEARCH_VIOLATION_MASTER = "select (select case when ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) and  challan.challan_status='CLOSED' then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end  )as challan_status,*,(select head_amount from egec_challan_detail ch where ch.budget_head ='FINE_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as fineAmount, \n"
			+ "(select head_amount from egec_challan_detail ch where ch.budget_head ='PENALTY_AMOUNT' and ch.challan_uuid=challan.challan_uuid) as penaltyAmount \n"
			+ "  from public.egec_violation_master violation\n" + 
			"	JOIN public.egec_violation_detail item on violation.violation_uuid = item.violation_uuid \n" + 
			"	JOIN public.egec_challan_master challan on violation.violation_uuid=challan.violation_uuid \n" + 
			"	JOIN public.egec_payment payment on violation.violation_uuid = payment.violation_uuid\n" + 
			"	LEFT JOIN public.egec_document doc on violation.violation_uuid = doc.violation_uuid\n" + 			
			"	where violation.violation_date between ?  and  ? and (?  ilike '' or violation.si_name ilike ?)  \n"
			+ " and(? ilike '' or violation.encroachment_type ilike ?) and (? ilike '' or violation.sector ilike ?) \n"
			+ " and(? ilike '' or (select case when (select store.item_store_deposit_date from egec_store_item_register store where store.challan_uuid=challan.challan_uuid limit 1)< now()- interval '30 days' and challan.challan_status <> 'CLOSED' and violation.encroachment_type <> 'Seizure of Vehicles' then 'PENDING FOR AUCTION'  when (select store.item_store_deposit_date from egec_store_item_register store where store.challan_uuid=challan.challan_uuid limit 1) < now()- interval '365 days' and challan.challan_status <> 'CLOSED' and violation.encroachment_type = 'Seizure of Vehicles' then 'PENDING FOR AUCTION' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) > 0) then 'RELEASED FROM STORE' when challan.challan_status='CLOSED' and ((select count(*) from egec_store_item_register store where store.violation_uuid=violation.violation_uuid) = 0) then 'RELEASED ON GROUND' else challan.challan_status end) ilike ?) \n"
			+ " and violation.tenant_id=? order by violation.last_modified_time desc";
	
	public static final String GET_AUCTION_UUID_CHALLAN_MASTER="\n" + 
			"		select auction.*,violation.si_name,violation.violator_name,violation.encroachment_type,violation.sector,challan.challan_id,violation.violation_date,violation.contact_number from egec_auction_master auction \n" + 
			"		JOIN egec_violation_master violation ON auction.violation_uuid=violation.violation_uuid \n" + 
			"		JOIN egec_challan_master challan ON auction.violation_uuid=challan.violation_uuid\n" + 
			"		where auction.auction_uuid=? and auction.tenant_id=? and auction.status='PENDING' order by violation.last_modified_time desc";
	
	public static final String GET_CHALLAN_PENDING_AUCTION ="select challan.challan_uuid,challan.challan_id from egec_challan_master challan JOIN egec_violation_master violation on challan.violation_uuid=violation.violation_uuid JOIN egec_payment payment ON challan.challan_uuid=payment.challan_uuid\n" + 
			"	where challan.tenant_id=? and challan.challan_uuid in \n" + 
			"	(select store.challan_uuid from egec_store_item_register store where (select store.item_store_deposit_date from egec_store_item_register store where store.challan_uuid=challan.challan_uuid limit 1)\n" + 
			"	< now()- interval '30 days' and challan.challan_status <> 'CLOSED' and violation.encroachment_type <> 'Seizure of Vehicles' OR\n" + 
			"  (select store.item_store_deposit_date from egec_store_item_register store where store.challan_uuid=challan.challan_uuid limit 1) < now()- interval '365 days' and challan.challan_status <> 'CLOSED' and violation.encroachment_type = 'Seizure of Vehicles')\n"+
			"   and ((payment.payment_status = 'PENDING' and violation.encroachment_type <> 'Unauthorized/Unregistered Vendor') OR (violation.encroachment_type = 'Unauthorized/Unregistered Vendor')) and challan.challan_status <> 'CLOSED'";
}


