package org.egov.ps.controller;

import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyPenalty;
import org.egov.ps.util.TestUtils;
import org.egov.ps.web.contracts.PropertyPenaltyRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PropertyViolationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void shouldReturnDefaultMessage() throws Exception {
		Property property = Property.builder().id("some-property-id").build();
		PropertyPenaltyRequest request = PropertyPenaltyRequest.builder().requestInfo(TestUtils.getRequestInfo())
				.propertyPenalties(Collections.singletonList(PropertyPenalty.builder().property(property)
						.branchType("estateBranch").penaltyAmount(500.0).violationType("check bounce").build()))
				.build();
		String requestAsString = new ObjectMapper().writeValueAsString(request);
		// requestAsString =
		// "{\"RequestInfo\":{\"apiId\":\"Rainmaker\",\"ver\":\".01\",\"ts\":\"\",\"action\":\"_create\",\"did\":\"1\",\"key\":\"\",\"msgId\":\"20170110110900|en_IN\",\"authToken\":\"f87a215c-c808-4aaf-839e-8c9ed867243b\"},\"PropertyPenaltys\":[{\"propertyId\":\"87c379b3-c3e5-45ce-ac7d-eb657ecd8ef8\",\"branchType\":\"estateBranch\",\"penaltyAmount\":500,\"violationType\":\"check
		// bounse\"}]}";
		// this.mockMvc
		// 		.perform(post("/violation/_penalty").contentType(TestUtils.APPLICATION_JSON_UTF8)
		// 				.content(requestAsString))
		// 		.andDo(print()).andExpect(status().isOk()).andExpect(content().string(containsString("Hello, World")));
	}

}
