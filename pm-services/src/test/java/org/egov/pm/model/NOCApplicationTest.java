package org.egov.pm.model;

import java.math.BigDecimal;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class <code>NOCApplicationTest</code> contains tests for the class <code>{@link NOCApplication}</code>.
 *
 * @generatedBy CodePro at 8/6/20 5:13 AM
 * @author Aniket.Marathe
 * @version $Revision: 1.0 $
 */
public class NOCApplicationTest {
	/**
	 * Run the NOCApplication() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testNOCApplication_1()
		throws Exception {

		NOCApplication result = new NOCApplication();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getLastModifiedTime());
		assertEquals(null, result.getNocApplicationRemark());
		assertEquals(null, result.getApplicationUuid());
		assertEquals(null, result.getApplicationType());
		assertEquals(null, result.getPerformanceBankGuarantee());
		assertEquals(null, result.getNocApplicationDetails());
		assertEquals(null, result.getApplicationStatus());
		assertEquals(null, result.getLastModifiedBy());
		assertEquals(null, result.getTenantId());
		assertEquals(null, result.getNocNumber());
		assertEquals(null, result.getAppliedBy());
		assertEquals(null, result.getApplicantName());
		assertEquals(null, result.getBadgeNumber());
		assertEquals(null, result.getTotalamount());
		assertEquals(null, result.getGstAmount());
		assertEquals(null, result.getCreatedTime());
		assertEquals(null, result.getAppliedDate());
		assertEquals(null, result.getHouseNo());
		assertEquals(null, result.getSector());
		assertEquals(null, result.getAmount());
		assertEquals(null, result.getIsActive());
		assertEquals(null, result.getCreatedBy());
	}

	/**
	 * Run the NOCApplication(String,String,String,String,String,String,String,Boolean,String,String,Long,Long,String,String,String,BigDecimal,BigDecimal,BigDecimal,String,BigDecimal,NOCApplicationDetail,NOCApplicationRemark) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testNOCApplication_2()
		throws Exception {
		String applicationUuid = "";
		String tenantId = "";
		String nocNumber = "";
		String appliedBy = "";
		String appliedDate = "";
		String applicationType = "";
		String applicationStatus = "";
		Boolean isActive = new Boolean(true);
		String createdBy = "";
		String lastModifiedBy = "";
		Long createdTime = new Long(1L);
		Long lastModifiedTime = new Long(1L);
		String applicantName = "";
		String houseNo = "";
		String sector = "";
		BigDecimal amount = new BigDecimal(1.0);
		BigDecimal gstAmount = new BigDecimal(1.0);
		BigDecimal performanceBankGuarantee = new BigDecimal(1.0);
		String badgeNumber = "";
		BigDecimal totalamount = new BigDecimal(1.0);
		NOCApplicationDetail nocApplicationDetails = new NOCApplicationDetail();
		NOCApplicationRemark nocApplicationRemark = new NOCApplicationRemark();

		NOCApplication result = new NOCApplication(applicationUuid, tenantId, nocNumber, appliedBy, appliedDate, applicationType, applicationStatus, isActive, createdBy, lastModifiedBy, createdTime, lastModifiedTime, applicantName, houseNo, sector, amount, gstAmount, performanceBankGuarantee, badgeNumber, totalamount, nocApplicationDetails, nocApplicationRemark);

		// add additional test code here
		assertNotNull(result);
		assertEquals(new Long(1L), result.getLastModifiedTime());
		assertEquals("", result.getApplicationUuid());
		assertEquals("", result.getApplicationType());
		assertEquals("", result.getApplicationStatus());
		assertEquals("", result.getLastModifiedBy());
		assertEquals("", result.getTenantId());
		assertEquals("", result.getNocNumber());
		assertEquals("", result.getAppliedBy());
		assertEquals("", result.getApplicantName());
		assertEquals("", result.getBadgeNumber());
		assertEquals(new Long(1L), result.getCreatedTime());
		assertEquals("", result.getAppliedDate());
		assertEquals("", result.getHouseNo());
		assertEquals("", result.getSector());
		assertEquals(Boolean.TRUE, result.getIsActive());
		assertEquals("", result.getCreatedBy());
	}

	/**
	 * Run the NOCApplication.NOCApplicationBuilder builder() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testBuilder_1()
		throws Exception {

		NOCApplication.NOCApplicationBuilder result = NOCApplication.builder();

		// add additional test code here
		assertNotNull(result);
		assertEquals("NOCApplication.NOCApplicationBuilder(applicationUuid=null, tenantId=null, nocNumber=null, appliedBy=null, appliedDate=null, applicationType=null, applicationStatus=null, isActive=null, createdBy=null, lastModifiedBy=null, createdTime=null, lastModifiedTime=null, applicantName=null, houseNo=null, sector=null, amount=null, gstAmount=null, performanceBankGuarantee=null, badgeNumber=null, totalamount=null, nocApplicationDetails=null, nocApplicationRemark=null)", result.toString());
	}

	/**
	 * Run the BigDecimal getAmount() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetAmount_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		BigDecimal result = fixture.getAmount();

		// add additional test code here
		assertNotNull(result);
		assertEquals("1", result.toString());
		assertEquals(1, result.intValue());
		assertEquals(1L, result.longValue());
		assertEquals(1.0f, result.floatValue(), 1.0f);
		assertEquals(1.0, result.doubleValue(), 1.0);
		assertEquals(1, result.signum());
		assertEquals(0, result.scale());
		assertEquals((byte) 1, result.byteValueExact());
		assertEquals(1, result.intValueExact());
		assertEquals(1L, result.longValueExact());
		assertEquals((short) 1, result.shortValueExact());
		assertEquals(1, result.precision());
		assertEquals("1", result.toEngineeringString());
		assertEquals("1", result.toPlainString());
		assertEquals((byte) 1, result.byteValue());
		assertEquals((short) 1, result.shortValue());
	}

	/**
	 * Run the String getApplicantName() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetApplicantName_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		String result = fixture.getApplicantName();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the String getApplicationStatus() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetApplicationStatus_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		String result = fixture.getApplicationStatus();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the String getApplicationType() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetApplicationType_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		String result = fixture.getApplicationType();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the String getApplicationUuid() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetApplicationUuid_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		String result = fixture.getApplicationUuid();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the String getAppliedBy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetAppliedBy_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		String result = fixture.getAppliedBy();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the String getAppliedDate() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetAppliedDate_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		String result = fixture.getAppliedDate();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the String getBadgeNumber() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetBadgeNumber_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		String result = fixture.getBadgeNumber();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the String getCreatedBy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetCreatedBy_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		String result = fixture.getCreatedBy();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the Long getCreatedTime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetCreatedTime_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		Long result = fixture.getCreatedTime();

		// add additional test code here
		assertNotNull(result);
		assertEquals("1", result.toString());
		assertEquals((byte) 1, result.byteValue());
		assertEquals((short) 1, result.shortValue());
		assertEquals(1, result.intValue());
		assertEquals(1L, result.longValue());
		assertEquals(1.0f, result.floatValue(), 1.0f);
		assertEquals(1.0, result.doubleValue(), 1.0);
	}

	/**
	 * Run the BigDecimal getGstAmount() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetGstAmount_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		BigDecimal result = fixture.getGstAmount();

		// add additional test code here
		assertNotNull(result);
		assertEquals("1", result.toString());
		assertEquals(1, result.intValue());
		assertEquals(1L, result.longValue());
		assertEquals(1.0f, result.floatValue(), 1.0f);
		assertEquals(1.0, result.doubleValue(), 1.0);
		assertEquals(1, result.signum());
		assertEquals(0, result.scale());
		assertEquals((byte) 1, result.byteValueExact());
		assertEquals(1, result.intValueExact());
		assertEquals(1L, result.longValueExact());
		assertEquals((short) 1, result.shortValueExact());
		assertEquals(1, result.precision());
		assertEquals("1", result.toEngineeringString());
		assertEquals("1", result.toPlainString());
		assertEquals((byte) 1, result.byteValue());
		assertEquals((short) 1, result.shortValue());
	}

	/**
	 * Run the String getHouseNo() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetHouseNo_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		String result = fixture.getHouseNo();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the Boolean getIsActive() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetIsActive_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		Boolean result = fixture.getIsActive();

		// add additional test code here
		assertNotNull(result);
		assertEquals("true", result.toString());
		assertEquals(true, result.booleanValue());
	}

	/**
	 * Run the String getLastModifiedBy() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetLastModifiedBy_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		String result = fixture.getLastModifiedBy();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the Long getLastModifiedTime() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetLastModifiedTime_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		Long result = fixture.getLastModifiedTime();

		// add additional test code here
		assertNotNull(result);
		assertEquals("1", result.toString());
		assertEquals((byte) 1, result.byteValue());
		assertEquals((short) 1, result.shortValue());
		assertEquals(1, result.intValue());
		assertEquals(1L, result.longValue());
		assertEquals(1.0f, result.floatValue(), 1.0f);
		assertEquals(1.0, result.doubleValue(), 1.0);
	}

	/**
	 * Run the NOCApplicationDetail getNocApplicationDetails() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetNocApplicationDetails_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		NOCApplicationDetail result = fixture.getNocApplicationDetails();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getLastModifiedTime());
		assertEquals(null, result.getApplicationDetailUuid());
		assertEquals(null, result.getApplicationDetail());
		assertEquals(null, result.getApplicationUuid());
		assertEquals(null, result.getLastModifiedBy());
		assertEquals(null, result.getTenantId());
		assertEquals(null, result.getCreatedTime());
		assertEquals(null, result.getIsActive());
		assertEquals(null, result.getCreatedBy());
	}

	/**
	 * Run the NOCApplicationRemark getNocApplicationRemark() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetNocApplicationRemark_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		NOCApplicationRemark result = fixture.getNocApplicationRemark();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getLastModifiedTime());
		assertEquals(null, result.getRemark());
		assertEquals(null, result.getRemarkBy());
		assertEquals(null, result.getDocumentId());
		assertEquals(null, result.getRemarkId());
		assertEquals(null, result.getApplicationUuid());
		assertEquals(null, result.getApplicationStatus());
		assertEquals(null, result.getLastModifiedBy());
		assertEquals(null, result.getTenantId());
		assertEquals(null, result.getCreatedTime());
		assertEquals(null, result.getIsActive());
		assertEquals(null, result.getCreatedBy());
	}

	/**
	 * Run the String getNocNumber() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetNocNumber_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		String result = fixture.getNocNumber();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the BigDecimal getPerformanceBankGuarantee() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetPerformanceBankGuarantee_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		BigDecimal result = fixture.getPerformanceBankGuarantee();

		// add additional test code here
		assertNotNull(result);
		assertEquals("1", result.toString());
		assertEquals(1, result.intValue());
		assertEquals(1L, result.longValue());
		assertEquals(1.0f, result.floatValue(), 1.0f);
		assertEquals(1.0, result.doubleValue(), 1.0);
		assertEquals(1, result.signum());
		assertEquals(0, result.scale());
		assertEquals((byte) 1, result.byteValueExact());
		assertEquals(1, result.intValueExact());
		assertEquals(1L, result.longValueExact());
		assertEquals((short) 1, result.shortValueExact());
		assertEquals(1, result.precision());
		assertEquals("1", result.toEngineeringString());
		assertEquals("1", result.toPlainString());
		assertEquals((byte) 1, result.byteValue());
		assertEquals((short) 1, result.shortValue());
	}

	/**
	 * Run the String getSector() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetSector_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		String result = fixture.getSector();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the String getTenantId() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetTenantId_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		String result = fixture.getTenantId();

		// add additional test code here
		assertEquals("", result);
	}

	/**
	 * Run the BigDecimal getTotalamount() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testGetTotalamount_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());

		BigDecimal result = fixture.getTotalamount();

		// add additional test code here
		assertNotNull(result);
		assertEquals("1", result.toString());
		assertEquals(1, result.intValue());
		assertEquals(1L, result.longValue());
		assertEquals(1.0f, result.floatValue(), 1.0f);
		assertEquals(1.0, result.doubleValue(), 1.0);
		assertEquals(1, result.signum());
		assertEquals(0, result.scale());
		assertEquals((byte) 1, result.byteValueExact());
		assertEquals(1, result.intValueExact());
		assertEquals(1L, result.longValueExact());
		assertEquals((short) 1, result.shortValueExact());
		assertEquals(1, result.precision());
		assertEquals("1", result.toEngineeringString());
		assertEquals("1", result.toPlainString());
		assertEquals((byte) 1, result.byteValue());
		assertEquals((short) 1, result.shortValue());
	}

	/**
	 * Run the void setAmount(BigDecimal) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetAmount_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		BigDecimal amount = new BigDecimal(1.0);

		fixture.setAmount(amount);

		// add additional test code here
	}

	/**
	 * Run the void setApplicantName(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetApplicantName_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		String applicantName = "";

		fixture.setApplicantName(applicantName);

		// add additional test code here
	}

	/**
	 * Run the void setApplicationStatus(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetApplicationStatus_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		String applicationStatus = "";

		fixture.setApplicationStatus(applicationStatus);

		// add additional test code here
	}

	/**
	 * Run the void setApplicationType(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetApplicationType_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		String applicationType = "";

		fixture.setApplicationType(applicationType);

		// add additional test code here
	}

	/**
	 * Run the void setApplicationUuid(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetApplicationUuid_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		String applicationUuid = "";

		fixture.setApplicationUuid(applicationUuid);

		// add additional test code here
	}

	/**
	 * Run the void setAppliedBy(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetAppliedBy_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		String appliedBy = "";

		fixture.setAppliedBy(appliedBy);

		// add additional test code here
	}

	/**
	 * Run the void setAppliedDate(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetAppliedDate_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		String appliedDate = "";

		fixture.setAppliedDate(appliedDate);

		// add additional test code here
	}

	/**
	 * Run the void setBadgeNumber(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetBadgeNumber_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		String badgeNumber = "";

		fixture.setBadgeNumber(badgeNumber);

		// add additional test code here
	}

	/**
	 * Run the void setCreatedBy(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetCreatedBy_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		String createdBy = "";

		fixture.setCreatedBy(createdBy);

		// add additional test code here
	}

	/**
	 * Run the void setCreatedTime(Long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetCreatedTime_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		Long createdTime = new Long(1L);

		fixture.setCreatedTime(createdTime);

		// add additional test code here
	}

	/**
	 * Run the void setGstAmount(BigDecimal) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetGstAmount_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		BigDecimal gstAmount = new BigDecimal(1.0);

		fixture.setGstAmount(gstAmount);

		// add additional test code here
	}

	/**
	 * Run the void setHouseNo(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetHouseNo_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		String houseNo = "";

		fixture.setHouseNo(houseNo);

		// add additional test code here
	}

	/**
	 * Run the void setIsActive(Boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetIsActive_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		Boolean isActive = new Boolean(true);

		fixture.setIsActive(isActive);

		// add additional test code here
	}

	/**
	 * Run the void setLastModifiedBy(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetLastModifiedBy_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		String lastModifiedBy = "";

		fixture.setLastModifiedBy(lastModifiedBy);

		// add additional test code here
	}

	/**
	 * Run the void setLastModifiedTime(Long) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetLastModifiedTime_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		Long lastModifiedTime = new Long(1L);

		fixture.setLastModifiedTime(lastModifiedTime);

		// add additional test code here
	}

	/**
	 * Run the void setNocApplicationDetails(NOCApplicationDetail) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetNocApplicationDetails_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		NOCApplicationDetail nocApplicationDetails = new NOCApplicationDetail();

		fixture.setNocApplicationDetails(nocApplicationDetails);

		// add additional test code here
	}

	/**
	 * Run the void setNocApplicationRemark(NOCApplicationRemark) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetNocApplicationRemark_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		NOCApplicationRemark nocApplicationRemark = new NOCApplicationRemark();

		fixture.setNocApplicationRemark(nocApplicationRemark);

		// add additional test code here
	}

	/**
	 * Run the void setNocNumber(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetNocNumber_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		String nocNumber = "";

		fixture.setNocNumber(nocNumber);

		// add additional test code here
	}

	/**
	 * Run the void setPerformanceBankGuarantee(BigDecimal) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetPerformanceBankGuarantee_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		BigDecimal performanceBankGuarantee = new BigDecimal(1.0);

		fixture.setPerformanceBankGuarantee(performanceBankGuarantee);

		// add additional test code here
	}

	/**
	 * Run the void setSector(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetSector_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		String sector = "";

		fixture.setSector(sector);

		// add additional test code here
	}

	/**
	 * Run the void setTenantId(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetTenantId_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		String tenantId = "";

		fixture.setTenantId(tenantId);

		// add additional test code here
	}

	/**
	 * Run the void setTotalamount(BigDecimal) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Test
	public void testSetTotalamount_1()
		throws Exception {
		NOCApplication fixture = new NOCApplication("", "", "", "", "", "", "", new Boolean(true), "", "", new Long(1L), new Long(1L), "", "", "", new BigDecimal(1.0), new BigDecimal(1.0), new BigDecimal(1.0), "", new BigDecimal(1.0), new NOCApplicationDetail(), new NOCApplicationRemark());
		BigDecimal totalamount = new BigDecimal(1.0);

		fixture.setTotalamount(totalamount);

		// add additional test code here
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@Before
	public void setUp()
		throws Exception {
		// add additional set up code here
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	@After
	public void tearDown()
		throws Exception {
		// Add additional tear down code here
	}

	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 * @generatedBy CodePro at 8/6/20 5:13 AM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(NOCApplicationTest.class);
	}
}