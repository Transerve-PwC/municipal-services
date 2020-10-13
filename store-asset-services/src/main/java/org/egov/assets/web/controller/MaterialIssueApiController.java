package org.egov.assets.web.controller;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.assets.model.MaterialIssue.IssueTypeEnum;
import org.egov.assets.model.MaterialIssueRequest;
import org.egov.assets.model.MaterialIssueResponse;
import org.egov.assets.model.MaterialIssueSearchContract;
import org.egov.assets.model.PDFRequest;
import org.egov.assets.model.PDFResponse;
import org.egov.assets.service.MaterialIssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/materialissues")
public class MaterialIssueApiController {

	@Autowired
	private MaterialIssueService materialIssueService;

	@PostMapping(value = "/_create", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialIssueResponse> materialIssueCreatePost(
			@NotNull @Valid @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody MaterialIssueRequest indentIssueRequest) {
		MaterialIssueResponse materialIssueResponse = materialIssueService.create(indentIssueRequest,
				IssueTypeEnum.INDENTISSUE.toString());
		return new ResponseEntity(materialIssueResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_search", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialIssueResponse> materialIssueSearchPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody org.egov.common.contract.request.RequestInfo requestInfo,
			@Size(max = 50) @RequestParam(value = "ids", required = false) List<String> ids,
			@RequestParam(value = "fromStore", required = false) String fromStore,
			@RequestParam(value = "toStore", required = false) String toStore,
			@RequestParam(value = "issueNoteNumber", required = false) String issueNoteNumber,
			@RequestParam(value = "issueDate", required = false) Long issueDate,
			@RequestParam(value = "issueFromDate", required = false) Long issueFromDate,
			@RequestParam(value = "issueToDate", required = false) Long issueToDate,
			@RequestParam(value = "materialIssueStatus", required = false) String materialIssueStatus,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "issuePurpose", required = false) String issuePurpose,
			@RequestParam(value = "totalIssueValue", required = false) BigDecimal totalIssueValue,
			@Min(0) @Max(100) @RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@RequestParam(value = "sortBy", required = false) String sortBy,
			@RequestParam(value = "purpose", required = false) String purpose) {

		MaterialIssueSearchContract searchContract = new MaterialIssueSearchContract(tenantId, ids, fromStore, toStore,
				issueNoteNumber, issuePurpose, issueDate, null, materialIssueStatus, description, totalIssueValue, null,
				pageNumber, sortBy, pageSize, purpose,issueFromDate,issueToDate);
		MaterialIssueResponse materialIssueResponse = materialIssueService.search(searchContract,
				IssueTypeEnum.INDENTISSUE.toString());
		return new ResponseEntity(materialIssueResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_print", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<PDFResponse> materialIssuePrintPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody PDFRequest pdfRequest,
			@RequestParam(value = "issueNoteNumber", required = false) String issueNoteNumber) {

		MaterialIssueSearchContract searchContract = new MaterialIssueSearchContract(tenantId, null, null, null,
				issueNoteNumber, null, null, null, null, null, null, null, null, null, null, null, null, null);
		PDFResponse pdfResponse = materialIssueService.printPdf(searchContract, IssueTypeEnum.INDENTISSUE.toString(),
				pdfRequest.getRequestInfo());
		return new ResponseEntity(pdfResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_update", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialIssueResponse> materialIssueUpdatePost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody MaterialIssueRequest indentIssueRequest) {
		MaterialIssueResponse materialIssueResponse = materialIssueService.update(indentIssueRequest, tenantId,
				IssueTypeEnum.INDENTISSUE.toString());
		return new ResponseEntity(materialIssueResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_preparemifromindents", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialIssueResponse> materiallIssuesPreparemiFromIndents(
			@NotNull @Valid @RequestParam(value = "tenantId", required = true) String tenantId,
			@RequestBody MaterialIssueRequest indentIssueRequest) {
		MaterialIssueResponse materialIssueResponse = materialIssueService.prepareMIFromIndents(indentIssueRequest,
				tenantId);
		return new ResponseEntity(materialIssueResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_updateStatus", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialIssueResponse> materialIssueUpdateStatusPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody MaterialIssueRequest indentIssueRequest) {
		MaterialIssueResponse materialIssueResponse = materialIssueService.updateStatus(indentIssueRequest);
		return new ResponseEntity(materialIssueResponse, HttpStatus.OK);
	}

}
