package org.egov.hc.repository.builder;


import java.util.List;

import org.egov.hc.model.RequestData;
import org.egov.hc.producer.HCConfiguration;
import org.egov.hc.utils.HCConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class HCQueryBuilder {
	
	@Autowired
	private HCConfiguration hcConfiguration;

	@Autowired
	public HCQueryBuilder(HCConfiguration config) {
		
	}

	private static final String QUERY = "select service_request_id,service_type,owner_name,service_request_status,tenant_id,current_assignee,to_char(to_timestamp(cast(createdtime/1000 as bigint))::date ,'DD/MM/YYYY')as createdtime,lastmodifiedtime,servicerequestsubtype from eg_hc_service_request hc ";

	public static final String SELECT_SERVICE_DETAIL_FOR_CITIZEN = "SELECT service_request_uuid, owner_name, tenant_id, location, latitude, longitude, locality, street_name, landmark, contact_number, email_id, tree_count, service_request_document, service_request_status, service_request_id, service_type, description,current_assignee, createdby, to_char(to_timestamp(cast(createdtime/1000 as bigint))::date,'DD/MM/YYYY') as createdtimes,servicerequest_lang ,lastmodifiedby,to_char(to_timestamp(cast(lastmodifiedtime/1000 as bigint))::date,'DD/MM/YYYY') as lastmodifiedtime,servicerequestsubtype from eg_hc_service_request WHERE service_request_id =? and createdby = ?";
	
	public static final String SELECT_SERVICE_DETAIL = "SELECT service_request_uuid, owner_name, tenant_id, location, latitude, longitude, locality, street_name, landmark, contact_number, email_id, tree_count, service_request_document, service_request_status, service_request_id, service_type, description,current_assignee, createdby, to_char(to_timestamp(cast(createdtime/1000 as bigint))::date,'DD/MM/YYYY') as createdtimes,servicerequest_lang ,lastmodifiedby,to_char(to_timestamp(cast(lastmodifiedtime/1000 as bigint))::date,'DD/MM/YYYY') as lastmodifiedtime,servicerequestsubtype from eg_hc_service_request WHERE service_request_id =?";
	
	public static final String GET_CREATED_TIME = "SELECT service_type,createdtime,service_request_id,current_assignee,to_char(to_timestamp(cast(createdtime/1000 as bigint))::date ,'DD-MM-YYYY')as serviceRequestDate \r\n" + 
			"from eg_hc_service_request WHERE \r\n" + 
			" service_request_status != '"+HCConstants.REJECTED_STATUS+"' AND\r\n" + 
			" service_request_status != '"+HCConstants.COMPLETED_STATUS+"' AND \r\n" +
			"current_assignee != ''";
		
	
	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY lastmodifiedtime DESC) offset_ FROM " + "({})"
			+ " result) result_offset "
			+ "WHERE offset_ > ? AND offset_ <= ?";

	public String getHCSearchQuery(RequestData criteria, List<Object> preparedStmtList) {


		StringBuilder builder = new StringBuilder(QUERY);

				// owner name
				if (criteria.getOwnerName() != null) {
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.owner_name like ? ");
		
					preparedStmtList.add("%"+criteria.getOwnerName().trim()+"%");
				}
		
				// address
				if (criteria.getAddress() != null) {
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.location like ? ");
		
					preparedStmtList.add("%"+criteria.getAddress().trim()+"%");
				}
		
				// latitude
				if (criteria.getLatitude() != null) {
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.latitude  =? ");
		
					preparedStmtList.add(criteria.getLatitude().trim());
				}
		
				// longitude
				if (criteria.getLongitude() != null) {
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.longitude = ?  ");
		
					preparedStmtList.add(criteria.getLongitude().trim());
				}
		
				// locality
				if (criteria.getHouseNoAndStreetName() != null) {
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.locality ilike ? ");
		
					preparedStmtList.add("%"+criteria.getHouseNoAndStreetName().trim()+"%");
				}
		
				// street name
				if (criteria.getStreetName() != null) {
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.street_name ilike ?");
					preparedStmtList.add("%"+criteria.getStreetName().trim()+"%");
				}
		
				// landmark
				if (criteria.getLandmark() != null) {
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.landmark ilike ?");
					preparedStmtList.add("%"+criteria.getLandmark().trim()+"%");
				}
				// contact_number
				if (criteria.getOwnerContactNumber() != null) {
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.contact_number ilike ?");
					preparedStmtList.add("%"+criteria.getOwnerContactNumber().trim()+"%");
				}
		
				// email_id
				if (criteria.getEmail() != null) {
		
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.email_id ilike ?");
					preparedStmtList.add("%"+criteria.getEmail().trim()+"%");
				}
		
				// tree_count
				if (criteria.getTreeCount() != null) {
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.tree_count = ?");
					preparedStmtList.add(criteria.getTreeCount());
				}
		
				// service type
				if (criteria.getServiceType() != null) {
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.service_type ilike ?");		
					preparedStmtList.add("%"+criteria.getServiceType().trim().toUpperCase()+"%");
				}
		
				// service request id
				if (criteria.getService_request_id() != null) {
		
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.service_request_id ilike ?");
					preparedStmtList.add("%"+criteria.getService_request_id().trim()+"%");
				}
		
				// service request status
				if (criteria.getServiceRequestStatus() != null) {
		
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.service_request_status   = ?");
					preparedStmtList.add(criteria.getServiceRequestStatus().trim().toUpperCase());
				}
		
				// from date
		
				if (criteria.getFromDate() != null) {
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.createdtime >= ? ");
					preparedStmtList.add(criteria.getFromDate());
				}
		
				// to date
				if (criteria.getToDate() != null) {
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.createdtime <= ? ");
					preparedStmtList.add(criteria.getToDate());
		
				}
				// service request sub type
				if (criteria.getServiceRequestSubtype() != null) {
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.servicerequestsubtype->>'subservicetype' = ?");
		
					preparedStmtList.add(criteria.getServiceRequestSubtype().trim());
				}
		
				// this is for citizen
				if (criteria.getRequestInfo().getUserInfo().getType().equals("CITIZEN")) {
					
		
					addClauseIfRequired(preparedStmtList, builder);
					builder.append(" hc.createdby  = ? ");
					preparedStmtList.add(criteria.getAuditDetails().getCreatedBy());
		
				}

		// builder.append(" ORDER BY service_request_id DESC");

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}

	private String addPaginationWrapper(String query, List<Object> preparedStmtList, RequestData criteria) {
		
		 int limit = hcConfiguration.getDefaultLimit();
	        int offset = hcConfiguration.getDefaultOffset();
	        String finalQuery = paginationWrapper.replace("{}",query);

	        if(criteria.getLimit()!=null && criteria.getLimit()<=hcConfiguration.getMaxSearchLimit())
	            limit = criteria.getLimit();

	        if(criteria.getLimit()!=null && criteria.getLimit()>hcConfiguration.getMaxSearchLimit())
	            limit = hcConfiguration.getMaxSearchLimit();

	        if(criteria.getOffset()!=null)
	            offset = criteria.getOffset();

	        preparedStmtList.add(offset);
	        preparedStmtList.add(limit+offset);

	       return finalQuery;
	}

	private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND");
		}
	}
}
