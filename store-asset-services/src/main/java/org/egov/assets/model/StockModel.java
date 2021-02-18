package org.egov.assets.model;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString

public class StockModel {
	
	
	private String material ;
	
	private String recieptdate ;
	
	private String days;
	
	private String acceptedqty;

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	public String getRecieptdate() {
		return recieptdate;
	}

	public void setRecieptdate(String recieptdate) {
		this.recieptdate = recieptdate;
	}

	public String getDays() {
		return days;
	}

	public void setDays(String days) {
		this.days = days;
	}

	public String getAcceptedqty() {
		return acceptedqty;
	}

	public void setAcceptedqty(String acceptedqty) {
		this.acceptedqty = acceptedqty;
	}
	
	
			
	
	
}
