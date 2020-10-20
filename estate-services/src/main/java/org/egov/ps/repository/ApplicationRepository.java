package org.egov.ps.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.ps.model.Application;
import org.egov.ps.model.ApplicationCriteria;
import org.egov.ps.model.Document;
import org.egov.ps.model.Owner;
import org.egov.ps.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Repository
public class ApplicationRepository {
	
	@Autowired
	private ApplicationQueryBuilder applicationQueryBuilder;
	
	@Autowired
	private ApplicationRowMapper applicationRowMapper;
	
	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	@Autowired
	private OwnerRowMapper ownerRowMapper;
	
	@Autowired
	private DocumentsRowMapper documentRowMapper;
	
	@Autowired
	PropertyRepository propertyRepository;
	
	public List<Application> getApplications(ApplicationCriteria criteria) {
		Map<String, Object> preparedStmtList = new HashMap<>();
		String query = applicationQueryBuilder.getApplicationSearchQuery(criteria, preparedStmtList);
		
		// application ....
		List<Application> application = namedParameterJdbcTemplate.query(query, preparedStmtList, applicationRowMapper);

		// property ....
		List<Property> properties = application.stream().map(application_ -> application_.getProperty())
				.collect(Collectors.toList());
				
		if (CollectionUtils.isEmpty(application)) {
			return application;
		}
		
		List<String> relationsApplication = criteria.getRelations();
		List<String> relationsProperty = criteria.getPropertyRelations();
		
		if (CollectionUtils.isEmpty(relationsApplication)) {
			relationsApplication = new ArrayList<String>();
			relationsProperty = new ArrayList<String>();
			
			if (application.size() == 1) {
				relationsApplication.add(ApplicationQueryBuilder.RELATION_OWNER);
				relationsProperty.add(PropertyQueryBuilder.RELATION_OWNER);
				
				relationsApplication.add(ApplicationQueryBuilder.RELATION_OWNER_DOCUMENTS);
				relationsProperty.add(PropertyQueryBuilder.RELATION_OWNER_DOCUMENTS);
			}
			
		}
		
		if (application.contains(ApplicationQueryBuilder.RELATION_OWNER)) {
			this.addOwnersToApplication(application);
			propertyRepository.addOwnersToProperties(properties);
		}
		
		if (application.contains(PropertyQueryBuilder.RELATION_OWNER_DOCUMENTS)) {
			this.addOwnerDocumentsToApplication(application);
			propertyRepository.addOwnerDocumentsToProperties(properties);
		}
		
		
		return application;
	}
	
	private void addOwnersToApplication(List<Application> applications) {
		if (CollectionUtils.isEmpty(applications)) {
			return;
		}
		/**
		 * Extract applications property detail ids.
		 */
		List<String> applicationDetailsIds = applications.stream().map(application -> application.getProperty().getId())
				.collect(Collectors.toList());

		/**
		 * Fetch owners from database
		 */
		Map<String, Object> params = new HashMap<String, Object>(1);
		String ownerDocsQuery = applicationQueryBuilder.getOwnersQuery(applicationDetailsIds, params);
		List<Owner> owners = namedParameterJdbcTemplate.query(ownerDocsQuery, params, ownerRowMapper);

		/**
		 * Assign owners to corresponding properties
		 */
		
		applications.stream().forEach(application -> {
			application.getProperty().getPropertyDetails().setOwners(owners.stream().filter(
					owner -> owner.getPropertyDetailsId().equalsIgnoreCase(application.getProperty().getPropertyDetails().getId()))
					.collect(Collectors.toList()));
		});
	}
	
	private void addOwnerDocumentsToApplication(List<Application> applications) {
		if (CollectionUtils.isEmpty(applications)) { 
			return;
		}
		/**
		 * Extract ownerIds
		 */
		List<Owner> owners = applications.stream().map(application -> application.getProperty().getPropertyDetails().getOwners())
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
		String ownerDocsQuery = applicationQueryBuilder.getOwnerDocsQuery(ownerDetailIds, params);
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
	
	
}
