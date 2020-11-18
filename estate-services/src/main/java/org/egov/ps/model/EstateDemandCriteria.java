package org.egov.ps.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstateDemandCriteria {

	private String date;
	
	public boolean isEmpty() {
		return (this.date == null);
	}
}
