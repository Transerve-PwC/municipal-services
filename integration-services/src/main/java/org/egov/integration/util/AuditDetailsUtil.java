package org.egov.integration.util;


import java.util.Date;

import org.egov.common.contract.request.RequestInfo;
import org.egov.integration.common.CommonConstants;
import org.egov.integration.model.AuditDetails;
import org.springframework.stereotype.Repository;

@Repository
public class AuditDetailsUtil {

	public AuditDetails getAuditDetails(RequestInfo requestInfo, String action) {
		AuditDetails auditDetails = new AuditDetails();
		if (action.equalsIgnoreCase(CommonConstants.ACTION_CREATE)) {
			if (requestInfo.getUserInfo() != null && requestInfo.getUserInfo().getId() != null) {
				auditDetails.createdBy(requestInfo.getUserInfo().getId().toString());
				auditDetails.lastModifiedBy(requestInfo.getUserInfo().getId().toString());
			}
			auditDetails.createdTime(new Date().getTime());
			auditDetails.lastModifiedTime(new Date().getTime());
			return auditDetails;
		} else {
			if (requestInfo.getUserInfo() != null && requestInfo.getUserInfo().getId() != null) {
				auditDetails.lastModifiedBy(requestInfo.getUserInfo().getId().toString());
			}
			auditDetails.lastModifiedTime(new Date().getTime());
			return auditDetails;
		}
	}



}
