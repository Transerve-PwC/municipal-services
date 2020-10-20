package org.egov.ps.service;

import java.util.List;

import org.egov.ps.model.AuctionBidder;
import org.egov.ps.model.ExcelSearchCriteria;
import org.egov.ps.util.FileStoreUtils;
import org.egov.ps.web.contracts.AuctionTransactionRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuctionService {

	@Autowired
	private AuctionExcelParseService readExcelService;

	@Autowired
	private FileStoreUtils fileStoreUtils;

	public List<AuctionBidder> saveAuctionWithProperty(ExcelSearchCriteria searchCriteria,
			AuctionTransactionRequest auctionTransactionRequest) {
		try {
			String filePath = fileStoreUtils.fetchFileStoreUrl(searchCriteria);
			if (!filePath.isEmpty()) {
				return readExcelService.getDatafromExcel(new UrlResource(filePath).getInputStream(), 0);
			} else {
				log.error("Could not find a filePath for given tenant '{}' and file store id '{}'",
						searchCriteria.getTenantId(), searchCriteria.getFileStoreId());
				throw new CustomException("FILE NOT FOUND", "Uploaded file could not be retrieved.");
			}
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error occur during runnig controller method readExcel():", e);
			throw new CustomException("PARSE FAILED", "Could not parse provided excel file for auction bidders.");
		}
	}

}
