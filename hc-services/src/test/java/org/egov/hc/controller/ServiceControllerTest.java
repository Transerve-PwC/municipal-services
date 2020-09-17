package org.egov.hc.controller;


import java.io.IOException;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.egov.common.contract.request.RequestInfo;

import org.egov.hc.contract.ServiceRequest;
import org.egov.hc.contract.ServiceResponse;
import org.egov.hc.model.RequestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.egov.hc.service.ServiceRequestService;
import org.egov.hc.utils.HCConstants;
import org.egov.hc.utils.HCUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.apache.commons.io.IOUtils;


//@RunWith(SpringRunner.class)

@RunWith(SpringJUnit4ClassRunner.class)

@WebMvcTest(ServiceController.class)

@Import(TestConfiguration.class)

@AutoConfigureMockMvc

public class ServiceControllerTest {
	

	@MockBean
	private ServiceRequestService service;
	
	@Autowired MockMvc mockMvc;
	
	 @MockBean KafkaTemplate<String, Object> producer;
	  
	 @MockBean JdbcTemplate jdbc;
	 
	 @MockBean
	private HCUtils hCUtils;
	
	@Test
	public void testGet() throws Exception {
		

		RequestInfo RequestInfo = null;
		Mockito.when(service.searchRequest(Matchers.any(RequestData.class), Matchers.any(RequestInfo.class)))
				.thenReturn(new ResponseEntity<ServiceResponse>(HttpStatus.OK));
		
		RequestData requestdata = new RequestData();
		String responseValidate = "";
		
		Gson gson = new Gson();
		String payloadData = gson.toJson(requestdata, RequestData.class);
		
		responseValidate = hCUtils.validateJsonAddUpdateData(payloadData,HCConstants.SERVICEREQUESTCREATE);

		mockMvc.perform(
				MockMvcRequestBuilders.post("/serviceRequest/_get").content(getFileContents("testResources/getServiceRequest.json"))
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());


	}
	
	
	@Test
	public void testgetDetail() throws Exception {
		

		Mockito.when(service.getServiceRequestDetails(Matchers.any(RequestData.class)))
				.thenReturn(new ResponseEntity<>(HttpStatus.OK));

		mockMvc.perform(
				MockMvcRequestBuilders.post("/serviceRequest/_getDetail").content(getFileContents("testResources/getServiceRequestDetail.json"))
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());


	}
	
	@Test
	public void testCreate() throws Exception {
		
		Mockito.when(service.create(Matchers.any(ServiceRequest.class),Matchers.anyString())).thenReturn(new ServiceResponse());
		
		mockMvc.perform(MockMvcRequestBuilders.post("/serviceRequest/_create").header("user-agent", "")
				.content(getFileContents("testResources/createRequest.json")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void testEdit() throws Exception {
		
		Mockito.when(service.updateServiceRequest(Matchers.any(ServiceRequest.class),Matchers.anyString())).thenReturn(new ServiceResponse());
		
		mockMvc.perform(MockMvcRequestBuilders.post("/serviceRequest/_create").header("user-agent", "")
				.content(getFileContents("testResources/editRequest.json")).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
	}
	
	@Test
	public void testUpdate() throws Exception {
		
		Mockito.when(service.update(Matchers.any(ServiceRequest.class), Matchers.anyString())).thenReturn(new ServiceResponse());
		

		mockMvc.perform(
				MockMvcRequestBuilders.post("/serviceRequest/_update").header("user-agent", "")
				.content(getFileContents("testResources/updateRequest.json"))
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		
	}
	
	@Test
	public void testScheduler() throws Exception {
		
		Mockito.when(service.scheduler(Matchers.any(ServiceRequest.class), Matchers.anyString())).thenReturn(new ServiceResponse());
		
		mockMvc.perform(MockMvcRequestBuilders.post("/serviceRequest/_scheduler").content("ch.chandigarh")
				.contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE));


		
	}
	

	private String getFileContents(String fileName) throws IOException {
		return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(fileName), "UTF-8");
	}
	
	
	

}


