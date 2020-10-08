package org.egov.assets.web.controller;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.assets.model.IndentRequest;
import org.egov.assets.model.IndentResponse;
import org.egov.assets.model.MaterialReceipt;
import org.egov.assets.model.MaterialReceiptSearch;
import org.egov.assets.model.OpeningBalanceRequest;
import org.egov.assets.model.OpeningBalanceResponse;
import org.egov.assets.model.PDFRequest;
import org.egov.assets.model.PDFResponse;
import org.egov.assets.model.StatusNums.StatusEnum;
import org.egov.assets.service.OpeningBalanceService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/openingbalance")
public class OpeningbalanceApiController {

	private static final Logger log = LoggerFactory.getLogger(OpeningbalanceApiController.class);

	@Autowired
	private OpeningBalanceService openingBalanceService;

	@PostMapping(value = "/_create", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<OpeningBalanceResponse> openingbalanceCreatePost(
			@Valid @RequestParam(value = "tenantId", required = true) String tenantId,
			@RequestBody OpeningBalanceRequest openingBalance) {
		List<MaterialReceipt> openbal = openingBalanceService.create(openingBalance, tenantId);
		OpeningBalanceResponse materialResponse = buildOpenBalanceResponse(openbal, openingBalance.getRequestInfo());
		return new ResponseEntity<>(materialResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_update", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<OpeningBalanceResponse> openingbalanceUpdatePost(
			@Valid @RequestParam(value = "tenantId", required = true) String tenantId,
			@RequestBody OpeningBalanceRequest openingBalanace) {
		List<MaterialReceipt> openbal = openingBalanceService.update(openingBalanace, tenantId);
		OpeningBalanceResponse materialResponse = buildOpenBalanceResponse(openbal, openingBalanace.getRequestInfo());
		return new ResponseEntity<>(materialResponse, HttpStatus.OK);

	}

	@PostMapping(value = "/_search", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<OpeningBalanceResponse> openingbalanceSearchPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@NotNull @RequestParam(value = "ids", required = false) String ids,
			@RequestParam(value = "receiptType", required = false) String receiptType,
			@RequestParam(value = "storeName", required = false) String storeName,
			@Valid @RequestBody RequestInfo requestInfo,
			@RequestParam(value = "financialYear", required = false) String financialYear,
			@RequestParam(value = "mrnNumber", required = false) String mrnNumber,
			@Min(0) @Max(100) @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
			@RequestParam(value = "pageNumber", required = false, defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy) {
		MaterialReceiptSearch receiptSearch = MaterialReceiptSearch.builder()
				.ids(null != ids ? Arrays.asList(ids) : null).tenantId(tenantId).financialYear(financialYear)
				.receivingStore(storeName)
				// .mrnNumber(Collections.singletonList(receiptNumber))
				.mrnNumber(null != mrnNumber ? Arrays.asList(mrnNumber) : null)
				.receiptType(null != receiptType ? Arrays.asList(receiptType) : null).pageNumber(pageNumber)
				.pageSize(pageSize).build();

		return new ResponseEntity<>(openingBalanceService.search(receiptSearch), HttpStatus.OK);
	}

	@PostMapping(value = "/_report", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<PDFResponse> openingbalanceReportPdfPrintPost(@Valid @RequestBody PDFRequest pdfRequest,
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@RequestParam(value = "storecode", required = true) String storecode,
			@RequestParam(value = "financialyear", required = true) String financialyear,
			@RequestParam(value = "isprint", required = true) boolean forprint) {
		MaterialReceiptSearch materialReceiptSearch = MaterialReceiptSearch.builder().tenantId(tenantId)
				.receiptType(Arrays.asList("OPENING BALANCE")).forprint(forprint).receivingStore(storecode)
				.forprint(forprint).financialYear(financialyear).build();
		PDFResponse response = openingBalanceService.printOpeningBalanceReportPdf(materialReceiptSearch,
				pdfRequest.getRequestInfo());
		return new ResponseEntity(response, HttpStatus.OK);
	}
	
	@PostMapping(value = "/_updateStatus", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<OpeningBalanceResponse> openingBalanceUpdateStatusPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody OpeningBalanceRequest openingBalance) {
		OpeningBalanceResponse response = openingBalanceService.updateStatus(openingBalance);
		return new ResponseEntity(response, HttpStatus.OK);
	}


	private OpeningBalanceResponse buildOpenBalanceResponse(List<MaterialReceipt> material, RequestInfo requestInfo) {
		return OpeningBalanceResponse.builder().responseInfo(getResponseInfo(requestInfo)).materialReceipt(material)
				.build();
	}

	private ResponseInfo getResponseInfo(RequestInfo requestInfo) {
		return ResponseInfo.builder().apiId(requestInfo.getApiId()).ver(requestInfo.getVer())
				.resMsgId(requestInfo.getMsgId()).resMsgId("placeholder").status(StatusEnum.SUCCESSFUL.toString())
				.build();
	}
}
