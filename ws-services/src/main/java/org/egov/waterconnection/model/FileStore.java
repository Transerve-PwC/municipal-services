package org.egov.waterconnection.model;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Validated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class FileStore {

	@Size(max = 64)
	@JsonProperty("files")
	private List<FileStore> files;

	@Size(max = 64)
	@JsonProperty("fileStoreIds")
	private List<FileStore> fileStoreIds;

	@Size(max = 64)
	@JsonProperty("fileStoreId")
	private String fileStoreId;

	@Size(max = 64)
	@JsonProperty("tenantId")
	private String tenantId;

	@Size(max = 64)
	@JsonProperty("id")
	private String id;

	@Size(max = 64)
	@JsonProperty("url")
	private String url;

	@Size(max = 256)
	@JsonProperty("module")
	private String module;

	@Size(max = 256)
	@JsonProperty("tag")
	private String tag;

}
