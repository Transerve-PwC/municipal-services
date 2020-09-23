package org.egov.cpt.controller;

import org.egov.cpt.models.RentDemandCriteria;
import org.egov.cpt.service.RentDemandGenerationService;
import org.egov.cpt.web.controllers.RentDemandGenerationController;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RentDemandGenerationControllerTest {

	@InjectMocks
	RentDemandGenerationController rentDemandGenerationController;

	@Mock
	private RentDemandGenerationService demandGenerationService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createRequestTest() {
		rentDemandGenerationController.create(buildDemandCriteria());
	}

	private RentDemandCriteria buildDemandCriteria() {
		RentDemandCriteria rentDemandCriteria = new RentDemandCriteria();
		rentDemandCriteria.setDate("01/04/2020");
		return rentDemandCriteria;
	}

}
