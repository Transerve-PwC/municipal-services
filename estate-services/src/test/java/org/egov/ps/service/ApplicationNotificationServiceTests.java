package org.egov.ps.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.model.Application;
import org.egov.ps.util.TestUtils;
import org.egov.ps.web.contracts.ApplicationRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ApplicationNotificationServiceTests {

	@Autowired
	ApplicationsNotificationService applicationNotificationService;

	@Test
	public void postProcessNotifications() {
		try {
			RequestInfo requestInfo = TestUtils.getRequestInfo();
			String json_ = TestUtils.getFileContents("notification_application.json");
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			List<Application> applications = mapper.readValue(json_, new TypeReference<List<Application>>() {
			});

			ApplicationRequest request = ApplicationRequest.builder().requestInfo(requestInfo)
					.applications(applications).build();
			applicationNotificationService.processNotifications(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}