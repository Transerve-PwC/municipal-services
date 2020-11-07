package org.egov.ps.service;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.stream.Collectors;

import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstateModuleResponse;
import org.egov.ps.web.contracts.EstatePayment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ReadExcelServiceTest {

	EstateCalculationExcelReadService estateCalculationExcelReadService;

	@Before
	public void setup() {
		this.estateCalculationExcelReadService = new EstateCalculationExcelReadService();
	}

	private void testExcelParsing(String excelFileToParse, int sheetNo, double expectedReceivedRent) {

		InputStream inputStream = ReadExcelServiceTest.class.getClassLoader().getResourceAsStream(excelFileToParse);
		EstateModuleResponse responsne = estateCalculationExcelReadService.getDatafromExcel(inputStream, sheetNo);
		Double rentReceived = responsne.getEstatePayments().stream().map(EstatePayment::getRentReceived)
				.collect(Collectors.summingDouble(Double::doubleValue));
		assertEquals(expectedReceivedRent, rentReceived, 1.0);
		assertEquals(expectedReceivedRent, rentReceived, 1.0);
	}

	private void testExcelParsingAll(String excelFileToParse, int sheetNo, double expectedReceivedRent,
			double expectedRentDue, double expectedPenaltyInterest, double expectedGstInterest) {

		InputStream inputStream = ReadExcelServiceTest.class.getClassLoader().getResourceAsStream(excelFileToParse);
		EstateModuleResponse responsne = estateCalculationExcelReadService.getDatafromExcel(inputStream, sheetNo);

		Double rentReceived = responsne.getEstatePayments().stream().map(EstatePayment::getRentReceived)
				.collect(Collectors.summingDouble(Double::doubleValue));
		Double rentDue = responsne.getEstateDemands().stream().map(EstateDemand::getRent)
				.collect(Collectors.summingDouble(Double::doubleValue));
		Double penaltyInterest = responsne.getEstateDemands().stream().map(EstateDemand::getPenaltyInterest)
				.collect(Collectors.summingDouble(Double::doubleValue));
		Double gstInterest = responsne.getEstateDemands().stream().map(EstateDemand::getGstInterest)
				.collect(Collectors.summingDouble(Double::doubleValue));
				
		assertEquals(expectedReceivedRent, rentReceived, 1.0);
		assertEquals(expectedRentDue, rentDue, 1.0);
		assertEquals(expectedPenaltyInterest, penaltyInterest, 1.0);
		assertEquals(expectedGstInterest, gstInterest, 3.0);
	}
	
	@Test
	public void testReadExcelsheetTemp() throws FileNotFoundException {
		String filepath = "excel/EstatePayment calculation_sriraj.xlsx";
		testExcelParsingAll(filepath, 0, 47770, 57001, 5697, 461);
	}

	@Test
	public void testReadExcelsheet0() throws FileNotFoundException {
		// String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		// testExcelParsingAll(filepath, 0, 47770, 57001,5697,464);
		String filepath = "excel/Estate Module Payment Calculation_DR4.xlsx";
		testExcelParsingAll(filepath, 0, 47770, 55323, 5697, 455);
	}

	@Test
	public void testReadExcelsheet1() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 1, 13200, 46250, 3966, 1288);
	}

	@Test
	public void testReadExcelsheet4() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		// testExcelParsingAll(filepath, 4, 3923520,10512880,1011356,35286);
		testExcelParsingAll(filepath, 4, 3923520, 10512880, 1038088, 35286);
	}

	@Test
	public void testReadExcelsheet5_verka() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 5, 48222407, 54790640, 2723264, 848936);
	}

	@Test
	public void testReadExcelsheet6_LA2() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 6, 74992, 148574, 10810, 4716);
	}

	@Test
	public void testReadExcelsheet7_LA1() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 7, 40680, 97997, 4266, 3740);
	}

	@Test /* Equation was missing in last column */
	public void testReadExcelsheet8_Dhanas_2() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		// testExcelParsingAll(filepath, 8, 81137,84474,4538,1721);
		testExcelParsingAll(filepath, 8, 81137, 84474, 4538, 1785);
	}

	@Test /* Equation was missing in last column */
	public void testReadExcelsheet9_Dhanas_7() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		// testExcelParsingAll(filepath, 9, 89107,91572,6323,1881);
		testExcelParsingAll(filepath, 9, 89107, 91572, 6323, 1954);
	}

	@Test /* Equation was missing in last column */
	public void testReadExcelsheet10_Dhanas_1() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		// testExcelParsingAll(filepath, 10, 79156,82493,3926,1704);
		testExcelParsingAll(filepath, 10, 79156, 82493, 3926, 1839);
	}

	@Test
	public void testReadExcelsheet11_Dhanas_8() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 11, 22840, 85300, 6528, 3015);
	}

	@Test
	public void testReadExcelsheet12_Dstore() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 12, 12732, 38880, 3888, 833);
	}

	@Test /* Equation was missing in last column also on blank entry with empty month */
	public void testReadExcelsheet13_CHD() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		// testExcelParsingAll(filepath, 13, 30000,43200,2430,37);
		testExcelParsingAll(filepath, 13, 30000, 43200, 2800, 560);
	}

	@Test
	public void testReadExcelsheet14_Dr5() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 14, 47770, 57001, 5697, 464);
	}

	@Test
	public void testReadExcelsheet15_dharia_11() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 15, 36719, 52565, 1121, 375);
	}

	@Test
	public void testReadExcelsheet16_dharia_11() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 16, 55012, 165156, 4538, 8118);
	}

	@Test
	public void testReadExcelsheet17_DR_8() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 17, 55734, 67757, 4659, 851);
	}

	@Test
	public void testReadExcelsheet18_DR_1() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 18, 16812, 84094, 5697, 3099);
	}

	@Test
	public void testReadExcelsheet19_DR_6() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 19, 37492, 43075, 1485, 518);
	}

	@Test
	public void testReadExcelsheet20_EX_LA() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 20, 0, 122556, 22800, 4732);
	}

	// @Test /*Fail because does not have month data so we are ignoring such fields
	// */
	// public void testReadExcelsheet21_Exchange_M() throws FileNotFoundException {
	// String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
	// testExcelParsing(filepath,21,626534);
	// }

	// @Test /*Fail because does not have month data so we are ignoring such fields
	// */
	// public void testReadExcelsheet22_tower_M () throws FileNotFoundException {
	// String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
	// testExcelParsing(filepath,22,740186);
	// }

	@Test /* Blank entry with empty month so ignore that value */
	public void testReadExcelsheet23_40_45() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		// testExcelParsingAll(filepath,23,267347,304166,20663,453);
		testExcelParsingAll(filepath, 23, 267347, 304166, 24200, 453);
	}

	// @Test /* ST/GST Rate is different in column and different in equation of
	// delayed payment calculation*/
	// public void testReadExcelsheet24_18_16() throws FileNotFoundException {
	// String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
	// //testExcelParsingAll(filepath,24,182000,185000,1700,144);
	// }
	//
	// @Test /* ST/GST Rate is different in some column and different in equation of
	// delayed payment calculation*/
	// public void testReadExcelsheet25_36_22() throws FileNotFoundException {
	// String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
	// //testExcelParsingAll(filepath,25,427000,430500,3630,9);
	// testExcelParsingAll(filepath,25,427000,430500,3630,9);
	// }

	@Test /*
			 * Blank entry with empty month so ignore that value & ST/GST Rate is different
			 * in column and different in equation of delayed payment calculation
			 */
	public void testReadExcelsheet26_14_11() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		// testExcelParsingAll(filepath,26,624549,643049,1850,9492);
		testExcelParsingAll(filepath, 26, 624549, 643049, 22350, 42563);
	}

	@Test /*
			 * ST/GST Rate is different in some column and different in equation of delayed
			 * payment calculation
			 */
	public void testReadExcelsheet27_15_8() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 27, 196500, 201500, 1900, 165);
	}

	@Test /*
			 * ST/GST Rate is different in some column and different in equation of delayed
			 * payment calculation
			 */
	public void testReadExcelsheet28_20_22() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 28, 218500, 223000, 2450, 256);
	}

	@Test /*
			 * Fail because does not have month data so we are ignoring such fields (Penalty
			 * Interest) & Format is very incorrect so can't read values
			 */
	public void testReadExcelsheet30_18x31_20x22_18x42() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 30, 1472500, 1472500, 5475, 24);
	}

	@Test /*
			 * ST/GST Rate is different in some column and different in equation of delayed
			 * payment calculation
			 */
	public void testReadExcelsheet31_M_17() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 31, 223965, 251465, 2600, 1432);
	}

	@Test /*
			 * ST/GST Rate is different in some column and different in equation of delayed
			 * payment calculation
			 */
	public void testReadExcelsheet32_M_3() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 32, 207500, 224500, 5400, 292);
	}

	@Test
	public void testReadExcelsheet33_M_18() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 33, 231797, 239668, 2469, 102);
	}

	@Test /* Previous bal st/gst rate is empty */
	public void testReadExcelsheet34_M_14() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 34, 88000, 203349, 15500, 3346);
	}

	@Test /* Previous bal st/gst rate is empty */
	public void testReadExcelsheet35_M_11() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsingAll(filepath, 35, 54000, 122912, 18250, 5920);
	}

	@Test
	public void testReadExcelsheet36_k3_Jap() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsing(filepath, 36, 55038);
	}

	@Test
	public void testReadExcelsheet37_maloya_pO() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsing(filepath, 37, 14420);
	}

	@Test
	public void testReadExcelsheet38_20_Beh() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsing(filepath, 38, 14420);
	}

	@Test
	public void testReadExcelsheet39_11_Beh() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsing(filepath, 39, 14420);
	}

	@Test
	public void testReadExcelsheet40_9_Beh() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsing(filepath, 40, 14420);
	}

	@Test
	public void testReadExcelsheet41_KIOSK_3() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsing(filepath, 41, 345000);
	}

	@Test
	public void testReadExcelsheet42_KIOSK_2() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsing(filepath, 42, 360000);
	}

	@Test
	public void testReadExcelsheet43_KIOSK_1() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsing(filepath, 43, 390000);
	}

	@Test
	public void testReadExcelsheet44_1Beh() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsing(filepath, 44, 14420);
	}

	@Test
	public void testReadExcelsheet45_kb() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsing(filepath, 45, 1260000);
	}

	@Test
	public void testReadExcelsheet46_Jai_GAS() throws FileNotFoundException {
		String filepath = "excel/Estate Module Revised Calculation sheet.xlsx";
		testExcelParsing(filepath, 47, 1260000);

	}
}
