package org.egov.ps.test.validator;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.model.Application;
import org.egov.ps.model.Property;
import org.egov.ps.service.ApplicationEnrichmentService;
import org.egov.ps.service.MDMSService;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jayway.jsonpath.JsonPath;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ApplicationFeesServiceTest {
	
	@Autowired
	ApplicationEnrichmentService applicationEnrichmentService;

	@Autowired
	private MDMSService mdmsService;
	
	
	
	private String SampleApplicationType = "SampleType";
	
	@Test
	public void testSimpleApplicationFeesValidation() throws JSONException {
		String configJSON = getFileContents("simpleApplicationFees.json");
		List<Map<String, Object>> results = JsonPath.read(configJSON, "$.fees");
		
		when(mdmsService.getApplicationFees(eq(SampleApplicationType), any(RequestInfo.class), any(String.class))).thenReturn(results);
		
		/* Here Only Category match with results data */
		Application categoryMatchApplication = Application.builder()
								.property(Property.builder().category("CAT.RESIDENTIAL").subCategory("SUBCAT.SUBCAT1").build()).build();
		
		/* Here Both Category and sub-category match with results data */
		Application categorySubCateMatchApplication = Application.builder()
				.property(Property.builder().category("CAT.COMMERCIAL").subCategory("SUBCAT.BOOTH").build()).build();
		
		/* None Category and sub-category match with results data*/
		Application noneMatchApplcaition = Application.builder()
				.property(Property.builder().category("CAT.CAT1").subCategory("SUBCAT.SUBCAT1").build()).build();
		
		assertTrue(new BigDecimal(6000.0).compareTo(applicationEnrichmentService.fetchEstimateAmountFromMDMSJson(results,categoryMatchApplication)) == 0);
		assertTrue(new BigDecimal(5000.0).compareTo(applicationEnrichmentService.fetchEstimateAmountFromMDMSJson(results,categorySubCateMatchApplication))== 0);
		assertTrue(new BigDecimal(100.0).compareTo(applicationEnrichmentService.fetchEstimateAmountFromMDMSJson(results,noneMatchApplcaition))== 0);
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
