package org.egov.waterconnection.model;

import java.util.Objects;

import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * WaterConnection
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-03-11T12:10:14.583+05:30[Asia/Kolkata]")
public class WaterConnection extends Connection {

	

	@JsonProperty("waterSource")
	private String waterSource = null;

	@JsonProperty("meterId")
	private String meterId = null;

	@JsonProperty("meterInstallationDate")
	private Long meterInstallationDate = null;

	@JsonProperty("proposedPipeSize")
	private String proposedPipeSize = null;

	@JsonProperty("proposedTaps")
	private Integer proposedTaps = null;

	@JsonProperty("pipeSize")
	private String pipeSize = null;

	@JsonProperty("noOfTaps")
	private Integer noOfTaps = null;
	
	@JsonProperty("waterApplicationType")
	private String waterApplicationType = null;
	
	@JsonProperty("securityCharge")
	private Double securityCharge = null;
	
	@JsonProperty("connectionUsagesType")
	private String connectionUsagesType = null;

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
	
	@JsonProperty("proposedUsageCategory")
	private String proposedUsageCategory = null;
	
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

	public WaterConnection waterSource(String waterSource) {
		this.waterSource = waterSource;
		return this;
	}

	/**
	 * It is a namespaced master data, defined in MDMS
	 * 
	 * @return waterSource
	 **/
	@ApiModelProperty(required = true, value = "It is a namespaced master data, defined in MDMS")
	@Size(min = 2, max = 64)
	public String getWaterSource() {
		return waterSource;
	}

	public void setWaterSource(String waterSource) {
		this.waterSource = waterSource;
	}

	public WaterConnection meterId(String meterId) {
		this.meterId = meterId;
		return this;
	}

	/**
	 * Unique id of the meter.
	 * 
	 * @return meterId
	 **/
	@ApiModelProperty(value = "Unique id of the meter.")

	@Size(min = 2, max = 64)
	public String getMeterId() {
		return meterId;
	}

	public void setMeterId(String meterId) {
		this.meterId = meterId;
	}

	public WaterConnection meterInstallationDate(Long meterInstallationDate) {
		this.meterInstallationDate = meterInstallationDate;
		return this;
	}

	/**
	 * The date of meter installation date.
	 * 
	 * @return meterInstallationDate
	 **/
	@ApiModelProperty(value = "The date of meter installation date.")

	public Long getMeterInstallationDate() {
		return meterInstallationDate;
	}

	public void setMeterInstallationDate(Long meterInstallationDate) {
		this.meterInstallationDate = meterInstallationDate;
	}

	public WaterConnection noOfTaps(Integer noOfTaps) {
		this.noOfTaps = noOfTaps;
		return this;
	}

	/**
	 * No of taps for non-metered calculation attribute.
	 * 
	 * @return noOfTaps
	 **/
	@ApiModelProperty(value = "No of taps for non-metered calculation attribute.")

	public Integer getNoOfTaps() {
		return noOfTaps;
	}

	public void setNoOfTaps(Integer noOfTaps) {
		this.noOfTaps = noOfTaps;
	}

	/**
	 * Proposed taps for non-metered calculation attribute.
	 * 
	 * @return pipeSize
	 **/
	@ApiModelProperty(value = "No of proposed taps no is citizen input")

	public Integer getProposedTaps() {
		return proposedTaps;
	}

	public void setProposedTaps(Integer proposedTaps) {
		this.proposedTaps = proposedTaps;
	}

	public WaterConnection proposedProposedTaps(Integer proposedTaps) {
		this.proposedTaps = proposedTaps;
		return this;
	}

	/**
	 * Proposed Pipe size for non-metered calculation attribute.
	 * 
	 * @return pipeSize
	 **/
	@ApiModelProperty(value = "No of proposed Pipe size is citizen input")

	public String getProposedPipeSize() {
		return proposedPipeSize;
	}

	public void setProposedPipeSize(String proposedPipeSize) {
		this.proposedPipeSize = proposedPipeSize;
	}

	public WaterConnection proposedPipeSize(String proposedPipeSize) {
		this.proposedPipeSize = proposedPipeSize;
		return this;
	}

	public WaterConnection pipeSize(String pipeSize) {
		this.pipeSize = pipeSize;
		return this;
	}

	/**
	 * Pipe size for non-metered calulation attribute.
	 * 
	 * @return pipeSize
	 **/
	@ApiModelProperty(value = "Pipe size for non-metered calulation attribute.")

	public String getPipeSize() {
		return pipeSize;
	}

	public void setPipeSize(String pipeSize) {
		this.pipeSize = pipeSize;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		WaterConnection waterConnection = (WaterConnection) o;
		return Objects.equals(this.waterSource, waterConnection.waterSource)
				&& Objects.equals(this.meterId, waterConnection.meterId)
				&& Objects.equals(this.meterInstallationDate, waterConnection.meterInstallationDate) && super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(waterSource, meterId, meterInstallationDate, super.hashCode());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class WaterConnection {\n");
		sb.append("    ").append(toIndentedString(super.toString())).append("\n");
		sb.append("    waterSource: ").append(toIndentedString(waterSource)).append("\n");
		sb.append("    meterId: ").append(toIndentedString(meterId)).append("\n");
		sb.append("    meterInstallationDate: ").append(toIndentedString(meterInstallationDate)).append("\n");
		sb.append("    activityType: ").append(toIndentedString(activityType)).append("\n");
		sb.append("    waterApplicationType: ").append(toIndentedString(waterApplicationType)).append("\n");
		sb.append("    securityCharge: ").append(toIndentedString(securityCharge)).append("\n");
		sb.append("    connectionUsagesType: ").append(toIndentedString(connectionUsagesType)).append("\n");
		sb.append("    inWorkflow: ").append(toIndentedString(inWorkflow)).append("\n");
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

	public String getWaterApplicationType() {
		return waterApplicationType;
	}

	public void setWaterApplicationType(String waterApplicationType) {
		this.waterApplicationType = waterApplicationType;
	}

	public Double getSecurityCharge() {
		return securityCharge;
	}

	public void setSecurityCharge(Double securityCharge) {
		this.securityCharge = securityCharge;
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

	public String getConnectionUsagesType() {
		return connectionUsagesType;
	}

	public void setConnectionUsagesType(String connectionUsagesType) {
		this.connectionUsagesType = connectionUsagesType;
	}

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

	public String getProposedUsageCategory() {
		return proposedUsageCategory;
	}

	public void setProposedUsageCategory(String proposedUsageCategory) {
		this.proposedUsageCategory = proposedUsageCategory;
	}
}
