package org.egov.hc.repository;


import java.util.Map;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.egov.hc.contract.ServiceRequest;

import org.egov.hc.model.RequestData;
import org.egov.hc.model.ServiceRequestData;
import org.egov.hc.producer.HCConfiguration;
import org.egov.hc.producer.HCProducer;
import org.egov.hc.repository.builder.HCQueryBuilder;
import org.egov.hc.repository.rowmapper.HCRowMapper;


import org.egov.tracer.model.ServiceCallException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;


@Repository
@Slf4j
public class ServiceRepository {
		
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private HCQueryBuilder queryBuilder;

	@Autowired
	private HCRowMapper rowMapper;

	@Autowired
	private HCConfiguration hcConfiguration;
	    
     
     @Autowired
     private RestTemplate rest;
  
     @Autowired
     public ServiceRepository(JdbcTemplate jdbcTemplate, HCQueryBuilder queryBuilder, HCRowMapper rowMapper,
                         HCProducer producer, HCConfiguration hcConfiguration) {
         this.jdbcTemplate = jdbcTemplate;
         this.queryBuilder = queryBuilder;
         this.rowMapper = rowMapper;
         this.hcConfiguration = hcConfiguration;
         
     }

     
     public JSONObject getServiceRequestData(RequestData requestData) {
  		try {
  			if (!requestData.getService_request_id().isEmpty()) {

  				JSONArray actualResult = (JSONArray) jdbcTemplate.query(queryBuilder.SELECT_SERVICE_DETAIL,
  						new Object[] { requestData.getService_request_id() }, rowMapper);
  				JSONObject serviceDetailjsonObject = (JSONObject) actualResult.get(0);

  				return serviceDetailjsonObject;
  			} else {
  				return null;
  			}
  		} catch (Exception e) {
  			return null;
  		}
  	}
     
     
     public JSONArray getServiceRequest(RequestData requestData) {
   		try {
   			if (!requestData.getService_request_id().isEmpty()) {

   				JSONArray actualResult = null;
   				if (requestData.getRequestInfo().getUserInfo().getType().equals("CITIZEN")) {
   					 actualResult = (JSONArray) jdbcTemplate.query(queryBuilder.SELECT_SERVICE_DETAIL_FOR_CITIZEN,
   	   						new Object[] { requestData.getService_request_id() ,requestData.getAuditDetails().getCreatedBy()  }, rowMapper);
		
				}
   				else 
   				{
   					 actualResult = (JSONArray) jdbcTemplate.query(queryBuilder.SELECT_SERVICE_DETAIL,
   						new Object[] { requestData.getService_request_id() }, rowMapper);
   					log.info("Get service getails ");
   				}
   					JSONArray jsonArray = new JSONArray();
   					JSONObject serviceDetailjsonObject = (JSONObject) actualResult.get(0);

   				jsonArray.add(serviceDetailjsonObject);
     				return jsonArray;
   			} else {
   				return null;
   			}
   		} catch (Exception e) {
   			return null;
   		}
   	}
    
     
	
     public List<ServiceRequestData> findRequest(RequestData requestData) throws ParseException {

 		 List<Object> preparedStmtList = new ArrayList<>();
 	        String query = queryBuilder.getHCSearchQuery(requestData, preparedStmtList);
 	        log.info("Query: " + query);
 	        List<ServiceRequestData> serviceRequestList =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
 	        return serviceRequestList;
	         
	}
     
     
  
	public JSONArray getuserDetails(String service_request_id) {
		
		
  				JSONArray actualResult = (JSONArray) jdbcTemplate.query(queryBuilder.SELECT_SERVICE_DETAIL,
  						new Object[] { service_request_id }, rowMapper);
  				JSONArray jsonArray = new JSONArray();
  				JSONObject serviceDetailjsonObject = (JSONObject) actualResult.get(0);
  				jsonArray.add(serviceDetailjsonObject);  				
  				return jsonArray;
  
  			}


	public List<String> getRoleDetails(ServiceRequest request, String role) {
		String allRoles = null;
	
		List <String> uuidList = new ArrayList<>();
		try {	
			
			allRoles = rest.postForObject(hcConfiguration.getEgovHRMShost().concat(hcConfiguration.getEgovHRMSSearchEndpoint()).concat("?").concat("roles="+role+"&tenantId="+request.getServices().get(0).getCity()), request, String.class);
		
		try {
			org.json.JSONObject obj = new org.json.JSONObject(allRoles);
			org.json.JSONArray employeesList  = obj.getJSONArray("Employees");

        for (int i = 0; i < employeesList.length(); i++) {
        	
        	   String uuid = null;

	 		   org.json.JSONObject empDetails = new org.json.JSONObject(employeesList.get(i).toString());

	           org.json.JSONObject user = empDetails.getJSONObject("user");
	        	
	           uuid = user.getString("uuid");
	        	
	           uuidList .add(uuid);
        }
		}
        catch( Exception ex) {
        	
        }
	
	} catch (HttpClientErrorException e) {
		System.out.print("Handled exception");
	}
        return uuidList;
	}


	
	 public JSONArray getServiceRequestList() {
	  		try {
	  			

	  				JSONArray actualResult = (JSONArray) jdbcTemplate.query(queryBuilder.GET_CREATED_TIME,
	  						new Object[] { }, rowMapper);

	  				return actualResult;
	  			
	  			
	  		} catch (Exception e) {
	  			return null;
	  		}
	  	}
	 
	 public Object fetchResult(StringBuilder uri, Object request) {
			ObjectMapper mapper = new ObjectMapper();
	        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			Object response = null;
			try {
				response = restTemplate.postForObject(uri.toString(), request, Map.class);
			}catch(HttpClientErrorException e) {
				log.error("External Service threw an Exception: ",e);
				throw new ServiceCallException(e.getResponseBodyAsString());
			}catch(Exception e) {
				log.error("Exception while fetching from searcher: ",e);
			}
			
			return response;
			
		}


	}


