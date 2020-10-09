package org.egov.cpt.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.DuplicateCopy;
import org.egov.cpt.models.DuplicateCopySearchCriteria;
import org.egov.cpt.models.Mortgage;
import org.egov.cpt.models.NoticeGeneration;
import org.egov.cpt.models.NoticeSearchCriteria;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.PropertyCriteria;
import org.egov.cpt.models.PropertyImages;
import org.egov.cpt.models.RentAccount;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentPayment;
import org.egov.cpt.producer.Producer;
import org.egov.cpt.web.contracts.DuplicateCopyRequest;
import org.egov.cpt.workflow.WorkflowIntegrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PropertyRepository {

	@Autowired
	private PropertyQueryBuilder queryBuilder;

	@Autowired
	private PropertyRowMapper rowMapper;

	@Autowired
	private DuplicateCopyPropertyRowMapper duplicateCopyPropertyRowMapper;

	@Autowired
	private DuplicateCopyQueryBuilder duplicatecopyQueryBuilder;

	@Autowired
	private MortgageRowMapper mortgageRowMapper;

	@Autowired
	private MortgageQueryBuilder mortgageQueryBuilder;

	@Autowired
	private PropertyImagesRowMapper propertyImagesRowMapper;

	@Autowired
	private PropertyImagesQueryBuilder propertyImagesQueryBuilder;

	@Autowired
	private NoticeRowMapper noticeRowMapper;

	@Autowired
	private NoticeQueryBuilder noticeQueryBuilder;

	@Autowired
	private Producer producer;

	@Autowired
	private PropertyConfiguration config;

	@Autowired
	WorkflowIntegrator workflowIntegrator;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private RentDetailQueryBuilder rentQueryBuilder;

	@Autowired
	private RentDemandRowMapper rentDemandrowMapper;

	@Autowired
	private RentPaymentRowMapper rentPaymentrowMapper;

	@Autowired
	private RentAccountRowMapper rentAccountrowMapper;

	@Autowired
	private PropertyDueRowMapper propertyDueRowMapper;

	public List<Property> getProperties(PropertyCriteria criteria) {

		Map<String, Object> preparedStmtList = new HashMap<>();
		String query = queryBuilder.getPropertySearchQuery(criteria, preparedStmtList);
		log.debug("query:" + query);
		log.debug("preparedStmtList:" + preparedStmtList);
		return namedParameterJdbcTemplate.query(query, preparedStmtList, rowMapper);
	}

	public List<String> getPropertyIds(PropertyCriteria criteria) {
		Map<String, Object> preparedStmtList = new HashMap<>();
		String query = rentQueryBuilder.getPropertyIdQuery(criteria, preparedStmtList);
		log.debug("query:" + query);
		log.debug("preparedStmtList:" + preparedStmtList);
		List<String> propertyIds = namedParameterJdbcTemplate.query(query, preparedStmtList, new RowMapper<String>() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString(1);
			}
		});
		log.debug("propertyIds:", propertyIds);
		return propertyIds;

	}

	public List<RentDemand> getPropertyRentDemandDetails(PropertyCriteria criteria) {
		Map<String, Object> preparedStmtList = new HashMap<>();
		String query = rentQueryBuilder.getPropertyRentDemandSearchQuery(criteria, preparedStmtList);
		log.debug("query:" + query);
		log.debug("preparedStmtList:" + preparedStmtList);
		return namedParameterJdbcTemplate.query(query, preparedStmtList, rentDemandrowMapper);

	}

	public List<RentPayment> getPropertyRentPaymentDetails(PropertyCriteria criteria) {
		Map<String, Object> preparedStmtList = new HashMap<>();
		String query = rentQueryBuilder.getPropertyRentPaymentSearchQuery(criteria, preparedStmtList);
		log.debug("query:" + query);
		log.debug("preparedStmtList:" + preparedStmtList);
		return namedParameterJdbcTemplate.query(query, preparedStmtList, rentPaymentrowMapper);

	}

	public RentAccount getPropertyRentAccountDetails(PropertyCriteria criteria) {
		Map<String, Object> preparedStmtList = new HashMap<>();
		String query = rentQueryBuilder.getPropertyRentAccountSearchQuery(criteria, preparedStmtList);
		log.debug("query:" + query);
		log.debug("preparedStmtList:" + preparedStmtList);
		return namedParameterJdbcTemplate.query(query, preparedStmtList, rentAccountrowMapper);
	}

	public List<DuplicateCopy> getDuplicateCopyProperties(DuplicateCopySearchCriteria criteria) {
		Map<String, Object> preparedStmtList = new HashMap<>();
		String query = duplicatecopyQueryBuilder.getDuplicateCopyPropertySearchQuery(criteria, preparedStmtList);
		// log.info("SearchQuery:"+query);
		return namedParameterJdbcTemplate.query(query, preparedStmtList, duplicateCopyPropertyRowMapper);
	}

	// PI
	public List<PropertyImages> getPropertyImagesProperties(DuplicateCopySearchCriteria criteria) {
		Map<String, Object> preparedStmtList = new HashMap<>();
		String query = propertyImagesQueryBuilder.getPropertyImagesSearchQuery(criteria, preparedStmtList);
		// log.info("SearchQuery:"+query);
		return namedParameterJdbcTemplate.query(query, preparedStmtList, propertyImagesRowMapper);
	}

	public List<Mortgage> getMortgageProperties(DuplicateCopySearchCriteria criteria) {
		Map<String, Object> preparedStmtList = new HashMap<>();
		String query = mortgageQueryBuilder.getMortgageSearchQuery(criteria, preparedStmtList);
		log.debug("MortgageSearchQuery:" + query);
		return namedParameterJdbcTemplate.query(query, preparedStmtList, mortgageRowMapper);
	}

	public List<NoticeGeneration> getNotices(NoticeSearchCriteria criteria) {
		Map<String, Object> preparedStmtList = new HashMap<>();
		String query = noticeQueryBuilder.getNoticeSearchQuery(criteria, preparedStmtList);
		log.debug("noticeSearchQuery:" + query);
		return namedParameterJdbcTemplate.query(query, preparedStmtList, noticeRowMapper);
	}

	public void updateDcPayment(DuplicateCopyRequest duplicateCopyRequest,
			Map<String, Boolean> idToIsStateUpdatableMapDc) {
		RequestInfo requestInfo = duplicateCopyRequest.getRequestInfo();
		List<DuplicateCopy> dcApplications = duplicateCopyRequest.getDuplicateCopyApplications();

		List<DuplicateCopy> dcApplicationsForUpdate = new LinkedList<>();

		for (DuplicateCopy dcApplication : dcApplications) {
			if (idToIsStateUpdatableMapDc.get(dcApplication.getId())) {
				dcApplicationsForUpdate.add(dcApplication);
			}
		}

		if (!CollectionUtils.isEmpty(dcApplicationsForUpdate)) {
			workflowIntegrator
					.callDuplicateCopyWorkFlow(new DuplicateCopyRequest(requestInfo, dcApplicationsForUpdate));
			producer.push(config.getUpdateDuplicateCopyTopic(),
					new DuplicateCopyRequest(requestInfo, dcApplicationsForUpdate));
		}
	}

	public List<Property> getPropertyOwner(PropertyCriteria criteria) {
		Map<String, Object> preparedStmtList = new HashMap<>();
		String query = rentQueryBuilder.getPropertyWithActiveOwnerQuery(criteria, preparedStmtList);
		log.debug("query:" + query);
		log.debug("preparedStmtList:" + preparedStmtList);
		return namedParameterJdbcTemplate.query(query, preparedStmtList, propertyDueRowMapper);
	}

}
