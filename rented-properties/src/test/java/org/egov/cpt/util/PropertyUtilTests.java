package org.egov.cpt.util;

import static org.junit.Assert.assertEquals;

import org.egov.cpt.CSPropertyApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CSPropertyApplication.class)
public class PropertyUtilTests {

    @Autowired
    PropertyUtil utils;

    @Test
    public void testValidConsumerCodes() {
        String[] transitSiteNumbers = { "3034", "1", "9999", "234" };
        for (String transitSiteNumber : transitSiteNumbers) {
            String consumerCode = utils.getPropertyRentConsumerCode(transitSiteNumber);
            assertEquals(transitSiteNumber, utils.getTransitNumberFromConsumerCode(consumerCode));
        }
    }

    @Test
    public void testValidConsumerCodeExtraction() {
        String consumerCode = "SITE-378-2020-09-248-03-09-614";
        assertEquals("378", utils.getTransitNumberFromConsumerCode(consumerCode));
    }
}