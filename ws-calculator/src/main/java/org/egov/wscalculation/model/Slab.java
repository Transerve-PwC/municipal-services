package org.egov.wscalculation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Slab {
	private int from;
	private long to;
	
	private double charge;
	private double meterCharge;
	private String code;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	
	public double getCharge() {
		return charge;
	}
	public void setCharge(double charge) {
		this.charge = charge;
	}
	public double getMeterCharge() {
		return meterCharge;
	}
	public void setMeterCharge(double meterCharge) {
		this.meterCharge = meterCharge;
	}
	public long getTo() {
		return to;
	}
	public void setTo(long to) {
		this.to = to;
	}

	
}
