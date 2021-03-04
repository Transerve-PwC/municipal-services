package org.egov.swservice.model;

import java.util.Objects;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * SewerageConnection
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-03-13T11:29:47.358+05:30[Asia/Kolkata]")
public class SewerageConnection extends Connection {
	@JsonProperty("proposedWaterClosets")
	private Integer proposedWaterClosets = null;

	@JsonProperty("proposedToilets")
	private Integer proposedToilets = null;

	@JsonProperty("noOfWaterClosets")
	private Integer noOfWaterClosets = null;

	@JsonProperty("noOfToilets")
	private Integer noOfToilets = null;
	

	@JsonProperty("div")
	private String div = null;

	@JsonProperty("subdiv")
	private String subdiv = null;

	@JsonProperty("ledgerNo")
	private String ledgerNo = null;

	@JsonProperty("ccCode")
	private String ccCode = null;

	@JsonProperty("meterCount")
	private String meterCount = null;

	@JsonProperty("meterRentCode")
	private String meterRentCode = null;

	@JsonProperty("mfrCode")
	private String mfrCode = null;

	@JsonProperty("meterDigits")
	private String meterDigits = null;

	@JsonProperty("meterUnit")
	private String meterUnit = null;

	@JsonProperty("sanctionedCapacity")
	private String sanctionedCapacity = null;

	@JsonProperty("ledgerGroup")
	private String ledgerGroup = null;
	
	@JsonProperty("inWorkflow")
	private Boolean inWorkflow = false;
	
	@JsonProperty("activityType")
	private String activityType = null;

	@JsonProperty("contractValue")
	private String contractValue = null;

	@JsonProperty("billGroup")
	private String billGroup = null;

	public String getDiv() {
		return div;
	}

	public void setDiv(String div) {
		this.div = div;
	}

	public String getSubdiv() {
		return subdiv;
	}

	public void setSubdiv(String subdiv) {
		this.subdiv = subdiv;
	}

	public String getLedgerNo() {
		return ledgerNo;
	}

	public void setLedgerNo(String ledgerNo) {
		this.ledgerNo = ledgerNo;
	}

	public String getCcCode() {
		return ccCode;
	}

	public void setCcCode(String ccCode) {
		this.ccCode = ccCode;
	}

	public String getMeterCount() {
		return meterCount;
	}

	public void setMeterCount(String meterCount) {
		this.meterCount = meterCount;
	}

	public String getMeterRentCode() {
		return meterRentCode;
	}

	public void setMeterRentCode(String meterRentCode) {
		this.meterRentCode = meterRentCode;
	}

	public String getMfrCode() {
		return mfrCode;
	}

	public void setMfrCode(String mfrCode) {
		this.mfrCode = mfrCode;
	}

	public String getMeterDigits() {
		return meterDigits;
	}

	public void setMeterDigits(String meterDigits) {
		this.meterDigits = meterDigits;
	}

	public String getMeterUnit() {
		return meterUnit;
	}

	public void setMeterUnit(String meterUnit) {
		this.meterUnit = meterUnit;
	}

	public String getSanctionedCapacity() {
		return sanctionedCapacity;
	}

	public void setSanctionedCapacity(String sanctionedCapacity) {
		this.sanctionedCapacity = sanctionedCapacity;
	}

	public String getLedgerGroup() {
		return ledgerGroup;
	}

	public void setLedgerGroup(String ledgerGroup) {
		this.ledgerGroup = ledgerGroup;
	}

	public Boolean getInWorkflow() {
		return inWorkflow;
	}

	public void setInWorkflow(Boolean inWorkflow) {
		this.inWorkflow = inWorkflow;
	}

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	public String getContractValue() {
		return contractValue;
	}

	public void setContractValue(String contractValue) {
		this.contractValue = contractValue;
	}

	public String getBillGroup() {
		return billGroup;
	}

	public void setBillGroup(String billGroup) {
		this.billGroup = billGroup;
	}

	public SewerageConnection noOfWaterClosets(Integer noOfWaterClosets) {
		this.noOfWaterClosets = noOfWaterClosets;
		return this;
	}

	/**
	 * Get noOfWaterClosets
	 * 
	 * @return noOfWaterClosets
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public Integer getNoOfWaterClosets() {
		return noOfWaterClosets;
	}

	public void setNoOfWaterClosets(Integer noOfWaterClosets) {
		this.noOfWaterClosets = noOfWaterClosets;
	}

	public SewerageConnection proposedWaterClosets(Integer proposedWaterClosets) {
		this.proposedWaterClosets = proposedWaterClosets;
		return this;
	}

	/**
	 * Get proposedWaterClosets
	 * 
	 * @return proposedWaterClosets
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public Integer getProposedWaterClosets() {
		return proposedWaterClosets;
	}

	public void setProposedWaterClosets(Integer proposedWaterClosets) {
		this.proposedWaterClosets = proposedWaterClosets;
	}

	public SewerageConnection noOfToilets(Integer noOfToilets) {
		this.noOfToilets = noOfToilets;
		return this;
	}

	/**
	 * Get noOfToilets
	 * 
	 * @return noOfToilets
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public Integer getNoOfToilets() {
		return noOfToilets;
	}

	public void setNoOfToilets(Integer noOfToilets) {
		this.noOfToilets = noOfToilets;
	}

	public SewerageConnection proposedToilets(Integer proposedToilets) {
		this.proposedToilets = proposedToilets;
		return this;
	}

	/**
	 * Get proposedToilets
	 * 
	 * @return proposedToilets
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public Integer getProposedToilets() {
		return proposedToilets;
	}

	public void setProposedToilets(Integer proposedToilets) {
		this.proposedToilets = proposedToilets;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SewerageConnection sewerageConnection = (SewerageConnection) o;
		return Objects.equals(this.noOfWaterClosets, sewerageConnection.noOfWaterClosets)
				&& Objects.equals(this.proposedWaterClosets, sewerageConnection.proposedWaterClosets)
				&& Objects.equals(this.noOfToilets, sewerageConnection.noOfToilets)
				&& Objects.equals(this.proposedToilets, sewerageConnection.proposedToilets) && super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(noOfWaterClosets, proposedWaterClosets, noOfToilets, proposedToilets, super.hashCode());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class SewerageConnection {\n");
		sb.append("    ").append(toIndentedString(super.toString())).append("\n");
		sb.append("    noOfWaterClosets: ").append(toIndentedString(noOfWaterClosets)).append("\n");
		sb.append("    proposedWaterClosets: ").append(toIndentedString(proposedWaterClosets)).append("\n");
		sb.append("    noOfToilets: ").append(toIndentedString(noOfToilets)).append("\n");
		sb.append("    proposedToilets: ").append(toIndentedString(proposedToilets)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
