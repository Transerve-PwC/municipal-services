package org.egov.ps.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.ps.model.AuctionBidder;
import org.egov.ps.model.CourtCase;
import org.egov.ps.model.Document;
import org.egov.ps.model.OfflinePaymentDetails;
import org.egov.ps.model.Owner;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.workflow.WorkflowIntegrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class PropertyRepository {

	@Autowired
	private PropertyQueryBuilder propertyQueryBuilder;

	@Autowired
	private PropertyRowMapper propertyRowMapper;

	@Autowired
	private DocumentsRowMapper documentRowMapper;

	@Autowired
	private OwnerRowMapper ownerRowMapper;

	@Autowired
	private CourtCasesRowMapper courtCasesRowMapper;

	@Autowired
	private AuctionRowMapper biddersRowMapper;

	@Autowired
	private EstateDemandRowMapper estateDemandRowMapper;

	@Autowired
	private EstatePaymentRowMapper estatePaymentRowMapper;

	@Autowired
	WorkflowIntegrator workflowIntegrator;

	@Autowired
	private EstateAccountRowMapper estateAccountrowMapper;


	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private OfflinePaymentRowMapper offlinePaymentRowMapper;

	public List<Property> getProperties(PropertyCriteria criteria) {

		Map<String, Object> paramMap = new HashMap<>();
		String query = propertyQueryBuilder.getPropertySearchQuery(criteria, paramMap);
		log.debug("Property Query {}", query);
		log.debug("ParamMap {}", paramMap);
		List<Property> properties = namedParameterJdbcTemplate.query(query, paramMap, propertyRowMapper);
		if (CollectionUtils.isEmpty(properties)) {
			return properties;
		}
		List<String> relations = criteria.getRelations();
		if (CollectionUtils.isEmpty(relations)) {
			relations = new ArrayList<String>();
			if (properties.size() == 1) {
				relations.add(PropertyQueryBuilder.RELATION_OWNER);
				relations.add(PropertyQueryBuilder.RELATION_OWNER_DOCUMENTS);
				relations.add(PropertyQueryBuilder.RELATION_COURT);
				relations.add(PropertyQueryBuilder.RELATION_BIDDER);
				relations.add(PropertyQueryBuilder.RELATION_ESTATE_FINANCE);
				relations.add(PropertyQueryBuilder.RELATION_OFFLINE_PAYMENT);
			}
		}
		if (relations.contains(PropertyQueryBuilder.RELATION_OWNER)) {
			this.addOwnersToProperties(properties);
		}
		if (relations.contains(PropertyQueryBuilder.RELATION_OWNER_DOCUMENTS)) {
			this.addOwnerDocumentsToProperties(properties);
		}
		if (relations.contains(PropertyQueryBuilder.RELATION_COURT)) {
			this.addCourtCasesToProperties(properties);
		}
		if (relations.contains(PropertyQueryBuilder.RELATION_BIDDER)) {
			this.addBiddersToProperties(properties);
		}
		if (relations.contains(PropertyQueryBuilder.RELATION_ESTATE_FINANCE)) {
			this.addEstateDemandToProperties(properties);
			this.addEstatePaymentToProperties(properties);
		}
		if (relations.contains(PropertyQueryBuilder.RELATION_OFFLINE_PAYMENT)) {
			this.addOfflinePaymentToProperties(properties);
		}
		return properties;
	}

	private void addOwnersToProperties(List<Property> properties) {
		if (CollectionUtils.isEmpty(properties)) {
			return;
		}
		/**
		 * Extract property detail ids.
		 */
		List<String> propertyDetailsIds = properties.stream().map(property -> property.getPropertyDetails().getId())
				.collect(Collectors.toList());

		/**
		 * Fetch owners from database
		 */
		Map<String, Object> params = new HashMap<String, Object>(1);
		String ownerDocsQuery = propertyQueryBuilder.getOwnersQuery(propertyDetailsIds, params);
		List<Owner> owners = namedParameterJdbcTemplate.query(ownerDocsQuery, params, ownerRowMapper);

		/**
		 * Assign owners to corresponding properties
		 */
		properties.stream().forEach(property -> {
			property.getPropertyDetails().setOwners(owners.stream().filter(
					owner -> owner.getPropertyDetailsId().equalsIgnoreCase(property.getPropertyDetails().getId()))
					.collect(Collectors.toList()));
		});
	}

	private void addOwnerDocumentsToProperties(List<Property> properties) {
		if (CollectionUtils.isEmpty(properties)) {
			return;
		}
		/**
		 * Extract ownerIds
		 */
		List<Owner> owners = properties.stream().map(property -> property.getPropertyDetails().getOwners())
				.flatMap(Collection::stream).collect(Collectors.toList());
		List<String> ownerDetailIds = owners.stream().map(owner -> owner.getOwnerDetails().getId())
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(ownerDetailIds)) {
			return;
		}
		/**
		 * Fetch documents from database.
		 */
		Map<String, Object> params = new HashMap<String, Object>(1);
		String ownerDocsQuery = propertyQueryBuilder.getOwnerDocsQuery(ownerDetailIds, params);
		List<Document> documents = namedParameterJdbcTemplate.query(ownerDocsQuery, params, documentRowMapper);

		/**
		 * Assign documents to corresponding owners.
		 */
		owners.stream().forEach(owner -> {
			owner.getOwnerDetails()
					.setOwnerDocuments(documents.stream().filter(
							document -> document.getReferenceId().equalsIgnoreCase(owner.getOwnerDetails().getId()))
							.collect(Collectors.toList()));
		});
	}

	private void addCourtCasesToProperties(List<Property> properties) {
		/**
		 * Extract property detail ids.
		 */
		List<String> propertyDetailsIds = properties.stream().map(property -> property.getPropertyDetails().getId())
				.collect(Collectors.toList());

		/**
		 * Fetch court cases from database
		 */
		Map<String, Object> params = new HashMap<String, Object>(1);
		String courtCasesQuery = propertyQueryBuilder.getCourtCasesQuery(propertyDetailsIds, params);
		List<CourtCase> courtCases = namedParameterJdbcTemplate.query(courtCasesQuery, params, courtCasesRowMapper);

		/**
		 * Assign court cases to corresponding properties
		 */
		properties.stream().forEach(property -> {
			property.getPropertyDetails()
					.setCourtCases(courtCases.stream()
							.filter(courtCase -> courtCase.getPropertyDetailsId()
									.equalsIgnoreCase(property.getPropertyDetails().getId()))
							.collect(Collectors.toList()));
		});
	}

	private void addBiddersToProperties(List<Property> properties) {
		/**
		 * Extract property detail ids.
		 */
		List<String> propertyDetailsIds = properties.stream().map(property -> property.getPropertyDetails().getId())
				.collect(Collectors.toList());

		/**
		 * Fetch bidders from database
		 */
		List<AuctionBidder> bidders = this.getBiddersForPropertyDetailsIds(propertyDetailsIds);

		/**
		 * Assign court cases to corresponding properties
		 */
		properties.stream().forEach(property -> {
			property.getPropertyDetails().setBidders(bidders.stream().filter(
					bidder -> bidder.getPropertyDetailsId().equalsIgnoreCase(property.getPropertyDetails().getId()))
					.collect(Collectors.toList()));
		});
	}

	private void addEstateDemandToProperties(List<Property> properties) {
		/**
		 * Extract property detail ids.
		 */
		List<String> propertyDetailsIds = properties.stream().map(property -> property.getPropertyDetails().getId())
				.collect(Collectors.toList());

		/**
		 * Fetch demand from database
		 */
		List<EstateDemand> estateDemands = this.getDemandDetailsForPropertyDetailsIds(propertyDetailsIds);

		/**
		 * Assign demands to corresponding properties
		 */
		properties.stream().forEach(property -> {
			property.getPropertyDetails()
					.setEstateDemands(estateDemands.stream()
							.filter(estateDemand -> estateDemand.getPropertyDetailsId()
									.equalsIgnoreCase(property.getPropertyDetails().getId()))
							.collect(Collectors.toList()));
		});
	}

	private void addEstatePaymentToProperties(List<Property> properties) {
		/**
		 * Extract property detail ids.
		 */
		List<String> propertyDetailsIds = properties.stream().map(property -> property.getPropertyDetails().getId())
				.collect(Collectors.toList());

		/**
		 * Fetch payments from database
		 */
		List<EstatePayment> estatePayments = this.getEstatePaymentsForPropertyDetailsIds(propertyDetailsIds);

		/**
		 * Assign payments to corresponding properties
		 */
		properties.stream().forEach(property -> {
			property.getPropertyDetails()
					.setEstatePayments(estatePayments.stream()
							.filter(estatePayment -> estatePayment.getPropertyDetailsId()
									.equalsIgnoreCase(property.getPropertyDetails().getId()))
							.collect(Collectors.toList()));
		});
	}

	private void addOfflinePaymentToProperties(List<Property> properties) {
		/**
		 * Extract property detail ids.
		 */
		List<String> propertyDetailsIds = properties.stream().map(property -> property.getPropertyDetails().getId())
				.collect(Collectors.toList());

		/**
		 * Fetch bidders from database
		 */
		List<OfflinePaymentDetails> offlinePayments = this.getOfflinePaymentsForPropertyDetailsIds(propertyDetailsIds);

		/**
		 * Assign court cases to corresponding properties
		 */
		properties.stream().forEach(property -> {
			property.getPropertyDetails()
					.setOfflinePaymentDetails(offlinePayments.stream()
							.filter(offlinePayment -> offlinePayment.getPropertyDetailsId()
									.equalsIgnoreCase(property.getPropertyDetails().getId()))
							.collect(Collectors.toList()));
		});
	}

	public List<AuctionBidder> getBiddersForPropertyDetailsIds(List<String> propertyDetailsIds) {
		Map<String, Object> params = new HashMap<String, Object>(1);
		String biddersQuery = propertyQueryBuilder.getBiddersQuery(propertyDetailsIds, params);
		return namedParameterJdbcTemplate.query(biddersQuery, params, biddersRowMapper);
	}

	public List<EstateDemand> getDemandDetailsForPropertyDetailsIds(List<String> propertyDetailsIds) {
		Map<String, Object> params = new HashMap<String, Object>(1);
		String estateDemandQuery = propertyQueryBuilder.getEstateDemandQuery(propertyDetailsIds, params);
		return namedParameterJdbcTemplate.query(estateDemandQuery, params, estateDemandRowMapper);
	}

	public List<EstatePayment> getEstatePaymentsForPropertyDetailsIds(List<String> propertyDetailsIds) {
		Map<String, Object> params = new HashMap<String, Object>(1);
		String estatePaymentsQuery = propertyQueryBuilder.getEstatePaymentsQuery(propertyDetailsIds, params);
		return namedParameterJdbcTemplate.query(estatePaymentsQuery, params, estatePaymentRowMapper);
	}

	public Property findPropertyById(String propertyId) {
		PropertyCriteria propertySearchCriteria = PropertyCriteria.builder().propertyId(propertyId).build();
		List<Property> properties = this.getProperties(propertySearchCriteria);
		if (properties == null || properties.isEmpty()) {
			return null;
		}
		return properties.get(0);
	}

	public EstateAccount getPropertyEstateAccountDetails(List<String> propertyDetailsIds) {
		Map<String, Object> preparedStmtList = new HashMap<>();
		String query = propertyQueryBuilder.getPropertyRentAccountSearchQuery(propertyDetailsIds, preparedStmtList);
		log.debug("query:" + query);
		log.debug("preparedStmtList:" + preparedStmtList);
		return namedParameterJdbcTemplate.query(query, preparedStmtList, estateAccountrowMapper);
	}

//	public RentAccount getPropertyRentAccountDetails(PropertyCriteria criteria) {
//		Map<String, Object> preparedStmtList = new HashMap<>();
//		String query = propertyQueryBuilder.getPropertyRentAccountSearchQuery(criteria, preparedStmtList);
//		log.debug("query:" + query);
//		log.debug("preparedStmtList:" + preparedStmtList);
//		return namedParameterJdbcTemplate.query(query, preparedStmtList, rentAccountRowMapper);
//	}

	public List<OfflinePaymentDetails> getOfflinePaymentsForPropertyDetailsIds(List<String> propertyDetailsIds) {
		Map<String, Object> params = new HashMap<String, Object>(1);
		String offlinePaymentsQuery = propertyQueryBuilder.getOfflinePaymentsQuery(propertyDetailsIds, params);
		return namedParameterJdbcTemplate.query(offlinePaymentsQuery, params, offlinePaymentRowMapper);
	}

	public EstateAccount getAccountDetailsForPropertyDetailsIds(List<String> propertyDetailsIds) {
		Map<String, Object> params = new HashMap<String, Object>(1);
		String estateAccountQuery = propertyQueryBuilder.getEstateAccountQuery(propertyDetailsIds, params);
		log.debug("query:" + estateAccountQuery);
		log.debug("preparedStmtList:" + params);
		return namedParameterJdbcTemplate.query(estateAccountQuery, params, estateAccountrowMapper);
	}
}
