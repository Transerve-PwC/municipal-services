package org.egov.waterconnection.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class BillGenerationFile {

	@JsonProperty("billGenerationFileId")
	private String billGenerationFileId = null;

	@JsonProperty("billFileStoreId")
	private String billFileStoreId = null;
	
	@JsonProperty("billFileStoreUrl")
	private String billFileStoreUrl = null;

	@JsonProperty("fileGenerationTime")
	private Long fileGenerationTime = null;
}
