package org.egov.ps.service.calculation;

import java.util.List;

import org.egov.ps.web.contracts.ManiMajraDemand;
import org.egov.ps.web.contracts.ManiMajraPayment;
import org.egov.ps.web.contracts.ManiMajraRentCollection;

public interface IManiMajraRentCollectionService {

	public List<ManiMajraRentCollection> settle(final List<ManiMajraDemand> demands,
			final List<ManiMajraPayment> payments, boolean isMonthly);

}
