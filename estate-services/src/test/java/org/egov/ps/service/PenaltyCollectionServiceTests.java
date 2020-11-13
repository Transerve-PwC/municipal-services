package org.egov.ps.service;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.egov.ps.model.PropertyPenalty;
import org.egov.ps.service.calculation.PenaltyCollectionService;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PenaltyCollectionServiceTests {

    private static final String JAN_1_2020 = "01 01 2020";
    private static final String JAN_15_2020 = "15 01 2020";
    private static final String FEB_1_2020 = "01 02 2020";
    // private static final String FEB_15_2020 = "15 02 2020";

    PenaltyCollectionService PenaltyCollectionService;

    @Before
    public void setup() {
        this.PenaltyCollectionService = new PenaltyCollectionService();
    }

    @Test
    public void testPenaltySettlement() throws ParseException {
        List<PropertyPenalty> penalties = Arrays.asList(getPenalty(100, 0, JAN_1_2020),
                getPenalty(500, 300, JAN_15_2020), getPenalty(200, 200, FEB_1_2020));
        this.PenaltyCollectionService.settle(penalties, 400);
        assertEquals(100D, getTotalDue(penalties), 0.1);
    }

    private double getTotalDue(List<PropertyPenalty> penalties) {
        return penalties.stream().mapToDouble(PropertyPenalty::getRemainingPenaltyDue).sum();
    }

    private PropertyPenalty getPenalty(double amount, double remainingAmount, String date) throws ParseException {
        return PropertyPenalty.builder().penaltyAmount(amount).remainingPenaltyDue(remainingAmount)
                .status(remainingAmount > 0 ? PaymentStatusEnum.UNPAID : PaymentStatusEnum.PAID)
                .generationDate(getEpochFromDateString(date)).build();
    }

    private long getEpochFromDateString(String date) throws ParseException {
        return this.getDateFromString(date).getTime();
    }

    private static final String DATE_FORMAT = "dd MM yyyy";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);

    private Date getDateFromString(String date) throws ParseException {
        return dateFormatter.parse(date);
    }
}
