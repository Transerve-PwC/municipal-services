package org.egov.cpt.web.controllers;

import java.io.File;
import java.net.URI;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.egov.cpt.models.ExcelSearchCriteria;
import org.egov.cpt.models.RentDemandResponse;
import org.egov.cpt.models.RequestInfoWrapper;
import org.egov.cpt.service.ReadExcelService;
import org.egov.cpt.util.FileStoreUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/v1/excel")
public class ReadExcelController {

	private ReadExcelService readExcelService;
	private FileStoreUtils fileStoreUtils;

	@Autowired
	public ReadExcelController(ReadExcelService readExcelService, FileStoreUtils fileStoreUtils) {
		this.fileStoreUtils = fileStoreUtils;
		this.readExcelService = readExcelService;
	}

	@PostMapping("/read")
	public ResponseEntity<RentDemandResponse> readExcel(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute ExcelSearchCriteria searchCriteria) {
		try {
			log.info("Start controller method readExcel() Request:" + searchCriteria);
			String filePath = fileStoreUtils.fetchFileStoreUrl(searchCriteria);
			if (StringUtils.isBlank(filePath)) {
				throw new CustomException("FILE_NOT_FOUND", "Cannot find rent history file that is uploaded");
			}
			File tempFile = File.createTempFile("File" + System.currentTimeMillis(), ".xlsx");
			FileUtils.copyURLToFile(new URI(filePath).toURL(), tempFile);
			RentDemandResponse data = this.readExcelService.getDatafromExcel(tempFile, 0);
			tempFile.delete();
			log.info("End controller method readExcel Demand data:" + data.getDemand().size() + " & Payment data:"
					+ data.getPayment().size());
			return new ResponseEntity<>(data, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred during readExcel():" + e.getMessage(), e);
			throw new CustomException("RENT_HISTORY_UPLOAD_FAILED", e.getMessage());
		}
	}
}
