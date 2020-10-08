package org.egov.cpt.service;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.text.ParseException;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentDemandResponse;
import org.egov.cpt.models.RentPayment;
import org.egov.cpt.service.xlsxparsing.IReadExcelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ReadExcelServiceTests {

	IReadExcelService readExcelService;

	@Before
	public void setup() {
		// this.readExcelService = new
		// org.egov.cpt.service.xlsxparsing.ReadExcelService();
		this.readExcelService = new org.egov.cpt.service.xlsxparsing.ReadExcelStreamingService();
	}

	private void _testExcelParsing(String excelFileToParse, int sheetNo, double expectedTotalRent,
			double expectedTotalPaid) {
		System.out.println("Parsing file: " + excelFileToParse + ", sheet no " + sheetNo);
		InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
		RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, sheetNo);
		Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
				.collect(Collectors.summingDouble(Double::doubleValue));
		Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
				.collect(Collectors.summingDouble(Double::doubleValue));
		assertEquals(0, rentDemandResponse.getPayment().stream().map(RentPayment::getDateOfPayment)
				.filter(date -> date == null).count());
		assertEquals(expectedTotalRent, totalRent, 1.0);
		assertEquals(expectedTotalPaid, totalPaid, 1.0);
	}

	private static final String FILE_501_TO_520_XLSX = "calculations/501 to 520.xlsx";
	private static final String FILE_521_TO_530_XLSX = "calculations/521 to 530.xlsx";
	private static final String FILE_531_TO_540_XLSX = "calculations/531 to 540.xlsx";
	private static final String FILE_541_TO_549_XLSX = "calculations/541 to 549.xlsx";

	@Test
	public void testSimpleParsing() {
		/* Sheet no. 502 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 0, 135708.0, 123536.0);
	}

	@Test
	public void testSheet1() {
		/* Sheet no. 502 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 1, 135708.0, 123035.0);
	}

	@Test
	public void testSheet2() {
		/* Sheet no. 503 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 2, 135708.0, 76485.0);
	}

	@Test
	public void testSheet3() {
		/* Sheet no. 504 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 3, 135708.0, 117637.0);
	}

	@Test
	public void testSheet4() {
		/* Sheet no. 505 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 4, 135708.0, 123535.0);
	}

	@Test
	public void testSheet5() {
		/* Sheet no. 506 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 5, 135708.0, 98970.0);
	}

	@Test
	public void testSheet6() {
		/* Sheet no. 507 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 6, 135708.0, 123535.0);
	}

	@Test
	public void testSheet7() {
		/* Sheet no. 508 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 7, 135708.0, 95158.0);
	}

	@Test
	public void testSheet8() {
		/* Sheet no. 509 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 8, 135734.0, 90373.0);
	}

	@Test
	public void testSheet9() {
		/* Sheet no. 510 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 9, 135734.0, 111937.0);
	}

	@Test
	public void testSheet10() {
		/* Sheet no. 511 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 10, 135734.0, 114027.0);
	}

	@Test
	public void testSheet11() {
		/* Sheet no. 512 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 11, 135734.0, 116652.0);
	}

	@Test
	public void testSheet12() {
		/* Sheet no. 513 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 12, 135734.0, 125426.0);
	}

	@Test
	public void testSheet13() {
		/* Sheet no. 514 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 13, 135734.0, 123535.0);
	}

	@Test
	public void testSheet14() {
		/* Sheet no. 515 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 14, 135734.0, 123557.0);
	}

	@Test
	public void testSheet15() {
		/* Sheet no. 516 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 15, 135734.0, 111715.0);
	}

	@Test
	public void testSheet16() {
		/* Sheet no. 517 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 16, 135734.0, 123535.0);
	}

	@Test
	public void testSheet17() {
		/* Sheet no. 518 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 17, 135734.0, 126663.0);
	}

	@Test
	public void testSheet18() {
		/* Sheet no. 519 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 18, 135734.0, 142764.0);
	}

	@Test
	public void testSheet19() {
		/* Sheet no. 520 */
		this._testExcelParsing(FILE_501_TO_520_XLSX, 19, 135734.0, 121612.0);
	}

	@Test
	public void testSheet521() {
		/* Sheet no. 521 */
		this._testExcelParsing(FILE_521_TO_530_XLSX, 0, 135708.0, 20478.0);
	}

	@Test
	public void testSheet522() {
		/* Sheet no. 522 */
		this._testExcelParsing(FILE_521_TO_530_XLSX, 1, 135708.0, 115709.0);
	}

	@Test
	public void testSheet523() {
		/* Sheet no. 523 */
		this._testExcelParsing(FILE_521_TO_530_XLSX, 2, 135708.0, 45186.0);
	}

	@Test
	public void testSheet524() {
		/* Sheet no. 524 */
		this._testExcelParsing(FILE_521_TO_530_XLSX, 3, 135708.0, 34892.0);
	}

	@Test
	public void testSheet525() {
		/* Sheet no. 525 */
		this._testExcelParsing(FILE_521_TO_530_XLSX, 4, 135708.0, 123788.0);
	}

	@Test
	public void testSheet526() {
		/* Sheet no. 526 */
		this._testExcelParsing(FILE_521_TO_530_XLSX, 5, 135708.0, 61728.0);
	}

	@Test
	public void testSheet527() {
		/* Sheet no. 527 */
		this._testExcelParsing(FILE_521_TO_530_XLSX, 6, 135708.0, 35150.0);
	}

	@Test
	public void testSheet528() {
		/* Sheet no. 528 */
		this._testExcelParsing(FILE_521_TO_530_XLSX, 7, 135708.0, 111937.0);
	}

	@Test
	public void testSheet529() {
		/* Sheet no. 529 */
		this._testExcelParsing(FILE_521_TO_530_XLSX, 8, 135708.0, 97525.0);
	}

	@Test
	public void testSheet530() {
		/* Sheet no. 530 */
		this._testExcelParsing(FILE_521_TO_530_XLSX, 9, 135708.0, 105158.0);
	}

	@Test
	public void testSheet531() {
		/* Sheet no. 531 */
		this._testExcelParsing(FILE_531_TO_540_XLSX, 7, 135708.0, 45478.0);
	}

	@Test
	public void testSheet532() {
		/* Sheet no. 532 */
		this._testExcelParsing(FILE_531_TO_540_XLSX, 8, 135708.0, 95725.0);
	}

	@Test
	public void testSheet533() {
		/* Sheet no. 533 */
		this._testExcelParsing(FILE_531_TO_540_XLSX, 9, 135708.0, 123535.0);
	}

	@Test
	public void testSheet534() {
		/* Sheet no. 534 */
		this._testExcelParsing(FILE_531_TO_540_XLSX, 10, 135708.0, 123535.0);
	}

	@Test
	public void testSheet535() {
		/* Sheet no. 535 */
		this._testExcelParsing(FILE_531_TO_540_XLSX, 11, 135708.0, 123535.0);
	}

	@Test
	public void testSheet536() {
		/* Sheet no. 536 */
		this._testExcelParsing(FILE_531_TO_540_XLSX, 12, 135708.0, 105658.0);
	}

	@Test
	public void testSheet537() {
		/* Sheet no. 537 */
		this._testExcelParsing(FILE_531_TO_540_XLSX, 13, 135708.0, 62775.0);
	}

	@Test
	public void testSheet538() {
		/* Sheet no. 538 */
		this._testExcelParsing(FILE_531_TO_540_XLSX, 14, 135708.0, 62372.0);
	}

	@Test
	public void testSheet539() {
		/* Sheet no. 539 */
		this._testExcelParsing(FILE_531_TO_540_XLSX, 15, 124531.0, 90380.0);
	}

	@Test
	public void testSheet540() {
		/* Sheet no. 540 */
		this._testExcelParsing(FILE_531_TO_540_XLSX, 16, 124531.0, 112838.0);
	}

	@Test
	public void testSheet541() {
		/* Sheet no. 541 */
		this._testExcelParsing(FILE_541_TO_549_XLSX, 0, 124531.0, 91500.0);
	}

	@Test
	public void testSheet542() {
		/* Sheet no. 542 */
		this._testExcelParsing(FILE_541_TO_549_XLSX, 1, 124531.0, 102782.0);
	}

	@Test
	public void testSheet543() {
		/* Sheet no. 543 */
		this._testExcelParsing(FILE_541_TO_549_XLSX, 2, 124531.0, 62000.0);
	}

	@Test
	public void testSheet544() {
		/* Sheet no. 544 */
		this._testExcelParsing(FILE_541_TO_549_XLSX, 3, 135708.0, 112924.0);
	}

	@Test
	public void testSheet545() {
		/* Sheet no. 545 */
		this._testExcelParsing(FILE_541_TO_549_XLSX, 4, 124531.0, 81792.0);
	}

	@Test
	public void testSheet546() {
		/* Sheet no. 546 */
		this._testExcelParsing(FILE_541_TO_549_XLSX, 5, 124531.0, 101546.0);
	}

	@Test
	public void testSheet547() {
		/* Sheet no. 547 */
		this._testExcelParsing(FILE_541_TO_549_XLSX, 6, 135708.0, 112000.0);
	}

	@Test
	public void testSheet548() {
		/* Sheet no. 548 */
		this._testExcelParsing(FILE_541_TO_549_XLSX, 7, 135708.0, 111730.0);
	}

	@Test
	public void testSheet549() {
		/* Sheet no. 549 */
		this._testExcelParsing(FILE_541_TO_549_XLSX, 8, 124531.0, 116852.0);
	}

	@Test
	public void testSplitReceiptData() {
		final String str = "10/10";
		Pattern pattern = Pattern.compile("^\\d*");
		Matcher matcher = pattern.matcher(str);
		assertEquals(true, matcher.find());
		assertEquals("10", matcher.group());
	}

	@Test
	public void testExtractDate() throws ParseException {
		final String[] MONTHS = new String[] { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT",
				"NOV", "DEC" };
		String str = "Aug.-20";
		Pattern monthPattern = Pattern.compile("^\\w*");
		Matcher monthMatcher = monthPattern.matcher(str);
		if (monthMatcher.find()) {
			String month = monthMatcher.group().toUpperCase();
			int monthIndex = Arrays.asList(MONTHS).indexOf(month);
			if (monthIndex < 0) {
				throw new DateTimeParseException("Cannot parse " + str + " as a date.", null, 0);
			}
			Pattern datePattern = Pattern.compile("\\d*$");
			Matcher dateMatcher = datePattern.matcher(str);
			if (dateMatcher.find()) {
				String twoYearDate = dateMatcher.group();
				int twoYearDateInt = Integer.parseInt(twoYearDate);
				if (twoYearDateInt >= 100) {
					throw new DateTimeParseException("Cannot parse " + str + " as a date.", null, 0);
				}
				int year = twoYearDateInt < 50 ? 2000 + twoYearDateInt : 1900 + twoYearDateInt;
				Calendar calendar = Calendar.getInstance();
				calendar.set(year, monthIndex, 1);
			}
		}
	}
}