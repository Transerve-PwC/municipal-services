package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.egov.ps.model.PaymentConfig;
import org.egov.ps.model.PaymentConfigItems;
import org.egov.ps.model.PremiumAmountConfigItems;
import org.egov.ps.web.contracts.AuditDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class PaymentConfigRowMapper implements ResultSetExtractor<PaymentConfig> {

	@Override
	public PaymentConfig extractData(ResultSet rs) throws SQLException, DataAccessException {
		PaymentConfig paymentConfig = null;
		while (rs.next()) {

			AuditDetails accountAuditDetails = AuditDetails.builder().createdBy(rs.getString("pc_created_by"))
					.createdTime(rs.getLong("pc_created_time")).lastModifiedBy(rs.getString("pc_last_modified_by"))
					.lastModifiedTime(rs.getLong("pc_last_modified_time")).build();

			paymentConfig = PaymentConfig.builder().id(rs.getString("pc_id")).tenantId(rs.getString("pc_tenant_id"))
					.propertyDetailsId(rs.getString("pc_property_details_id"))
					.isIntrestApplicable(rs.getBoolean("pc_is_intrest_applicable"))
					.dueDateOfPayment(rs.getLong("pc_due_date_of_payment")).noOfMonths(rs.getLong("pc_no_of_months"))
					.rateOfInterest(rs.getBigDecimal("pc_rate_of_interest"))
					.securityAmount(rs.getBigDecimal("pc_security_amount"))
					.totalAmount(rs.getBigDecimal("pc_total_amount")).isGroundRent(rs.getBoolean("pc_is_ground_rent"))
					.groundRentGenerationType(rs.getString("pc_ground_rent_generation_type"))
					.groundRentGenerateDemand(rs.getLong("pc_ground_rent_generate_demand"))
					.groundRentAdvanceRent(rs.getBigDecimal("pc_ground_rent_advance_rent"))
					.groundRentBillStartDate(rs.getLong("pc_ground_rent_bill_start_date"))
					.groundRentAdvanceRentDate(rs.getLong("pc_ground_rent_advance_rent_date"))
					.auditDetails(accountAuditDetails).build();

			addChildrenToPaymentConfig(rs, paymentConfig);
		}
		return paymentConfig;
	}

	private void addChildrenToPaymentConfig(ResultSet rs, PaymentConfig paymentConfig) {

		try {
			PaymentConfigItems paymentConfigItems = PaymentConfigItems.builder().id(rs.getString("pci_id"))
					.tenantId(rs.getString("pci_tenant_id")).paymentConfigId(rs.getString("pci_payment_config_id"))
					.groundRentAmount(rs.getBigDecimal("pci_ground_rent_amount"))
					.groundRentStartMonth(rs.getLong("pci_ground_rent_start_month"))
					.groundRentEndMonth(rs.getLong("pci_ground_rent_end_month")).build();

			paymentConfig.addPaymentConfigItem(paymentConfigItems);

			if (rs.getString("paci_id") != null) {
				PremiumAmountConfigItems premiumAmountConfigItems = PremiumAmountConfigItems.builder()
						.id(rs.getString("paci_id")).tenantId(rs.getString("paci_tenant_id"))
						.paymentConfigId(rs.getString("paci_payment_config_id"))
						.premiumAmount(rs.getBigDecimal("paci_premium_amount"))
						.premiumAmountDate(rs.getLong("paci_premiumamountdate")).build();

				paymentConfig.addPremiumAmountConfigItem(premiumAmountConfigItems);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
