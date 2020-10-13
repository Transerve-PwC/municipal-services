package org.egov.assets.web.controller;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.assets.model.PDFRequest;
import org.egov.assets.model.PDFResponse;
import org.egov.assets.model.PurchaseOrderRequest;
import org.egov.assets.model.PurchaseOrderResponse;
import org.egov.assets.model.PurchaseOrderSearch;
import org.egov.assets.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/purchaseorders")
public class PurchaseordersApiController {

	@Autowired
	private PurchaseOrderService purchaseOrderService;

	@PostMapping(value = "/_create", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<PurchaseOrderResponse> purchaseordersCreatePost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody PurchaseOrderRequest purchaseOrderRequest) {
		PurchaseOrderResponse response = purchaseOrderService.create(purchaseOrderRequest, tenantId);
		return new ResponseEntity(response, HttpStatus.OK);
	}

	@PostMapping(value = "/_search", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<PurchaseOrderResponse> purchaseordersSearchPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody org.egov.common.contract.request.RequestInfo requestInfo,
			@Size(max = 50) @RequestParam(value = "ids", required = false) List<String> ids,
			@RequestParam(value = "store", required = false) String store,
			@RequestParam(value = "purchaseOrderNumber", required = false) String purchaseOrderNumber,
			@RequestParam(value = "purchaseOrderDate", required = false) Long purchaseOrderDate,
			@RequestParam(value = "rateType", required = false) String rateType,
			@RequestParam(value = "supplierCode", required = false) String supplierCode,
			@RequestParam(value = "searchPoAdvReq", required = false) Boolean searchPoAdvReq,
			@RequestParam(value = "status", required = false) String status,
			@Min(0) @Max(100) @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
			@RequestParam(value = "pageNumber", required = false, defaultValue = "1") Integer pageNumber,
			@RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy) {

		PurchaseOrderSearch purchaseOrderSearch = new PurchaseOrderSearch();
		purchaseOrderSearch.setIds(ids);
		purchaseOrderSearch.setStore(store);
		purchaseOrderSearch.setTenantId(tenantId);
		purchaseOrderSearch.setPurchaseOrderNumber(purchaseOrderNumber);
		purchaseOrderSearch.setPurchaseOrderDate(purchaseOrderDate);
		purchaseOrderSearch.setRateType(rateType);
		purchaseOrderSearch.setSupplier(supplierCode);
		purchaseOrderSearch.setSearchPoAdvReq(searchPoAdvReq);
		purchaseOrderSearch.setStatus(status);
		purchaseOrderSearch.setPageSize(pageSize);
		purchaseOrderSearch.setPageNumber(pageNumber);

		return new ResponseEntity<>(purchaseOrderService.search(purchaseOrderSearch), HttpStatus.OK);
	}

	@PostMapping(value = "/_print", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<PDFResponse> purchaseordersSearchPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody PDFRequest pdfRequest,
			@RequestParam(value = "purchaseOrderNumber", required = false) String purchaseOrderNumber) {

		PurchaseOrderSearch purchaseOrderSearch = new PurchaseOrderSearch();
		purchaseOrderSearch.setTenantId(tenantId);
		purchaseOrderSearch.setPurchaseOrderNumber(purchaseOrderNumber);
		return new ResponseEntity<>(purchaseOrderService.printPdf(purchaseOrderSearch, pdfRequest.getRequestInfo()),
				HttpStatus.OK);
	}

	@PostMapping(value = "/_update", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<PurchaseOrderResponse> purchaseordersUpdatePost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody PurchaseOrderRequest purchaseOrderRequest) {
		PurchaseOrderResponse response = purchaseOrderService.update(purchaseOrderRequest, tenantId);
		return new ResponseEntity(response, HttpStatus.OK);
	}

	@PostMapping(value = "/_preparepofromindents", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<PurchaseOrderResponse> purchaseordersPreparepofromindentsPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@RequestBody PurchaseOrderRequest purchaseOrderRequest) {
		PurchaseOrderResponse response = purchaseOrderService.preparePoFromIndents(purchaseOrderRequest, tenantId);
		return new ResponseEntity(response, HttpStatus.OK);
	}

	@PostMapping(value = "/_searchpoforadvancerequisition", produces = { "application/json" }, consumes = {
			"application/json" })
	public ResponseEntity<PurchaseOrderResponse> purchaseordersSearchpoforadvancerequisitionPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId) {
		PurchaseOrderResponse response = purchaseOrderService.getPOsForAdvanceRequisition(tenantId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PostMapping(value = "/_updateStatus", produces = { "application/json" }, consumes = { "application/json" })
	public ResponseEntity<PurchaseOrderResponse> purchaseordersUpdateStatusPost(
			@NotNull @RequestParam(value = "tenantId", required = true) String tenantId,
			@Valid @RequestBody PurchaseOrderRequest purchaseOrderRequest) {
		PurchaseOrderResponse response = purchaseOrderService.updateStatus(purchaseOrderRequest, tenantId);
		return new ResponseEntity(response, HttpStatus.OK);
	}

}
