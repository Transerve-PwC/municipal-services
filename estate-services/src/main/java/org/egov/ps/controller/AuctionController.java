package org.egov.ps.controller;

import java.io.FileNotFoundException;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.ps.model.AuctionBidder;
import org.egov.ps.model.ExcelSearchCriteria;
import org.egov.ps.service.AuctionService;
import org.egov.ps.util.ResponseInfoFactory;
import org.egov.ps.web.contracts.AuctionBiddersResponse;
import org.egov.ps.web.contracts.AuctionTransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auctions")
public class AuctionController {

	@Autowired
	private AuctionService auctionService;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	@PostMapping("/_parse")
	public ResponseEntity<AuctionBiddersResponse> create(@Valid @ModelAttribute ExcelSearchCriteria searchCriteria,
			@Valid @RequestBody AuctionTransactionRequest auctionTransactionRequest) throws FileNotFoundException {
		List<AuctionBidder> bidders = auctionService.saveAuctionWithProperty(searchCriteria, auctionTransactionRequest);
		ResponseInfo resInfo = responseInfoFactory
				.createResponseInfoFromRequestInfo(auctionTransactionRequest.getRequestInfo(), true);
		AuctionBiddersResponse response = AuctionBiddersResponse.builder().bidders(bidders).responseInfo(resInfo)
				.build();
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}
}
