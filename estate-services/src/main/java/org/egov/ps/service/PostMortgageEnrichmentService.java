package org.egov.ps.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.model.Document;
import org.egov.ps.model.EstateDocumentList;
import org.egov.ps.model.MortgageDetails;
import org.egov.ps.model.Owner;
import org.egov.ps.model.Property;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.ApplicationRequest;
import org.egov.ps.web.contracts.AuditDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PostMortgageEnrichmentService {

	@Autowired
	Util util;

	@Autowired
	private MDMSService mdmsservice;

	public void postEnrichMortgageDetails(ApplicationRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		AuditDetails auditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);

		if (!CollectionUtils.isEmpty(request.getApplications())) {
			request.getApplications().forEach(application -> {
				if (null != application && null != application.getProperty()
						&& application.getState().equalsIgnoreCase(PSConstants.PM_APPROVED)
						&& application.getProperty().getId() != null
						&& application.getProperty().getPropertyDetails() != null
						&& application.getProperty().getPropertyDetails().getOwners() != null
						&& !application.getProperty().getPropertyDetails().getOwners().isEmpty()) {

					// set audit details..
					application.setAuditDetails(auditDetails);

					List<Owner> ownerList = application.getProperty().getPropertyDetails().getOwners();
					if (!CollectionUtils.isEmpty(ownerList)) {
						ownerList.forEach(owner -> {
							owner.setMortgageDetails(
									getMortgage(application.getProperty(), owner, request, owner.getId()));
							validateMortgageDetails(application.getProperty(), owner, request.getRequestInfo(),
									owner.getId());
						});
					}
				}
			});
		}
	}

	public void validateMortgageDetails(Property property, Owner owner, RequestInfo requestInfo, String id) {
		MortgageDetails mortgage = owner.getMortgageDetails();

		if (mortgage != null) {
			List<Map<String, Object>> fieldConfigurations = mdmsservice.getMortgageDocumentConfig("mortgage",
					requestInfo, "ch");

			// To Do :: write code to validate documents base on master json template.
			ObjectMapper mapper = new ObjectMapper();
			List<EstateDocumentList> mortgageTypeList = mapper.convertValue(fieldConfigurations,
					new TypeReference<List<EstateDocumentList>>() {
					});
			Map<String, String> errorMap = new HashMap<>();

			if (mortgage.getMortgageDocuments() != null && !mortgage.getMortgageDocuments().isEmpty()) {
				mortgage.getMortgageDocuments().stream().forEach(document -> {
					if (!mortgageTypeList
							.contains(EstateDocumentList.builder().code(document.getDocumentType()).build())) {
						errorMap.put("INVALID DOCUMENT",
								"Document is not valid for user : " + owner.getOwnerDetails().getOwnerName());
					}
				});
			}
		}
	}

	private MortgageDetails getMortgage(Property property, Owner owner, ApplicationRequest application,
			String gen_owner_id) {
		String gen_mortgage_id = UUID.randomUUID().toString();

		MortgageDetails mortgage = owner.getMortgageDetails();
		mortgage.setId(gen_mortgage_id);
		mortgage.setTenantId(property.getTenantId());
		mortgage.setOwnerId(gen_owner_id);

		List<Document> documentsMortgage = owner.getMortgageDetails().getMortgageDocuments();
		if (!CollectionUtils.isEmpty(documentsMortgage)) {
			AuditDetails docAuditDetails = util.getAuditDetails(application.getRequestInfo().getUserInfo().getUuid(),
					true);
			documentsMortgage.forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
				document.setTenantId(property.getTenantId());
				document.setReferenceId(property.getId());
				document.setPropertyId(property.getId());
				document.setAuditDetails(docAuditDetails);
			});
		}
		return mortgage;
	}
}
