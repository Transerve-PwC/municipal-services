package org.egov.ps.web.contracts;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.ps.model.AuctionBidder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuctionSearhResponse {

	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;

	@JsonProperty("Auctions")
	@Valid
	private List<AuctionBidder> auctions;

	public AuctionSearhResponse addAuctions(AuctionBidder auction) {
		if (this.auctions == null) {
			this.auctions = new ArrayList<>();
		}
		this.auctions.add(auction);
		return this;
	}
}
