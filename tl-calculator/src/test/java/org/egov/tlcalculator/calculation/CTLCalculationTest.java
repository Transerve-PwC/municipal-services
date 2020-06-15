package org.egov.tlcalculator.calculation;

import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.egov.TLCalculatorApp;
import org.egov.tlcalculator.TestConfiguration;
import org.egov.tlcalculator.kafka.broker.TLCalculatorProducer;
import org.egov.tlcalculator.service.CTLCalculationService;
import org.egov.tlcalculator.web.controllers.CalculatorController;
import org.egov.tlcalculator.web.models.Calculation;
import org.egov.tlcalculator.web.models.CalculationReq;
import org.egov.tlcalculator.web.models.CalculationRes;
import org.egov.tlcalculator.web.models.FeeAndBillingSlabIds;
import org.egov.tlcalculator.web.models.demand.Category;
import org.egov.tlcalculator.web.models.demand.TaxHeadEstimate;
import org.egov.tlcalculator.web.models.tradelicense.TradeLicense;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@RunWith(SpringRunner.class)
/*@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureMockMvc*/
//@WebMvcTest(CalculatorController.class)
//@WebMvcTest(value = CalculatorController.class, secure = false)
//@SpringBootTest(classes = CalculatorController.class)




@SpringBootTest(classes = TLCalculatorApp.class)
@TestPropertySource(locations = "classpath:org/egov/tlcalculator/")
@AutoConfigureMockMvc
@Import(TestConfiguration.class)


public class CTLCalculationTest {
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	
	@Autowired
    private MockMvc mockMvc;
	
	@Autowired
	private CalculatorController calculatorController;
	
	@Mock
	TLCalculatorProducer producer;
	
	
	/*@MockBean
	private CTLCalculationService ctlCalculationService;*/
	
	@Mock
	CTLCalculationService ctlCalculationService = new CTLCalculationService();
	
	@Mock
	CalculationRes calculationRes = new CalculationRes();
	
	
	

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		mockMvc = MockMvcBuilders.standaloneSetup(calculatorController).build();
		Mockito.doNothing().when(producer).push(any(String.class), any(Object.class));
	}
	
    @Test
    public void testDhobiGhatCalculation() throws Exception {
    	/*MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));*/
       
//        final List<Calculation> calculation = getCalculations();
        		List<Calculation> calculations = getCalculations();
//        		CalculationReq calculationReq =new CalculationReq();
        		Mockito.when(ctlCalculationService.calculate(Mockito.any(CalculationReq.class))).thenReturn(calculations);
//        		when(cTLCalculationService.calculate(calculationReq).thenReturn(getCalculations()));
        		System.out.println("test");
        		mockMvc.perform(post("/v1/CTL.DHOBI_GHAT/_calculate")
        				.contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(getFileContents("getDhobighatCalculationRequest.json")))
        				.andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_UTF8))
                        .andExpect(content().json(getFileContents("dhobighatCalculationResponse.json")));       
    }
    
    private List<Calculation> getCalculations() {
    	List<TaxHeadEstimate> taxHeadEstimate =new ArrayList();
    	taxHeadEstimate.add(new TaxHeadEstimate("taxHeadCode1",BigDecimal.valueOf(15),Category.FEE));
    	List<String> billingslabId = new ArrayList<>();
    	billingslabId.add("a1");
    	FeeAndBillingSlabIds feeBillingSlab=new FeeAndBillingSlabIds();
    	feeBillingSlab.setBillingSlabIds(billingslabId);
    	feeBillingSlab.setFee(BigDecimal.valueOf(15));
    	feeBillingSlab.setId("1");

        final Calculation calculation = Calculation.builder()
            .applicationNumber("CH-TL-2020-06-04-000021")
            .tradeLicense(new TradeLicense())
            .tenantId("ch")
            .taxHeadEstimates(taxHeadEstimate)
            .accessoryBillingIds(feeBillingSlab)
            .build();
        return Collections.singletonList(calculation);
//        return Arrays.asList(calculation);
    }
    
    private String getFileContents(String fileName) {
        try {
            return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(fileName), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    

}
