package org.egov.waterconnection.repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.egov.waterconnection.config.WSConfiguration;
import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.model.BillGeneration;
import org.egov.waterconnection.model.BillGenerationFile;
import org.egov.waterconnection.model.FileStore;
import org.egov.waterconnection.producer.WaterConnectionProducer;
import org.egov.waterconnection.repository.builder.WsQueryBuilder;
import org.egov.waterconnection.service.WorkflowNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Repository
public class BillGenerationDaoImpl implements BillGenerationDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private WSConfiguration wsConfiguration;

	@Autowired
	private WorkflowNotificationService workflowNotificationService;
	
	@Autowired
	private WaterConnectionProducer waterConnectionProducer;

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private WsQueryBuilder queryBuilder;

	@Override
	public List<BillGeneration> saveBillingData(List<BillGeneration> listOfBills) {

		waterConnectionProducer.push(wsConfiguration.getSaveWaterBilling(), listOfBills);

		return listOfBills;
	}

	@Override
	public List<BillGeneration> getBillingEstimation() {

		List<BillGeneration> billingDetails;

		billingDetails = jdbcTemplate.query(queryBuilder.getBillingDataForDemandGeneration, new Object[] {},
				new BeanPropertyRowMapper<BillGeneration>(BillGeneration.class));
		return billingDetails;

	}

	@Override
	public BillGenerationFile getFilesStoreUrl() {
		HttpHeaders http = new HttpHeaders();
		http.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultiValueMap<String, Object> mmap = new LinkedMultiValueMap<String, Object>();
		try {
			mmap.add("file", getFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mmap.add("tenantId", "ch");
		mmap.add("module", WCConstants.MODULE);
		HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity(mmap, http);

		FileStore response = restTemplate.postForObject(wsConfiguration.getFileStoreHost() + wsConfiguration.getFileStoreupload(), entity,
				FileStore.class);

	String fileStroreUrl =	workflowNotificationService.getApplicationDownloadLink(response.getFiles().get(0).getTenantId(), response.getFiles().get(0).getFileStoreId());
	BillGenerationFile billFile	= BillGenerationFile.builder().billFileStoreId(UUID.randomUUID().toString()).billFileStoreUrl(fileStroreUrl).billFileStoreId( response.getFiles().get(0).getFileStoreId()).fileGenerationTime(System.currentTimeMillis()).build();
	return billFile;
	}
	
	public static Resource getFile() throws IOException {
		File report = new File(WCConstants.WS_BILLING_FILENAME);
		Path filePath = report.toPath();
		Files.write(report.toPath(), Files.readAllBytes(report.toPath()));
		return new FileSystemResource(filePath.toFile());
	}

	@Override
	public void savefileHistory(BillGenerationFile billFile, List<BillGeneration> bill) {
		waterConnectionProducer.push(wsConfiguration.getSavewaterbillingfile(), billFile);
		
		waterConnectionProducer.push(wsConfiguration.getUpdateBillfileflag(), bill);
		
	}

	@Override
	public List<BillGenerationFile> getBillingFiles() {

		List<BillGenerationFile> billingDetails;

		billingDetails = jdbcTemplate.query(queryBuilder.GET_WS_BILLING_FILES, new Object[] {},
				new BeanPropertyRowMapper<BillGenerationFile>(BillGenerationFile.class));
		return billingDetails;

	}

	@Override
	public List<BillGeneration> getBillData(BillGeneration billGeneration) {
		List<BillGeneration> billingDetails;

		billingDetails = jdbcTemplate.query(queryBuilder.GET_WS_BILLING_Data, new Object[] {billGeneration.getConsumerCode()},
				new BeanPropertyRowMapper<BillGeneration>(BillGeneration.class));
		return billingDetails;
	}

}
