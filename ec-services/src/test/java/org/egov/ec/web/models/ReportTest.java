package org.egov.ec.web.models;

import java.text.DateFormat;
import java.util.Date;

import org.egov.ec.web.models.Report;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class <code>ReportTest</code> contains tests for the class <code>{@link Report}</code>.
 *
 * @generated at 17/5/20 2:10 PM
 * @author Aniket.Marathe
 * @version $Revision: 1.0 $
 */
public class ReportTest {
	/**
	 * Run the Report() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testReport_1()
		throws Exception {

		Report result = new Report();

		
		assertNotNull(result);
		assertEquals("Report(reportType=null, challanId=null, paymentAmount=null, fromDate=null, toDate=null, violationDate=null, paymentStatus=null, sector=null, encroachmentType=null, siName=null, challanStatus=null, violatorName=null, age=null, violationUuid=null, vehicleNumber=null, paymentMode=null, itemsAgeFrom=null, itemsAgeTo=null, itemName=null, itemQuantity=null, itemStoreDepositDate=null, createdBy=null, createdTime=null, lastModifiedBy=null, lastModifiedTime=null, tenantId=null, transactionId=null)", result.toString());
		assertEquals(null, result.getLastModifiedTime());
		assertEquals(null, result.getCreatedTime());
		assertEquals(null, result.getCreatedBy());
		assertEquals(null, result.getEncroachmentType());
		assertEquals(null, result.getFromDate());
		assertEquals(null, result.getToDate());
		assertEquals(null, result.getPaymentStatus());
		assertEquals(null, result.getSiName());
		assertEquals(null, result.getViolationUuid());
		assertEquals(null, result.getPaymentMode());
		assertEquals(null, result.getItemsAgeTo());
		assertEquals(null, result.getSector());
		assertEquals(null, result.getViolationDate());
		assertEquals(null, result.getItemsAgeFrom());
		assertEquals(null, result.getVehicleNumber());
		assertEquals(null, result.getChallanId());
		assertEquals(null, result.getReportType());
		assertEquals(null, result.getLastModifiedBy());
		assertEquals(null, result.getItemName());
		assertEquals(null, result.getItemQuantity());
		assertEquals(null, result.getItemStoreDepositDate());
		assertEquals(null, result.getChallanStatus());
		assertEquals(null, result.getTenantId());
		assertEquals(null, result.getTransactionId());
	}

	/**
	 * Run the Report(String,String,Date,Date,String,String,String,String,String,String,String,String,String,String,String,long,String,long) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testReport_2()
		throws Exception {
		String reportType = "";
		String challanId = "";
		Integer paymentAmount=null;
		Date fromDate =null;
		Date toDate = null;
		String ViolationDate = "";
		String paymentStatus = "";
		String sector = "";
		String encroachmentType = "";
		String siName = "";
		String violationUuid = "";
		String vehicleNumber = "";
		String paymentMode = "";
		String itemsAgeFrom = "";
		String itemsAgeTo = "";
		String createdBy = "";
		long createdTime = 1L;
		String lastModifiedBy = "";
		long lastModifiedTime = 1L;
		String itemName="";
		Integer itemQuantity=null;
		String itemStoreDepositDate="";
		String challanStatus="";
		String violatorName="";
		Integer age=null;
		String tenantId="";
		String transactionId = "";
		
		Report result = new Report(reportType, challanId, paymentAmount,fromDate, toDate, ViolationDate, paymentStatus, sector, encroachmentType, siName, challanStatus, violatorName, age, violationUuid, vehicleNumber, paymentMode, itemsAgeFrom, itemsAgeTo, itemName, itemQuantity, itemStoreDepositDate, createdBy, createdTime, lastModifiedBy, lastModifiedTime, tenantId,transactionId);

		
		assertNotNull(result);
		assertEquals("Report(reportType=, challanId=, paymentAmount=null, fromDate=null, toDate=null, violationDate=, paymentStatus=, sector=, encroachmentType=, siName=, challanStatus=, violatorName=, age=null, violationUuid=, vehicleNumber=, paymentMode=, itemsAgeFrom=, itemsAgeTo=, itemName=, itemQuantity=null, itemStoreDepositDate=, createdBy=, createdTime=1, lastModifiedBy=, lastModifiedTime=1, tenantId=, transactionId=)", result.toString());
		assertEquals(new Long(1L), result.getLastModifiedTime());
		assertEquals(new Long(1L), result.getCreatedTime());
		assertEquals("", result.getCreatedBy());
		assertEquals("", result.getEncroachmentType());
		assertEquals("", result.getPaymentStatus());
		assertEquals("", result.getSiName());
		assertEquals("", result.getViolationUuid());
		assertEquals("", result.getPaymentMode());
		assertEquals("", result.getItemsAgeTo());
		assertEquals("", result.getSector());
		assertEquals("", result.getViolationDate());
		assertEquals("", result.getItemsAgeFrom());
		assertEquals("", result.getVehicleNumber());
		assertEquals("", result.getChallanId());
		assertEquals("", result.getReportType());
		assertEquals("", result.getLastModifiedBy());
		assertEquals(null, result.getPaymentAmount());
		assertEquals("", result.getItemName());
		assertEquals(null, result.getItemQuantity());
		assertEquals("", result.getItemStoreDepositDate());
		assertEquals("", result.getChallanStatus());
		assertEquals("", result.getViolatorName());
		assertEquals(null, result.getAge());
		assertEquals("", result.getTenantId());
		assertEquals("", result.getTransactionId());

	}

	/**
	 * Run the Report.ReportBuilder builder() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testBuilder_1()
		throws Exception {

		Report.ReportBuilder result = Report.builder();

		
		assertNotNull(result);
		assertEquals("Report.ReportBuilder(reportType=null, challanId=null, paymentAmount=null, fromDate=null, toDate=null, violationDate=null, paymentStatus=null, sector=null, encroachmentType=null, siName=null, challanStatus=null, violatorName=null, age=null, violationUuid=null, vehicleNumber=null, paymentMode=null, itemsAgeFrom=null, itemsAgeTo=null, itemName=null, itemQuantity=null, itemStoreDepositDate=null, createdBy=null, createdTime=null, lastModifiedBy=null, lastModifiedTime=null, tenantId=null, transactionId=null)", result.toString());
	}
	
	@Test
	public void testBuilder_2()
		throws Exception {

		Report.ReportBuilder builder = new Report.ReportBuilder();
		builder.reportType(null);
		builder.challanId(null);
		builder.createdBy(null);
		builder.createdTime(null);
		builder.paymentAmount(null);
		builder.fromDate(null);
		builder.lastModifiedBy(null);
		builder.lastModifiedTime(null);
		builder.toDate(null);
		builder.violationDate(null);
		builder.paymentStatus(null);
		builder.sector(null);
		builder.encroachmentType(null);
		builder.siName(null);
		builder.paymentStatus(null);
		builder.violationUuid(null);
		builder.vehicleNumber(null);
		builder.itemsAgeFrom(null);
		builder.itemsAgeTo(null);
		builder.paymentMode(null);
		builder.itemName(null);
		builder.itemQuantity(null);
		builder.itemStoreDepositDate(null);
		builder.challanStatus(null);
		builder.violatorName(null);
		builder.age(null);
		builder.tenantId(null);
		builder.build();

		Report.ReportBuilder builder2 = new Report.ReportBuilder();
		builder2.reportType(null);
		builder2.challanId(null);
		builder2.createdBy(null);
		builder2.createdTime(null);
		builder2.paymentAmount(null);
		builder2.fromDate(null);
		builder2.lastModifiedBy(null);
		builder2.lastModifiedTime(null);
		builder2.toDate(null);
		builder2.violationDate(null);
		builder2.paymentStatus(null);
		builder2.sector(null);
		builder2.encroachmentType(null);
		builder2.siName(null);
		builder2.paymentStatus(null);
		builder2.violationUuid(null);
		builder2.vehicleNumber(null);
		builder2.itemsAgeFrom(null);
		builder2.itemsAgeTo(null);
		builder2.paymentMode(null);
		builder2.itemName(null);
		builder2.itemQuantity(null);
		builder2.itemStoreDepositDate(null);
		builder2.challanStatus(null);
		builder2.violatorName(null);
		builder2.age(null);
		builder2.tenantId(null);
		builder2.build();

		Assert.assertEquals(builder2.toString(), builder.toString());
	}

	/**
	 * Run the String getViolationDate() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetViolationDate_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getViolationDate();

		
		assertEquals("", result);
	}

	/**
	 * Run the String getChallanId() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetChallanId_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getChallanId();

		
		assertEquals("", result);
	}

	/**
	 * Run the String getCreatedBy() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetCreatedBy_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getCreatedBy();

		
		assertEquals("", result);
	}

	/**
	 * Run the long getCreatedTime() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetCreatedTime_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		long result = fixture.getCreatedTime();

		
		assertEquals(1L, result);
	}

	/**
	 * Run the String getEncroachmentType() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetEncroachmentType_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getEncroachmentType();

		
		assertEquals("", result);
	}

	/**
	 * Run the Date getFromDate() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetFromDate_1()
		throws Exception {
		Report fixture = new Report("", "", null, null, null, "", "", "", "", "", "", "", null, "", "", "", "", null, "", null, "", null, 1L, "", 1L,"", null);

		Date result = fixture.getFromDate();
		assertEquals(null, result);
	}

	/**
	 * Run the String getItemsAgeFrom() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetItemsAgeFrom_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getItemsAgeFrom();

		
		assertEquals("", result);
	}

	/**
	 * Run the String getItemsAgeTo() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetItemsAgeTo_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getItemsAgeTo();

		
		assertEquals("", result);
	}

	/**
	 * Run the String getLastModifiedBy() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetLastModifiedBy_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getLastModifiedBy();

		
		assertEquals("", result);
	}

	/**
	 * Run the long getLastModifiedTime() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetLastModifiedTime_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		long result = fixture.getLastModifiedTime();

		
		assertEquals(1L, result);
	}

	/**
	 * Run the String getPaymentMode() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetPaymentMode_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getPaymentMode();

		
		assertEquals("", result);
	}

	/**
	 * Run the String getPaymentStatus() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetPaymentStatus_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getPaymentStatus();

		
		assertEquals("", result);
	}

	/**
	 * Run the String getReportType() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetReportType_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getReportType();

		
		assertEquals("", result);
	}

	/**
	 * Run the String getSector() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetSector_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getSector();

		
		assertEquals("", result);
	}

	/**
	 * Run the String getSiName() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetSiName_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getSiName();

		
		assertEquals("", result);
	}

	/**
	 * Run the Date getToDate() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetToDate_1()
		throws Exception {
		Report fixture = new Report("", "", null, null, null, "", "", "", "", "", "", "", null, "", "", "", "", null, "", null, "", null, 1L, "", 1L,"", null);

		Date result = fixture.getToDate();
		assertEquals(null,result);
	}

	/**
	 * Run the String getVehicleNumber() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetVehicleNumber_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getVehicleNumber();

		
		assertEquals("", result);
	}

	/**
	 * Run the String getViolationUuid() method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testGetItemName_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getItemName();		
		assertEquals("", result);
	}

	@Test
	public void testGetItemQuantity_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		Integer result = fixture.getItemQuantity();		
		assertEquals(null, result);
	}
	@Test
	public void testGetItemStoreDepositDate_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getItemStoreDepositDate();
		
		assertEquals("", result);
	}
	@Test
	public void testGetViolationUuid_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getViolationUuid();
		assertEquals("", result);
	}
	
	@Test
	public void testGetChallanStatus_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getChallanStatus();
		assertEquals("", result);
	}
	
	@Test
	public void testGetViolatorName_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getViolatorName();
		assertEquals("", result);
	}
	
	@Test
	public void testGetAge_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		Integer result = fixture.getAge();
		assertEquals(null, result);
	}
	
	@Test
	public void testGetTenantId_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);

		String result = fixture.getTenantId();
		assertEquals("", result);
	}
	
	/**
	 * Run the void setViolationDate(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetViolationDate_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String ViolationDate = "";

		fixture.setViolationDate(ViolationDate);

		
	}

	/**
	 * Run the void setChallanId(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetChallanId_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String challanId = "";

		fixture.setChallanId(challanId);

		
	}

	/**
	 * Run the void setCreatedBy(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetCreatedBy_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String createdBy = "";

		fixture.setCreatedBy(createdBy);

		
	}

	/**
	 * Run the void setCreatedTime(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetCreatedTime_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		long createdTime = 1L;

		fixture.setCreatedTime(createdTime);

		
	}

	/**
	 * Run the void setEncroachmentType(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetEncroachmentType_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String encroachmentType = "";

		fixture.setEncroachmentType(encroachmentType);

		
	}

	/**
	 * Run the void setFromDate(Date) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetFromDate_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		Date fromDate = new Date();

		fixture.setFromDate(fromDate);

		
	}

	/**
	 * Run the void setItemsAgeFrom(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetItemsAgeFrom_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String itemsAgeFrom = "";

		fixture.setItemsAgeFrom(itemsAgeFrom);

		
	}

	/**
	 * Run the void setItemsAgeTo(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetItemsAgeTo_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String itemsAgeTo = "";

		fixture.setItemsAgeTo(itemsAgeTo);

		
	}

	/**
	 * Run the void setLastModifiedBy(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetLastModifiedBy_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String lastModifiedBy = "";

		fixture.setLastModifiedBy(lastModifiedBy);

		
	}

	/**
	 * Run the void setLastModifiedTime(long) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetLastModifiedTime_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		long lastModifiedTime = 1L;

		fixture.setLastModifiedTime(lastModifiedTime);

		
	}

	/**
	 * Run the void setPaymentMode(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetPaymentMode_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String paymentMode = "";

		fixture.setPaymentMode(paymentMode);

		
	}

	/**
	 * Run the void setPaymentStatus(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetPaymentStatus_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String paymentStatus = "";

		fixture.setPaymentStatus(paymentStatus);

		
	}

	/**
	 * Run the void setReportType(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetReportType_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String reportType = "";

		fixture.setReportType(reportType);

		
	}

	/**
	 * Run the void setSector(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetSector_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String sector = "";

		fixture.setSector(sector);

		
	}

	/**
	 * Run the void setSiName(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetSiName_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String siName = "";

		fixture.setSiName(siName);

		
	}

	/**
	 * Run the void setToDate(Date) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetToDate_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		Date toDate = new Date();

		fixture.setToDate(toDate);

		
	}

	/**
	 * Run the void setVehicleNumber(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetVehicleNumber_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String vehicleNumber = "";

		fixture.setVehicleNumber(vehicleNumber);

		
	}

	/**
	 * Run the void setViolationUuid(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generated at 17/5/20 2:10 PM
	 */
	@Test
	public void testSetViolationUuid_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String violationUuid = "";

		fixture.setViolationUuid(violationUuid);

		
	}
	
	@Test
	public void testSetPaymentAmount_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		Integer paymentAmount = null;

		fixture.setPaymentAmount(paymentAmount);

		
	}
	
	@Test
	public void testSetItemName_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String itemName = null;

		fixture.setItemName(itemName);

		
	}
	
	@Test
	public void testSetItemStoreDepositDate_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String itemStoreDepositDate = null;

		fixture.setItemStoreDepositDate(itemStoreDepositDate);

		
	}
	
	@Test
	public void testSetItemQuantity_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		Integer itemQuantity = null;

		fixture.setItemQuantity(itemQuantity);;

		
	}
	
	@Test
	public void testSetChallanStatus_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String challanStatus = null;

		fixture.setChallanStatus(challanStatus);;

		
	}
	
	@Test
	public void testSetViolatorName_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String violatorName = null;

		fixture.setViolatorName(violatorName);;

		
	}
	
	@Test
	public void testSetAge_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		Integer age = null;

		fixture.setAge(age);;

		
	}
	
	@Test
	public void testSetTenantId_1()
		throws Exception {
		Report fixture = new Report("", "", null, new Date(), new Date(), "", "", "", "", "", "", "", null, "", "", "" ,"", "", "", null, "", "", 1L, "", 1L,"", null);
		String tenantId = null;

		fixture.setTenantId(tenantId);

		
	}

}