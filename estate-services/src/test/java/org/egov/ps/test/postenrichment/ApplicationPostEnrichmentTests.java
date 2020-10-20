package org.egov.ps.test.postenrichment;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.egov.ps.service.PostApprovalEnrichmentService;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ApplicationPostEnrichmentTests {

	@Autowired
	PostApprovalEnrichmentService postApprovalEnrichmentService;

	@Test
	public void testApplicationOwnershipTransfer() throws JSONException {
		String configJSON = getFileContents("applicationSampleRequestOT.json");

	}

	public static String getFileContents(String fileName) {
		try {
			return IOUtils.toString(ApplicationPostEnrichmentTests.class.getClassLoader().getResourceAsStream(fileName),
					"UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
