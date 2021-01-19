package org.egov.ps.test.validator;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import org.apache.commons.io.IOUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.service.MDMSService;
import org.egov.ps.validator.ApplicationValidatorService;
import org.json.JSONException;
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
public class ApplicationValidatorServiceTests {

	@Autowired
	ApplicationValidatorService validatorService;

	@Autowired
	private MDMSService mdmsService;

	private String SampleApplicationType = "SampleType";

	@Test
	public void testSimpleConfigValidation() throws JSONException {
		String configJSON = getFileContents("simpleApplicationConfig.json");
		String simpleApplicationObjectJSON = getFileContents("simpleApplicationObject.json");
		List<Map<String, Object>> result = JsonPath.read(configJSON, "$.fields");
		Configuration conf = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
		DocumentContext applicationObjectContext = JsonPath.using(conf).parse(simpleApplicationObjectJSON);

		/**
		 * if mdmsService.getApplicationConfig is called in the future, then return
		 * `result`
		 */
		Mockito.when(
				mdmsService.getApplicationConfig(eq(SampleApplicationType), any(RequestInfo.class), any(String.class)))
				.thenReturn(result);
		Map<String, List<String>> errorMap = this.validatorService.performValidationsFromMDMS(SampleApplicationType,
				applicationObjectContext, null, null, null);
		// assertTrue(errorMap.isEmpty());
	}

	public static String getFileContents(String fileName) {
		try {
			return IOUtils.toString(
					ApplicationValidatorServiceTests.class.getClassLoader().getResourceAsStream(fileName), "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
