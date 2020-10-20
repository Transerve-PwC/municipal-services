package org.egov.ps.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.ps.repository.ServiceRequestRepository;
import org.egov.ps.util.TestUtils;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.BusinessServiceResponse;
import org.egov.ps.web.contracts.WorkFlowResponseDetails;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class WorkflowCreationServiceTests {
	@Autowired
	Util util;

	@Autowired
	WorkflowCreationService workflowCreationService;

	@Autowired
	ServiceRequestRepository serviceRequestRepository;

	@SuppressWarnings("unchecked")
	@Test
	public void createTest() throws Exception {
		BusinessServiceResponse value = BusinessServiceResponse.builder()
				.responseInfo(ResponseInfo.builder().status("created").build()).businessServices(null).build();
		Mockito.when(this.serviceRequestRepository.fetchResult(Mockito.anyString(), Mockito.anyObject(),
				Mockito.any(Class.class))).thenReturn(value);
		List<WorkFlowResponseDetails> response = workflowCreationService.createWorkflows(TestUtils.getRequestInfo());
		assertNotNull("response is null", response);
		assertTrue("response is empty", !response.isEmpty());
	}
}