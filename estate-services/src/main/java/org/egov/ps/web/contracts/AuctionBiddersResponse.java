package org.egov.ps.web.contracts;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.ps.model.AuctionBidder;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuctionBiddersResponse {

	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;

	@JsonProperty("Bidders")
	@Valid
	private List<AuctionBidder> bidders;

	public AuctionBiddersResponse addAuctions(AuctionBidder bidder) {
		if (this.bidders == null) {
			this.bidders = new ArrayList<>();
		}
		this.bidders.add(bidder);
		return this;
	}

}
