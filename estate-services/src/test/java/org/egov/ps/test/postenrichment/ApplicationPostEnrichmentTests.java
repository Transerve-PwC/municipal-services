package org.egov.ps.test.postenrichment;

import org.egov.ps.service.PostApprovalEnrichmentService;
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
}
