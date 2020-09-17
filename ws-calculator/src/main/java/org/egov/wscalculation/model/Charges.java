package org.egov.wscalculation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class Charges {
	private String security;	
	private String ferrule;
	private String metertesting;	
	private String meterfitting;
	public String getSecurity() {
		return security;
	}
	public void setSecurity(String security) {
		this.security = security;
	}
	public String getFerrule() {
		return ferrule;
	}
	public void setFerrule(String ferrule) {
		this.ferrule = ferrule;
	}
	public String getMetertesting() {
		return metertesting;
	}
	public void setMetertesting(String metertesting) {
		this.metertesting = metertesting;
	}
	public String getMeterfitting() {
		return meterfitting;
	}
	public void setMeterfitting(String meterfitting) {
		this.meterfitting = meterfitting;
	}
	
	
	
}
