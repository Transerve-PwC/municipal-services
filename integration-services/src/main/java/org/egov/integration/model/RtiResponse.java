package org.egov.integration.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class RtiResponse {	
	private RtiIndividualResponse cpio;
	private RtiIndividualResponse nodal;
	private RtiIndividualResponse appellate;
}
