package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.egov.ps.model.CourtCase;
import org.egov.ps.model.Document;
import org.egov.ps.model.Owner;
import org.egov.ps.model.OwnerDetails;
import org.egov.ps.model.PaymentConfig;
import org.egov.ps.model.PaymentConfigItems;
import org.egov.ps.model.PremiumAmountConfigItems;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyDetails;
import org.egov.ps.web.contracts.AuditDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class PropertyRowMapper implements ResultSetExtractor<List<Property>> {

	@Override
	public List<Property> extractData(final ResultSet rs) throws SQLException, DataAccessException {

		final LinkedHashMap<String, Property> propertyMap = new LinkedHashMap<>();

		while (rs.next()) {

			Property currentProperty = null;

			if (hasColumn(rs, "pid")) {
				final String propertyId = rs.getString("pid");
				currentProperty = propertyMap.get(propertyId);
				final String tenantId = rs.getString("pttenantid");
				final String propertyDetailId = rs.getString("ptdlid");

				if (null == currentProperty) {
					final AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("pcreated_by"))
							.createdTime(rs.getLong("pcreated_time")).lastModifiedBy(rs.getString("pmodified_by"))
							.lastModifiedTime(rs.getLong("pmodified_time")).build();

					final AuditDetails pdAuditdetails = AuditDetails.builder().createdBy(rs.getString("pcreated_by"))
							.createdTime(rs.getLong("pcreated_time")).lastModifiedBy(rs.getString("pmodified_by"))
							.lastModifiedTime(rs.getLong("pmodified_time")).build();

					PaymentConfig paymentConfig = null;

					if (hasColumn(rs, "pc_id")) {
						String paymentConfigId = rs.getString("pc_id");
						if (paymentConfigId != null) {
							final String pcPropertyDetailsId = rs.getString("pc_property_details_id");

							AuditDetails accountAuditDetails = AuditDetails.builder()
									.createdBy(rs.getString("pc_created_by")).createdTime(rs.getLong("pc_created_time"))
									.lastModifiedBy(rs.getString("pc_last_modified_by"))
									.lastModifiedTime(rs.getLong("pc_last_modified_time")).build();

							paymentConfig = PaymentConfig.builder().id(rs.getString("pc_id"))
									.tenantId(rs.getString("pc_tenant_id")).propertyDetailsId(pcPropertyDetailsId)
									.isIntrestApplicable(rs.getBoolean("pc_is_intrest_applicable"))
									.dueDateOfPayment(rs.getLong("pc_due_date_of_payment"))
									.noOfMonths(rs.getLong("pc_no_of_months"))
									.rateOfInterest(rs.getBigDecimal("pc_rate_of_interest"))
									.securityAmount(rs.getBigDecimal("pc_security_amount"))
									.totalAmount(rs.getBigDecimal("pc_total_amount"))
									.isGroundRent(rs.getBoolean("pc_is_ground_rent"))
									.groundRentGenerationType(rs.getString("pc_ground_rent_generation_type"))
									.groundRentGenerateDemand(rs.getLong("pc_ground_rent_generate_demand"))
									.groundRentAdvanceRent(rs.getBigDecimal("pc_ground_rent_advance_rent"))
									.groundRentBillStartDate(rs.getLong("pc_ground_rent_bill_start_date"))
									.groundRentAdvanceRentDate(rs.getLong("pc_ground_rent_advance_rent_date"))
									.auditDetails(accountAuditDetails).build();
						}
					}

					final PropertyDetails propertyDetails = PropertyDetails.builder().id(propertyDetailId)
							.propertyId(rs.getString("pdproperty_id")).propertyType(rs.getString("pdproperty_type"))
							.branchType(rs.getString("branch_type")).tenantId(tenantId)
							.typeOfAllocation(rs.getString("type_of_allocation"))
							.modeOfAuction(rs.getString("mode_of_auction")).schemeName(rs.getString("scheme_name"))
							.areaSqft(rs.getInt("area_sqft")).dateOfAuction(rs.getLong("date_of_auction"))
							.ratePerSqft(rs.getBigDecimal("rate_per_sqft")).lastNocDate(rs.getLong("last_noc_date"))
							.serviceCategory(rs.getString("service_category"))
							.isPropertyActive(rs.getBoolean("is_property_active")).tradeType(rs.getString("trade_type"))
							.companyName(rs.getString("company_name")).companyAddress(rs.getString("company_address"))
							.companyRegistrationNumber(rs.getString("company_registration_number"))
							.companyType(rs.getString("company_type")).emdAmount(rs.getBigDecimal("emd_amount"))
							.emdDate(rs.getLong("emd_date")).decreeDate(rs.getLong("decree_date"))
							.courtDetails(rs.getString("court_details")).civilTitledAs(rs.getString("civil_titled_as"))
							.companyRegistrationDate(rs.getLong("company_registration_date"))
							.entityType(rs.getString("entity_type")).houseNumber(rs.getString("house_number"))
							.village(rs.getString("village")).mohalla(rs.getString("mohalla"))
							.propertyRegisteredTo(rs.getString("property_registered_to"))
							.companyOrFirm(rs.getString("company_or_firm")).interestRate(rs.getDouble("interest_rate"))
							.mmDemandStartYear(rs.getInt("mm_demand_start_year"))
							.mmDemandStartMonth(rs.getInt("mm_demand_start_month")).paymentConfig(paymentConfig)
							.auditDetails(pdAuditdetails).build();

					currentProperty = Property.builder().id(propertyId).fileNumber(rs.getString("file_number"))
							.tenantId(tenantId).category(rs.getString("category"))
							.subCategory(rs.getString("sub_category")).sectorNumber(rs.getString("sector_number"))
							.siteNumber(rs.getString("site_number")).state(rs.getString("pstate"))
							.propertyMasterOrAllotmentOfSite(rs.getString("property_master_or_allotment_of_site"))
							.isCancelationOfSite(rs.getBoolean("is_cancelation_of_site"))
							.action(rs.getString("paction")).propertyDetails(propertyDetails).auditDetails(auditdetails)
							.build();

					propertyMap.put(propertyId, currentProperty);
				}
			}

			addChildrenToProperty(rs, currentProperty, propertyMap);
		}

		return new ArrayList<>(propertyMap.values());

	}

	private void addChildrenToProperty(final ResultSet rs, final Property property,
			final LinkedHashMap<String, Property> propertyMap) throws SQLException {

		if (hasColumn(rs, "oid")) {
			final String ownerId = rs.getString("oid");
			final String ownerDetailId = rs.getString("odid");
			final String OwnerPropertyDetailId = rs.getString("oproperty_details_id");

			if (ownerId != null) {

				final AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("ocreated_by"))
						.createdTime(rs.getLong("ocreated_time")).lastModifiedBy(rs.getString("omodified_by"))
						.lastModifiedTime(rs.getLong("omodified_time")).build();

				final OwnerDetails ownerDetails = OwnerDetails.builder().id(ownerDetailId)
						.ownerId(rs.getString("odowner_id")).ownerName(rs.getString("odowner_name"))
						.tenantId(rs.getString("otenantid")).guardianName(rs.getString("guardian_name"))
						.guardianRelation(rs.getString("guardian_relation")).mobileNumber(rs.getString("mobile_number"))
						.allotmentNumber(rs.getString("allotment_number"))
						.dateOfAllotment(rs.getLong("date_of_allotment")).possesionDate(rs.getLong("possesion_date"))
						.isApproved(rs.getBoolean("is_approved")).isCurrentOwner(rs.getBoolean("is_current_owner"))
						.isMasterEntry(rs.getBoolean("is_master_entry")).address(rs.getString("address"))
						.isDirector(rs.getBoolean("is_director")).auditDetails(auditdetails).build();

				final Owner owners = Owner.builder().id(ownerId).propertyDetailsId(OwnerPropertyDetailId)
						.tenantId(rs.getString("otenantid")).serialNumber(rs.getString("oserial_number"))
						.share(rs.getDouble("oshare")).cpNumber(rs.getString("ocp_number"))
						.state(rs.getString("ostate")).action(rs.getString("oaction"))
						.ownershipType(rs.getString("ownership_type")).ownerDetails(ownerDetails)
						.auditDetails(auditdetails).build();

				if (hasColumn(rs, "pid")) {
					property.getPropertyDetails().addOwnerItem(owners);
				} else {
					final Property property2 = new Property();
					final PropertyDetails propertyDetails = new PropertyDetails();
					propertyDetails.addOwnerItem(owners);
					property2.setPropertyDetails(propertyDetails);

					propertyMap.put(ownerId, property2);
				}

			}
		}

		if (hasColumn(rs, "docid")) {
			final String docOwnerDetailId = rs.getString("docreference_id");
			final List<Owner> owners = property.getPropertyDetails().getOwners();
			if (!CollectionUtils.isEmpty(owners)) {
				owners.forEach(owner -> {
					try {
						if (rs.getString("docid") != null && rs.getBoolean("docis_active")
								&& docOwnerDetailId.equals(owner.getOwnerDetails().getId())) {

							final AuditDetails docAuditdetails = AuditDetails.builder()
									.createdBy(rs.getString("dcreated_by")).createdTime(rs.getLong("dcreated_time"))
									.lastModifiedBy(rs.getString("dmodified_by"))
									.lastModifiedTime(rs.getLong("dmodified_time")).build();

							final Document ownerDocument = Document.builder().id(rs.getString("docid"))
									.referenceId(rs.getString("docowner_details_id"))
									.tenantId(rs.getString("doctenantid")).isActive(rs.getBoolean("docis_active"))
									.documentType(rs.getString("document_type"))
									.fileStoreId(rs.getString("file_store_id"))
									.propertyId(rs.getString("docproperty_id")).auditDetails(docAuditdetails).build();
							owner.getOwnerDetails().addOwnerDocumentsItem(ownerDocument);
						}
					} catch (final SQLException e) {
						e.printStackTrace();
					}
				});
			}
		}

		if (hasColumn(rs, "ccid")) {
			final String courtCasePropertDetailId = rs.getString("ccproperty_details_id");

			try {

				if (courtCasePropertDetailId != null
						&& courtCasePropertDetailId.equals(property.getPropertyDetails().getId())) {

					final AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("cccreated_by"))
							.createdTime(rs.getLong("cccreated_time")).lastModifiedBy(rs.getString("ccmodified_by"))
							.lastModifiedTime(rs.getLong("ccmodified_time")).build();

					final CourtCase courtCase = CourtCase.builder().id(rs.getString("ccid"))
							.propertyDetailsId(courtCasePropertDetailId).tenantId(rs.getString("cctenantid"))
							.estateOfficerCourt(rs.getString("ccestate_officer_court"))
							.commissionersCourt(rs.getString("cccommissioners_court"))
							.chiefAdministartorsCourt(rs.getString("ccchief_administartors_court"))
							.advisorToAdminCourt(rs.getString("ccadvisor_to_admin_court"))
							.honorableDistrictCourt(rs.getString("cchonorable_district_court"))
							.honorableHighCourt(rs.getString("cchonorable_high_court"))
							.honorableSupremeCourt(rs.getString("cchonorable_supreme_court")).auditDetails(auditdetails)
							.build();

					property.getPropertyDetails().addCourtCaseItem(courtCase);

				}
			} catch (final SQLException e) {
				e.printStackTrace();
			}

		}

		if (null != property.getPropertyDetails().getPaymentConfig()) {
			if (hasColumn(rs, "pci_id")) {

				String paymentConfigItemsId = rs.getString("pci_id");
				if (null != paymentConfigItemsId) {
					PaymentConfigItems paymentConfigItems = PaymentConfigItems.builder().id(rs.getString("pci_id"))
							.tenantId(rs.getString("pci_tenant_id")).paymentConfigId("pci_payment_config_id")
							.groundRentAmount(rs.getBigDecimal("pci_ground_rent_amount"))
							.groundRentStartMonth(rs.getLong("pci_ground_rent_start_month"))
							.groundRentEndMonth(rs.getLong("pci_ground_rent_end_month")).build();

					property.getPropertyDetails().getPaymentConfig().addPaymentConfigItem(paymentConfigItems);
				}
			}

			if (hasColumn(rs, "paci_id")) {

				String permiumAmountId = rs.getString("paci_id");
				if (null != permiumAmountId) {
					PremiumAmountConfigItems premiumAmountConfigItems = PremiumAmountConfigItems.builder()
							.id(rs.getString("paci_id")).tenantId(rs.getString("paci_tenant_id"))
							.paymentConfigId("paci_payment_config_id")
							.premiumAmount(rs.getBigDecimal("paci_premium_amount"))
							.premiumAmountDate(rs.getLong("paci_premiumamountdate")).build();

					property.getPropertyDetails().getPaymentConfig()
							.addPremiumAmountConfigItem(premiumAmountConfigItems);
				}
			}
		}
	}

	public static boolean hasColumn(final ResultSet rs, final String columnName) throws SQLException {
		final ResultSetMetaData rsmd = rs.getMetaData();
		final int columns = rsmd.getColumnCount();
		for (int x = 1; x <= columns; x++) {
			if (columnName.equals(rsmd.getColumnName(x))) {
				return true;
			}
		}
		return false;
	}

}
