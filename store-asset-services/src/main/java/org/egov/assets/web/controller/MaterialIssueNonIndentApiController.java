package org.egov.assets.web.controller;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.assets.model.MaterialIssueRequest;
import org.egov.assets.model.MaterialIssueResponse;
import org.egov.assets.model.MaterialIssueSearchContract;
import org.egov.assets.model.PDFRequest;
import org.egov.assets.model.PDFResponse;
import org.egov.assets.service.NonIndentMaterialIssueService;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/materialissues-ni")
public class MaterialIssueNonIndentApiController {

	@Autowired
	private NonIndentMaterialIssueService nonIndentMaterialIssueService;

	@PostMapping(value = "/_create", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialIssueResponse> materialissuesNonIndentCreatePost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody MaterialIssueRequest nonIndentIssueRequest) {
		MaterialIssueResponse materialIssueResponse = nonIndentMaterialIssueService.create(nonIndentIssueRequest,tenantId);
		return new ResponseEntity(materialIssueResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_search", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialIssueResponse> materialissuesNonIndentSearchPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody RequestInfo requestInfo,
			@Size(max = 50) @RequestParam(value = "ids", required = false) List<String> ids,
			@RequestParam(value = "fromStore", required = false) String fromStore,
			@RequestParam(value = "toStore", required = false) String toStore,
			@RequestParam(value = "issueNoteNumber", required = false) String issueNoteNumber,
			@RequestParam(value = "issueDate", required = false) Long issueDate,
			@RequestParam(value = "materialIssueStatus", required = false) String materialIssueStatus,
			@RequestParam(value = "issuePurpose", required = false) String issuePurpose,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "totalIssueValue", required = false) BigDecimal totalIssueValue,
			@Min(0) @Max(100) @RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@RequestParam(value = "sortBy", required = false) String sortBy,
			@RequestParam(value = "purpose", required = false) String purpose) {
		MaterialIssueSearchContract searchContract = new MaterialIssueSearchContract(tenantId, ids, fromStore, toStore,
				issueNoteNumber, issuePurpose, issueDate, null, materialIssueStatus, description, totalIssueValue, null,
				pageNumber, sortBy, pageSize, purpose,null,null);
		MaterialIssueResponse materialIssueResponse = nonIndentMaterialIssueService.search(searchContract);
		return new ResponseEntity(materialIssueResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_print", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<PDFResponse> materialissuesNonIndentSearchPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody PDFRequest pdfRequest,
			@RequestParam(value = "issueNoteNumber", required = false) String issueNoteNumber) {
		MaterialIssueSearchContract searchContract = new MaterialIssueSearchContract(tenantId, null, null, null,
				issueNoteNumber, null, null, null, null, null, null, null, null, null, null, null, null, null);
		PDFResponse materialIssueResponse = nonIndentMaterialIssueService.printPdf(searchContract,
				pdfRequest.getRequestInfo());
		return new ResponseEntity(materialIssueResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_update", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialIssueResponse> materialissuesNonIndentUpdatePost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody MaterialIssueRequest nonIndentIssueRequest) {
		MaterialIssueResponse materialIssueResponse = nonIndentMaterialIssueService.update(nonIndentIssueRequest,
				tenantId);
		return new ResponseEntity(materialIssueResponse, HttpStatus.OK);

	}

	@PostMapping(value = "/_updateStatus", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialIssueResponse> materialIssueUpdateStatusPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody MaterialIssueRequest indentIssueRequest) {
		MaterialIssueResponse materialIssueResponse = nonIndentMaterialIssueService.updateStatus(indentIssueRequest);
		return new ResponseEntity(materialIssueResponse, HttpStatus.OK);
	}

}
