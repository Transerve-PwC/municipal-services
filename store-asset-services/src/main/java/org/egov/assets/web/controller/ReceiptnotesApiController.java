package org.egov.assets.web.controller;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.assets.model.MaterialBalanceRateResponse;
import org.egov.assets.model.MaterialReceiptRequest;
import org.egov.assets.model.MaterialReceiptResponse;
import org.egov.assets.model.MaterialReceiptSearch;
import org.egov.assets.model.PDFRequest;
import org.egov.assets.model.PDFResponse;
import org.egov.assets.service.ReceiptNoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/receiptnotes")
public class ReceiptnotesApiController {

	private ReceiptNoteService receiptNoteService;

	public ReceiptnotesApiController(ReceiptNoteService receiptNoteService) {
		this.receiptNoteService = receiptNoteService;
	}

	@PostMapping(value = "/_create", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialReceiptResponse> receiptnotesCreatePost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody MaterialReceiptRequest materialReceipt) {
		MaterialReceiptResponse materialReceiptResponse = receiptNoteService.create(materialReceipt, tenantId);
		return new ResponseEntity<>(materialReceiptResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_search", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialReceiptResponse> receiptnotesSearchPost(
			@Valid @RequestBody org.egov.common.contract.request.RequestInfo requestInfo,
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@NotNull @RequestParam(value = "ids", required = false) String ids,
			@Size(max = 100) @RequestParam(value = "mrnNumber", required = false) List<String> mrnNumber,
			@Size(max = 3) @RequestParam(value = "receiptType", required = false) List<String> receiptType,
			@RequestParam(value = "mrnStatus", required = false) String mrnStatus,
			@RequestParam(value = "receivingStore", required = false) String receivingStore,
			@RequestParam(value = "supplierCode", required = false) String supplierCode,
			@RequestParam(value = "receiptDateFrom", required = false) Long receiptDateFrom,
			@RequestParam(value = "receiptDateTo", required = false) Long receiptDateTo,
			@RequestParam(value = "supplierBillPaid", required = false) Boolean supplierBillPaid,
			@Min(0) @Max(100) @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
			@RequestParam(value = "pageNumber", required = false, defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy) {
		MaterialReceiptSearch materialReceiptSearch = MaterialReceiptSearch.builder().tenantId(tenantId)
				.mrnNumber(mrnNumber).receiptType(receiptType).mrnStatus(null != mrnStatus ? asList(mrnStatus) : null)
				.ids(null != ids ? Arrays.asList(ids) : null).receivingStore(receivingStore).supplierCode(supplierCode)
				.receiptDate(receiptDateFrom).receiptDate(receiptDateTo).supplierBillPaid(supplierBillPaid)
				.pageNumber(pageNumber).pageSize(pageSize).build();
		MaterialReceiptResponse materialReceiptResponse = receiptNoteService.search(materialReceiptSearch);
		return new ResponseEntity<>(materialReceiptResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_print", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<PDFResponse> receiptnotesPrintPost(@Valid @RequestBody PDFRequest pdfRequest,
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@RequestParam(value = "mrnNumber", required = false) String mrnNumber) {
		MaterialReceiptSearch materialReceiptSearch = MaterialReceiptSearch.builder().tenantId(tenantId)
				.mrnNumber(Arrays.asList(mrnNumber)).build();
		PDFResponse response = receiptNoteService.printPdf(materialReceiptSearch, pdfRequest.getRequestInfo());
		return new ResponseEntity(response, HttpStatus.OK);
	}

	@PostMapping(value = "/_inventoryreport", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<PDFResponse> receiptnotesInventoryReportPrintPost(@Valid @RequestBody PDFRequest pdfRequest,
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@RequestParam(value = "storecode", required = true) String storecode,
			@RequestParam(value = "material", required = true) String material,
			@RequestParam(value = "isprint", required = true) boolean forprint) {
		MaterialReceiptSearch materialReceiptSearch = MaterialReceiptSearch.builder().tenantId(tenantId)
				.forprint(forprint).receivingStore(storecode).materials(Arrays.asList(material)).forprint(forprint)
				.build();
		PDFResponse response = receiptNoteService.printInventoryReportPdf(materialReceiptSearch,
				pdfRequest.getRequestInfo());
		return new ResponseEntity(response, HttpStatus.OK);
	}

	@PostMapping(value = "/_balance", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialBalanceRateResponse> balanceAndRateSearchPost(
			@Valid @RequestBody org.egov.common.contract.request.RequestInfo requestInfo,
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Size(max = 100) @RequestParam(value = "material", required = false) List<String> materials,
			@RequestParam(value = "issueingStore", required = true) String issueingStore) {
		MaterialReceiptSearch materialReceiptSearch = MaterialReceiptSearch.builder().tenantId(tenantId)
				.materials(materials).issueingStore(issueingStore).build();
		MaterialBalanceRateResponse materialBalanceRateResponse = receiptNoteService
				.searchBalanceAndRate(materialReceiptSearch);
		return new ResponseEntity<>(materialBalanceRateResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_update", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialReceiptResponse> receiptnotesUpdatePost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody MaterialReceiptRequest materialReceipt) {
		MaterialReceiptResponse materialReceiptResponse = receiptNoteService.update(materialReceipt, tenantId);
		return new ResponseEntity<>(materialReceiptResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_updateStatus", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialReceiptResponse> receiptnotesUpdateStatusPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody MaterialReceiptRequest materialReceipt) {
		MaterialReceiptResponse materialReceiptResponse = receiptNoteService.updateStatus(materialReceipt, tenantId);
		return new ResponseEntity<>(materialReceiptResponse, HttpStatus.OK);
	}
}
