package org.egov.assets.web.controller;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.assets.model.MaterialReceiptRequest;
import org.egov.assets.model.MaterialReceiptResponse;
import org.egov.assets.model.MaterialReceiptSearch;
import org.egov.assets.model.PDFRequest;
import org.egov.assets.model.PDFResponse;
import org.egov.assets.service.MiscellaneousReceiptNoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/miscellaneousreceiptnotes")
public class MiscellaneousreceiptnotesApiController {

	private MiscellaneousReceiptNoteService miscellaneousReceiptNoteService;

	public MiscellaneousreceiptnotesApiController(MiscellaneousReceiptNoteService miscellaneousReceiptNoteService) {
		this.miscellaneousReceiptNoteService = miscellaneousReceiptNoteService;
	}

	@PostMapping(value = "/_create", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialReceiptResponse> miscellaneousreceiptnotesCreatePost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody MaterialReceiptRequest materialReceipt) {

		MaterialReceiptResponse materialReceiptResponse = miscellaneousReceiptNoteService.create(materialReceipt,
				tenantId);
		return new ResponseEntity<>(materialReceiptResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_search", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialReceiptResponse> miscellaneousreceiptnotesSearchPost(
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
			@Min(0) @Max(100) @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
			@RequestParam(value = "pageNumber", required = false, defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy) {
		MaterialReceiptSearch materialReceiptSearch = MaterialReceiptSearch.builder().tenantId(tenantId)
				.ids(null != ids ? Arrays.asList(ids) : null).mrnNumber(mrnNumber).receiptType(receiptType)
				.mrnStatus(null != mrnStatus ? asList(mrnStatus) : null).receivingStore(receivingStore)
				.supplierCode(supplierCode).receiptDate(receiptDateFrom).receiptDate(receiptDateTo)
				.pageNumber(pageNumber).pageSize(pageSize).build();
		MaterialReceiptResponse materialReceiptResponse = miscellaneousReceiptNoteService.search(materialReceiptSearch);
		return new ResponseEntity<>(materialReceiptResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_print", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<PDFResponse> miscellaneousreceiptnotesPrintPost(@Valid @RequestBody PDFRequest pdfRequest,
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@NotNull @RequestParam(value = "ids", required = false) String ids,
			@Size(max = 100) @RequestParam(value = "mrnNumber", required = true) String mrnNumber) {
		MaterialReceiptSearch materialReceiptSearch = MaterialReceiptSearch.builder().tenantId(tenantId)
				.mrnNumber(Arrays.asList(mrnNumber)).build();
		PDFResponse materialReceiptResponse = miscellaneousReceiptNoteService.printPdf(materialReceiptSearch,
				pdfRequest.getRequestInfo());
		return new ResponseEntity<>(materialReceiptResponse, HttpStatus.OK);
	}

	@PostMapping(value = "/_update", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialReceiptResponse> miscellaneousreceiptnotesUpdatePost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody MaterialReceiptRequest materialReceipt) {
		MaterialReceiptResponse materialReceiptResponse = miscellaneousReceiptNoteService.update(materialReceipt,
				tenantId);
		return new ResponseEntity<>(materialReceiptResponse, HttpStatus.OK);
	}
	@PostMapping(value = "/_updateStatus", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<MaterialReceiptResponse> miscellaneousnotesUpdateStatusPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody MaterialReceiptRequest materialReceipt) {
		MaterialReceiptResponse materialReceiptResponse = miscellaneousReceiptNoteService.updateStatus(materialReceipt, tenantId);
		return new ResponseEntity<>(materialReceiptResponse, HttpStatus.OK);
	}

}
