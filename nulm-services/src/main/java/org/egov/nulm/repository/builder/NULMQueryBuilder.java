package org.egov.nulm.repository.builder;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NULMQueryBuilder {
	public static final String GET_SEP_APPLICATION_QUERY = "SELECT  NA.application_uuid,  NA.remark, NA.application_id,  NA.nulm_application_id,  NA.application_status, \n"
			+ "        NA.name,  NA.gender,  NA.age,  NA.dob,  NA.adhar_no,  NA.mother_name,  NA.father_or_husband_name, \n"
			+ "        NA.occupation,  NA.address,  NA.contact,  NA.since_how_long_in_chandigarh,  NA.qualification, \n"
			+ "        NA.category,  NA.is_urban_poor,  NA.is_minority,  NA.is_handicapped,  NA.is_loan_from_bankinginstitute, \n"
			+ "        NA.is_repayment_made,  NA.bpl_no,  NA.minority,  NA.type_of_business_to_be_started, \n"
			+ "        NA.previous_experience,  NA.place_of_work,NA.no_of_family_members, NA.task_committee_approved_amount,NA.task_committee_remark,NA.task_committee_action_date,NA.task_committee_status,NA.committee_bank_name,NA.committee_branch_name,NA.application_forwarded_on_date\n"
			+ ",NA.sanction_date,NA.sanction_remarks,\n"
			+ "         NA.loan_amount,  NA.recommended_by, NA.bank_name,NA.branch_name,NA.account_name,NA.is_disability_certificate_available,\n"
			+ "        NA.representative_name,  NA.representative_address,  NA.tenant_id,NA.is_active,NA.created_by ,NA.created_time,NA.last_modified_by, NA.last_modified_time,\n"
			+ "        array_to_json(array_agg(json_build_object('documentType',ND.document_type,'filestoreId',ND.filestore_id,'documnetUuid',ND.document_uuid,'isActive',ND.is_active,\n"
			+ "        'tenantId',ND.tenant_id,'applicationUuid',ND.application_uuid) ))as document \n"
			+ "  FROM public.nulm_sep_application_detail NA inner  join nulm_sep_application_document ND on NA.application_uuid=ND.application_uuid and NA.tenant_id=ND.tenant_id\n"
			+ "  where NA.application_id=(case when ?  <>'' then ?  else NA.application_id end) and NA.created_by=(case when ?  <>'' then ?  else NA.created_by end) AND NA.tenant_id=? "
			+ "AND NA.application_status=(case when ?  <>'' then ?  else NA.application_status end) \n"
			+ "  AND NA.is_active='true' AND ND.is_active='true' AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(NA.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') >= CASE WHEN ?<>'' THEN DATE(?) ELSE \n"
			+ " TO_DATE(TO_CHAR(TO_TIMESTAMP(NA.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END \n"
			+ "AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(NA.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <= CASE WHEN ?<>'' THEN DATE(?) ELSE \n"
			+ " TO_DATE(TO_CHAR(TO_TIMESTAMP(NA.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END and "
			+ "UPPER(NA.name) like concat('%',case when UPPER(?)<>'' then UPPER(?) else UPPER(NA.name) end,'%') AND  NA.application_status<>?  group by NA.application_uuid    ORDER BY created_time desc";

	public static final String GET_SEP_DOCUMENT_QUERY = "SELECT count(*) \n"
			+ "        FROM public.nulm_sep_application_document \n"
			+ "        WHERE application_uuid=? and tenant_id=? and filestore_id=? and document_type=? and is_active='true'; ";

	public static final String GET_SMID_APPLICATION_QUERY = "select * FROM nulm_smid_application_detail where application_id=(case when ?  <>'' then ?  else application_id end) and created_by=(case when ?  <>'' then ?  else created_by end) \n"
			+ "AND tenant_id=? AND application_status=(case when ?  <>'' then ?  else application_status end) AND is_active='true' AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') >= CASE WHEN ?<>'' THEN DATE(?) ELSE \n"
			+ " TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END \n"
			+ "AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <= CASE WHEN ?<>'' THEN DATE(?) ELSE \n"
			+ " TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END  AND UPPER(name) like concat('%',case when UPPER(?)<>'' then UPPER(?) else UPPER(name) end,'%') AND application_status<>?  ORDER BY created_time desc";

	public static final String SHG_UUID_EXIST_QUERY = "select count(*) from nulm_smid_shg_detail where shg_uuid=? and tenant_id=? and is_active='true'";
	public static final String MEMBER_UUID_EXIST_QUERY = "select count(*) from nulm_smid_shg_member_details where application_uuid=? and tenant_id=? and is_active='true'";
	public static final String GET_MEMBER_STATUS_QUERY = "select MB.application_status from nulm_smid_shg_member_details MB  where MB.tenant_id=:tenantId and MB.is_active='true' AND MB.application_uuid=:applicationUuid";
	public static final String GET_SHG_STATUS_QUERY = "select GP.status from nulm_smid_shg_detail GP  where GP.tenant_id=:tenantId and GP.is_active='true' AND GP.shg_uuid=:shgUuid";
	public static final String GET_SHG_QUERY = "select GP.shg_uuid,GP.shg_id,GP.name,GP.type,GP.groups_nominated_by,GP.formed_through,GP.status,GP.address,GP.contact_no,GP.date_of_formation,GP.account_no,GP.date_of_opening_account,GP.bank_name,\n"
			+ "GP.branch_name,GP.main_activity,GP.remark,GP.tenant_id,GP.is_active,GP.created_by ,GP.created_time ,GP.last_modified_by,GP.last_modified_time,\n"
			+ "array_to_json(array_agg(json_build_object('applicationUuid',MB.application_uuid,'shgUuid',MB.shg_uuid,'applicationId',MB.application_id,'nulmApplicationId',MB.nulm_application_id,'applicationStatus',MB.application_status,\n"
			+ "'name',MB.name,'positionLevel',MB.position_level,'gender',MB.gender,'dob',MB.dob,'dateOfOpeningAccount',MB.date_of_opening_account,'adharNo',MB.adhar_no,'motherName',MB.mother_name,\n"
			+ "'fatherOrHusbandName',MB.father_or_husband_name,'address',MB.address,'phoneNo',MB.phone_no,'mobileNo',MB.mobile_no,'qualification',MB.qualification,'emailId',MB.email_id,\n"
			+ "'isUrbanPoor',MB.is_urban_poor,'isMinority',MB.is_minority,'isPwd',MB.is_pwd,'isStreetVendor',MB.is_street_vendor,'isHomeless',MB.is_homeless,'isInsurance',MB.is_insurance,\n"
			+ "'bplNo',MB.bpl_no,'minority',MB.minority,'caste',MB.caste,'wardNo',MB.ward_no,'nameAsPerAdhar',MB.name_as_per_adhar,'adharAcknowledgementNo',\n"
			+ "MB.adhar_acknowledgement_no,'insuranceThrough',MB.insurance_through,'documentAttachemnt',MB.document_attachemnt,'accountNo',MB.account_no,\n"
			+ "'bankName',MB.bank_name,'branchName',MB.branch_name,'remark',MB.remark,'tenantId',MB.tenant_id,'isActive',MB.is_active,'createdBy',MB.created_by,'createdTime',MB.created_time\n"
			+ ",'lastModifiedBy',MB.last_modified_by,'lastModifiedTime',MB.last_modified_time) ))as member  from nulm_smid_shg_detail GP LEFT JOIN  nulm_smid_shg_member_details MB on GP.shg_uuid=MB.shg_uuid AND GP.tenant_id=MB.tenant_id  \n"
			+ "  where GP.created_by=(case when :createdBy <>'' then :createdBy else GP.created_by end) and GP.tenant_id=:tenantId and GP.is_active='true'AND\n"
			+ "GP.status IN (:status) AND (MB.application_status IN(:applicationStatus) OR  MB.application_status is null )AND GP.shg_id=(case when :shgId <>'' then :shgId else GP.shg_id end)  AND TO_DATE(TO_CHAR(TO_TIMESTAMP(GP.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') >= CASE WHEN :fromDate<>'' THEN DATE(:fromDate) ELSE\n"
			+ "		TO_DATE(TO_CHAR(TO_TIMESTAMP(GP.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(GP.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <= CASE WHEN :toDate<>'' THEN DATE(:toDate) ELSE \n"
			+ "		TO_DATE(TO_CHAR(TO_TIMESTAMP(GP.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END  AND UPPER(GP.name) like concat('%',case when UPPER(:name)<>'' then UPPER(:name) else UPPER(GP.name) end,'%') \n"
			+ " GROUP BY GP.shg_uuid ORDER BY created_time desc";

	public static final String GET_SHG_MEMBER_QUERY = "SELECT MB.application_uuid, MB.shg_uuid, MB.application_id, MB.nulm_application_id,MB.application_status, MB.name, MB.position_level, MB.gender, MB.dob,\n"
			+ "MB.date_of_opening_account ,MB.adhar_no, MB.mother_name, MB.father_or_husband_name, MB.address, MB.phone_no,MB.mobile_no, MB.qualification, MB.email_id, MB.is_urban_poor, MB.is_minority,\n"
			+ "MB.is_pwd, MB.is_street_vendor, MB.is_homeless, MB.is_insurance, MB.bpl_no,MB.minority, MB.caste, MB.ward_no, MB.name_as_per_adhar, MB.adhar_acknowledgement_no,\n"
			+ "MB.insurance_through, document_attachemnt, MB.account_no, MB.bank_name,MB.branch_name, MB.remark, MB.tenant_id, MB.is_active, MB.created_by, MB.created_time,\n"
			+ "MB.last_modified_by, MB.last_modified_time ,\n"
			+ "array_to_json(array_agg(json_build_object('shgUuid',GP.shg_uuid,'shgId',GP.shg_id,'name',GP.name,'type',GP.type,'groupsNominatedBy',GP.groups_nominated_by,\n"
			+ "'formedThrough',GP.formed_through,'status',GP.status,'address',GP.address,'contactNo',GP.contact_no,'dateOfFormation',\n"
			+ "GP.date_of_formation,'accountNo',GP.account_no,'dateOfOpeningAccount',GP.date_of_opening_account,'bankName',GP.bank_name,'branchName',GP.branch_name,'mainActivity',GP.main_activity,'remark',GP.remark) ))as group \n"
			+ "FROM public.nulm_smid_shg_member_details MB Inner  join nulm_smid_shg_detail GP on GP.shg_uuid=MB.shg_uuid AND GP.tenant_id=MB.tenant_id\n"
			+ "WHERE application_id=(case when ?  <>'' then ?  else application_id end) and MB.created_by=(case when ?  <>'' then ?  else MB.created_by end)  \n"
			+ " AND MB.tenant_id=? AND MB.application_status=(case when ?  <>'' then ?  else MB.application_status end) AND MB.is_active='true'\n"
			+ "   AND TO_DATE(TO_CHAR(TO_TIMESTAMP(MB.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') >= CASE WHEN ?<>'' THEN DATE(?) ELSE\n"
			+ "		TO_DATE(TO_CHAR(TO_TIMESTAMP(MB.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(MB.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <= CASE WHEN ?<>'' THEN DATE(?) ELSE \n"
			+ "		TO_DATE(TO_CHAR(TO_TIMESTAMP(MB.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END AND UPPER(GP.name) like concat('%',case when UPPER(?)<>'' then UPPER(?) else UPPER(GP.name) end,'%') and UPPER(MB.name) like concat('%',case when UPPER(?)<>'' then UPPER(?) else UPPER(MB.name) end,'%') AND GP.shg_id=(case when ?  <>'' then ?  else GP.shg_id end) GROUP BY MB.application_uuid ORDER BY created_time desc";

	public static final String GET_SHG_MEMBER_COUNT_QUERY = "select count(*) from nulm_smid_shg_member_details where shg_uuid=? and tenant_id=? and is_active='true' and position_level='MEMBER'";
	public static final String GET_ORGANIZATION_MOBILE_NO_QUERY = "select count(*) from nulm_organization where tenant_id=? and is_active='true'and mobile_no=? ";
	public static final String GET_ORGANIZATION_NAME_QUERY = "select count(*) from nulm_organization where tenant_id=? and is_active='true'and organization_name=? ";
	public static final String GET_ORGANIZATION_QUERY = "SELECT organization_uuid, user_id, organization_name, address, email_id,representative_name, mobile_no, registration_no, tenant_id, is_active,created_by, created_time, last_modified_by, last_modified_time\n"
			+ " FROM public.nulm_organization where tenant_id=? and is_active='true' and organization_uuid=(case when ?  <>'' then ?  else organization_uuid end)\n"
			+ " and  UPPER(organization_name) like concat('%',case when UPPER(?)<>'' then UPPER(?) else UPPER(organization_name) end,'%') and  UPPER(registration_no) like concat('%',case when UPPER(?)<>'' then UPPER(?) else UPPER(registration_no) end,'%') \n"
			+ " AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') >= CASE WHEN ?<>'' THEN DATE(?) ELSE TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END\n"
			+ " AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <= CASE WHEN ?<>'' THEN DATE(?) ELSE TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END\n"
			+ "ORDER BY created_time desc";
	public static final String GET_ORGANIZATION_UUID_QUERY = "select count(*) from nulm_organization where tenant_id=? and is_active='true'and organization_uuid=? ";

	public static final String GET_SUH_NAME_QUERY = "select count(*) from public.nulm_suh_application_detail where name_of_shelter=? and tenant_id=? and is_active='true'";

	public static final String GET_SUH_QUERY = "SELECT NA.*,FM.facilities,RM.record,SM.staff FROM public.nulm_suh_application_detail NA \n"
			+ "left join (SELECT count(suh_uuid) facility, suh_uuid,max(tenant_id) tenant_id,array_to_json( array_agg(json_build_object('suhUuid',suh_uuid,'facilityUuid',facility_uuid,'isBedding',is_bedding,'beddingRemark',bedding_remark,\n"
			+ "'isWashingOfLinen',is_washing_of_linen,'washingOfLinenRemark',washing_of_linen_remark,'isCleaningOfPremises',is_cleaning_of_premises,'cleaningOfPremiseRemark',cleaning_of_premise_remark,\n"
			+ "'isRecreationFacilities',is_recreation_facilities,'recreationFacilitiesRemark',recreation_facilities_remark,'isDrinkingWater',is_drinking_water,\n"
			+ "'drinkingWaterRemark',drinking_water_remark,'isMeals',is_meals,'mealsRemark',meals_remark,'isLockerForInmates',is_locker_for_inmates,'lockerForInmatesRemark',locker_for_inmates_remark,\n"
			+ "'isFireSafetyMeasure',is_fire_safety_measure,'fireSafetyMeasureRemark',fire_safety_measure_remark,'isOfficeSetUp',is_office_set_up,'officeSetUpRemark',office_set_up_remark,\n"
			+ "'isFirstAidKitAndTrainingToStaff',is_first_aid_kit_and_training_to_staff,'firstAidKitAndTrainingToStaffRemark',first_aid_kit_and_training_to_staff_remark,\n"
			+ "'isDisplayOfEmergencyNumbers',is_display_of_emergency_numbers,'displayOfEmergencyNumbers_remark',display_of_emergency_numbers_remark,'isToilet',is_toilet,\n"
			+ "'toiletRemark',toilet_remark,'facilityPicture',facility_picture,'tenant_id',tenant_id,'is_active',is_active,'created_by',created_by,'created_time',created_time\n"
			+ ",'last_modified_by',last_modified_by,'last_modified_time',last_modified_time) ))as facilities from nulm_suh_facilities_maintenance \n"
			+ "GROUP BY suh_uuid )FM ON NA.suh_uuid=FM.suh_uuid and NA.tenant_id=FM.tenant_id\n"
			+ "LEFT JOIN (SELECT count(suh_uuid) facility, suh_uuid,max(tenant_id) tenant_id,\n"
			+ "array_to_json(array_agg(json_build_object('suhUuid',suh_uuid,'recordUuid',record_uuid,'isAssetInventoryRegister',is_asset_inventory_register,'assetInventoryRegisterRemark',asset_inventory_register_remark,\n"
			+ "'isAcountRegister',is_acount_register,'acountRegisterRemark',acount_register_remark,'isAttendanceRegisterOfStaff',is_attendance_register_of_staff,'attendanceRegisterOfStaffRemark',attendance_register_of_staff_remark,\n"
			+ "'isShelterManagementCommitteeRegister',is_shelter_management_committee_register,'shelterManagementCommitteeRegisterRemark',shelter_management_committee_register_remark,\n"
			+ "'isPersonnelAndSalaryRegister',is_personnel_and_salary_register,'personnelAndSalaryRegisterRemark',personnel_and_salary_register_remark,\n"
			+ "'isHousekeepingAndMaintenanceRegister',is_housekeeping_and_maintenance_register,'housekeepingAndMaintenanceRegisterRemark',housekeeping_and_maintenance_register_remark,\n"
			+ "'isComplaintAndSuggestionRegister',is_complaint_and_suggestion_register,'complaintAndSuggestionRegisterRemark',complaint_and_suggestion_register_remark,\n"
			+ "'isVisitorRegister',is_visitor_register,'visitorRegisterRemark',visitor_register_remark,'isProfileRegister',is_profile_register,'profileRegisterRemark',profile_register_remark,\n"
			+ "'tenant_id',tenant_id,'is_active',is_active,'created_by',created_by,'created_time',created_time\n"
			+ ",'last_modified_by',last_modified_by,'last_modified_time',last_modified_time) ))as record FROM nulm_suh_record_maintenance GROUP BY suh_uuid )RM \n"
			+ "ON NA.suh_uuid=RM.suh_uuid and NA.tenant_id=RM.tenant_id \n"
			+ "LEFT JOIN (SELECT count(suh_uuid) facility, suh_uuid,max(tenant_id) tenant_id,array_to_json(array_agg(json_build_object('suhUuid',suh_uuid,'staffUuid',staff_uuid,'isManager',is_manager,'managerRemark',manager_remark,\n"
			+ "'isSecurityStaff',is_security_staff,'securityStaffRemark',security_staff_remark,'isCleaner',is_cleaner,'cleanerRemark',cleaner_remark,'tenant_id',tenant_id,'is_active',is_active,'created_by',created_by,'created_time',created_time\n"
			+ ",'last_modified_by',last_modified_by,'last_modified_time',last_modified_time) ))as staff FROM nulm_suh_staff_maintenance GROUP BY suh_uuid ) SM\n"
			+ "ON NA.suh_uuid=SM.suh_uuid and NA.tenant_id=SM.tenant_id \n"
			+ "where NA.suh_id=(case when :suhId  <>'' then :suhId   else NA.suh_id end) and NA.created_by=(case when :createdBy  <>'' then :createdBy  else NA.created_by end) AND NA.tenant_id=:tenantId  AND NA.application_status IN (:status) \n"
			+ "AND NA.is_active='true'  AND TO_DATE(TO_CHAR(TO_TIMESTAMP(NA.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') >= CASE WHEN :fromDate<>'' THEN DATE(:fromDate) ELSE\n"
			+ "TO_DATE(TO_CHAR(TO_TIMESTAMP(NA.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(NA.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <= CASE WHEN :toDate<>'' THEN DATE(:toDate) ELSE \n"
			+ " TO_DATE(TO_CHAR(TO_TIMESTAMP(NA.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END AND UPPER(na.name_of_shelter) like concat('%',case when UPPER(:nameOfShelter)<>'' then UPPER(:nameOfShelter) else UPPER(na.name_of_shelter) end,'%') ORDER BY created_time desc";

	public static final String GET_SUH_LOG_QUERY = "SELECT log_uuid, name_of_shelter, date, name, qualification, gender, age, address, adhar_no, reason_for_staying, tenant_id, is_active, created_by, created_time, last_modified_by, last_modified_time\n"
			+ "  FROM public.nulm_suh_occupancy_log where log_uuid=(case when ?  <>'' then ?   else log_uuid end) and created_by=(case when ?  <>'' then ?  else created_by end)  "
			+ "AND UPPER(name) like concat('%',case when UPPER(?)<>'' then UPPER(?) else UPPER(name) end,'%') "
			+ "AND UPPER(name_of_shelter) like concat('%',case when UPPER(?)<>'' then UPPER(?) else UPPER(name_of_shelter) end,'%') AND TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') >= CASE WHEN ?<>'' THEN DATE(?) ELSE \n"
			+ "TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <= CASE WHEN ?<>'' THEN DATE(?) ELSE \n"
			+ "TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END and tenant_id=? and is_active='true'";

	public static final String GET_SUH_LOG_DATE_QUERY = "SELECT log_uuid,created_time FROM public.nulm_suh_occupancy_log where log_uuid=? and tenant_id=? and is_active='true';";
	public static final String GET_SUH_SHELTER_NAME_QUERY = "SELECT NA.name_of_shelter,NA.suh_id,NA.tenant_id,NA.suh_uuid,NA.assigned_to FROM public.nulm_suh_application_detail NA \n"
			+ "inner join nulm_organization OG on  OG.tenant_Id=NA.tenant_id where NA.tenant_id=:tenantId AND NA.application_status IN (:status) \n"
			+ "and assigned_to in (select organization_uuid from nulm_organization where user_id=(:userId) )\n"
			+ "	 		AND NA.is_active='true'  GROUP BY NA.suh_uuid";

	public static final String GET_SUSV_DOCUMENT_QUERY = "SELECT count(*)  FROM public.nulm_susv_application_document  WHERE application_uuid=? and tenant_id=? and filestore_id=? and document_type=? and is_active='true';";

	public static final String GET_SUSV_QUERY = "SELECT SA.application_uuid, SA.application_id, SA.nulm_application_id, SA.application_status,SA.name_of_applicant, SA.gender, SA.age, SA.adhar_no, SA.mother_name, SA.father_or_husband_name,\n"
			+ "SA.permanent_address, SA.present_address, SA.mobile_no, SA.is_disability,SA.category, SA.qualification, SA.blood_group, SA.category_of_vending, SA.proposed_location_of_vending,\n"
			+ "SA.proposed_time_of_vending, SA.goverment_scheme, SA.name_of_nominee,SA.tenant_id, SA.remark,SA.is_undertaking,SA.date,SA.place,  SA.is_active, SA.created_by,\n"
			+ "SA.created_time, SA.last_modified_by, SA.last_modified_time,ND.document,NF.familymembers\n"
			+ "FROM public.nulm_susv_application_detail SA  left join (SELECT count(application_uuid) docs, application_uuid,max(tenant_id) tenant_id, array_to_json(array_agg(json_build_object('documentType',document_type,'filestoreId',filestore_id,'documnetUuid',document_uuid,'isActive',is_active, \n"
			+ "'tenantId',tenant_id,'applicationUuid',application_uuid) ))as document FROM nulm_susv_application_document GROUP BY application_uuid) ND on SA.application_uuid=ND.application_uuid and SA.tenant_id=ND.tenant_id \n"
			+ "left join (SELECT count(application_uuid) fmember, application_uuid,max(tenant_id) tenant_id, array_to_json(array_agg(json_build_object('uuid',uuid,'name',name,'age',age,'relation',relation,'isActive',is_active, \n"
			+ "'tenantId',tenant_id,'applicationUuid',application_uuid))) as familymembers FROM nulm_susv_familiy_detail GROUP BY application_uuid) NF \n"
			+ "on SA.application_uuid=NF.application_uuid and SA.tenant_id=NF.tenant_id \n"
			+ "where SA.application_id=(case when :applicationId  <>'' then :applicationId  else SA.application_id end) and SA.created_by=(case when :createdBy  <>'' then :createdBy  else SA.created_by end) AND SA.tenant_id=:tenantId\n"
			+ "AND SA.is_active='true'  AND SA.application_status NOT IN(:applicationStaus) and \n"
			+ "TO_DATE(TO_CHAR(TO_TIMESTAMP(SA.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') >= CASE WHEN :fromDate <> ''THEN DATE(:fromDate) ELSE\n"
			+ "TO_DATE(TO_CHAR(TO_TIMESTAMP(SA.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END\n"
			+ "AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(SA.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <= CASE WHEN :toDate <>'' THEN DATE(:toDate) ELSE\n"
			+ "TO_DATE(TO_CHAR(TO_TIMESTAMP(SA.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END  AND UPPER(SA.name_of_applicant) like concat('%',case when UPPER(:nameOfApplicant)<>'' then UPPER(:nameOfApplicant) else UPPER(SA.name_of_applicant) end,'%')  ORDER BY created_time desc";

	public static final String GET_SHG_MEMBER_STATUS_QUERY = "SELECT application_uuid, shg_uuid, application_id, nulm_application_id,application_status, name, position_level, gender, dob, date_of_opening_account, \n"
			+ "       adhar_no, mother_name, father_or_husband_name, address, phone_no,mobile_no, qualification, email_id, is_urban_poor, is_minority, \n"
			+ "       is_pwd, is_street_vendor, is_homeless, is_insurance, bpl_no,minority, caste, ward_no, name_as_per_adhar, adhar_acknowledgement_no, \n"
			+ "       insurance_through, document_attachemnt, account_no, bank_name,branch_name, remark, tenant_id, is_active, created_by, created_time, \n"
			+ "       last_modified_by, last_modified_time\n"
			+ "  FROM public.nulm_smid_shg_member_details where shg_uuid=:shgUuid and tenant_id=:tenantId";

	public static final String GET_MEMBER_COUNT_QUERY = "SELECT COUNT(*) AS memberCount FROM nulm_smid_shg_member_details NM INNER JOIN nulm_smid_shg_detail ND on NM.shg_uuid=ND.shg_uuid AND NM.tenant_id=ND.tenant_id WHERE NM.shg_uuid=:shgUuid \n"
			+ " AND NM.tenant_id=:tenantId and CASE WHEN ND.status<> 'REJECTED' THEN application_status IN('APPROVED','UPDATED','CREATED') ELSE application_status IN('APPROVED','UPDATED','CREATED','REJECTED') END";

	public static final String GET_SUSV_TRANSACTOIN_QUERY = "SELECT  TD.uuid,  TD.transaction_type,  TD.amount,  TD.mode_of_payment,  TD.payment_details, TD.donation_received_from,  TD.donor_details,  TD.expenditure_type,\n"
			+ "TD.expenditure_details,  TD.email_id,  TD.comments,  TD.tenant_id,  TD.remark,  TD.is_active,  TD.created_by, TD.created_time,  TD.last_modified_by,  TD.last_modified_time\n"
			+ ",array_to_json(array_agg(json_build_object('documentType',ND.document_type,'filestoreId',ND.filestore_id,'documnetUuid',ND.document_uuid,'isActive',ND.is_active,\n"
			+ "'tenantId',ND.tenant_id,'uuid',ND.uuid) ))as document\n"
			+ "FROM public.nulm_susv_transaction_detail TD inner  join nulm_susv_transaction_document ND on TD.uuid=ND.uuid and TD.tenant_id=ND.tenant_id\n"
			+ "where TD.created_by=(case when :createdBy  <>'' then  :createdBy  else TD.created_by end) AND TD.tenant_id=:tenantId and TD.transaction_type=(case when :transactionType  <>'' then  :transactionType  else TD.transaction_type end)\n"
			+ "and TD.uuid=(case when :uuid  <>'' then  :uuid  else TD.uuid end) AND TD.is_active='true' AND ND.is_active='true' AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(TD.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') >= CASE WHEN :fromDate<>'' THEN DATE(:fromDate) ELSE\n"
			+ "TO_DATE(TO_CHAR(TO_TIMESTAMP(TD.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END\n"
			+ "AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(TD.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <= CASE WHEN :toDate<>'' THEN DATE(:toDate) ELSE\n"
			+ "TO_DATE(TO_CHAR(TO_TIMESTAMP(TD.created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END group by TD.uuid    ORDER BY created_time desc";

	public static final String GET_SUH_CITIZEN_QUERY = " select * FROM nulm_suh_citizenngo_application "
			+ "where suh_citizen_ngo_uuid=(case when :suhCitizenNGOId  <>'' then :suhCitizenNGOId   else suh_citizen_ngo_uuid end)"
			+ "AND is_active='true'  AND TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') >= CASE WHEN :fromDate<>'' THEN DATE(:fromDate) ELSE\n"
			+ "TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END AND  TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <= CASE WHEN :toDate<>'' THEN DATE(:toDate) ELSE \n"
			+ " TO_DATE(TO_CHAR(TO_TIMESTAMP(created_time / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') END ";

}
