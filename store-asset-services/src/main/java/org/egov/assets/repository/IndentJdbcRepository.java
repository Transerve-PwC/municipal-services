package org.egov.assets.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.assets.common.Pagination;
import org.egov.assets.model.Indent;
import org.egov.assets.model.Indent.IndentStatusEnum;
import org.egov.assets.model.IndentSearch;
import org.egov.assets.repository.entity.IndentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class IndentJdbcRepository extends org.egov.assets.common.JdbcRepository {
	private static final Logger LOG = LoggerFactory.getLogger(IndentJdbcRepository.class);

	@Autowired
	private IndentDetailJdbcRepository indentDetailJdbcRepository;
	static {
		LOG.debug("init indent");
		init(IndentEntity.class);
		LOG.debug("end init indent");
	}

	public static synchronized void init(Class T) {
		String TABLE_NAME = "";

		List<String> insertFields = new ArrayList<>();
		List<String> updateFields = new ArrayList<>();
		List<String> uniqueFields = new ArrayList<>();

		String insertQuery = "";
		String updateQuery = "";
		String searchQuery = "";

		try {

			TABLE_NAME = (String) T.getDeclaredField("TABLE_NAME").get(null);
		} catch (Exception e) {

		}
		insertFields.addAll(fetchFields(T));
		uniqueFields.add("indentNumber");
		uniqueFields.add("tenantId");
		insertFields.removeAll(uniqueFields);
		allInsertQuery.put(T.getSimpleName(), insertQuery(insertFields, TABLE_NAME, uniqueFields));
		updateFields.addAll(insertFields);
		updateFields.remove("createdBy");
		updateQuery = updateQuery(updateFields, TABLE_NAME, uniqueFields);
		System.out.println(T.getSimpleName() + "--------" + insertFields);
		allInsertFields.put(T.getSimpleName(), insertFields);
		allUpdateFields.put(T.getSimpleName(), updateFields);
		allIdentitiferFields.put(T.getSimpleName(), uniqueFields);
		// allInsertQuery.put(T.getSimpleName(), insertQuery);
		allUpdateQuery.put(T.getSimpleName(), updateQuery);
		getByIdQuery.put(T.getSimpleName(), getByIdQuery(TABLE_NAME, uniqueFields));
		System.out.println(allInsertQuery);
	}

	public IndentJdbcRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public IndentEntity create(IndentEntity entity) {
		super.create(entity);
		return entity;
	}

	public IndentEntity update(IndentEntity entity) {
		super.update(entity);
		return entity;

	}

	public boolean delete(IndentEntity entity, String reason) {
		super.delete(entity, reason);
		return true;

	}

	public IndentEntity findById(IndentEntity entity) {
		List<String> list = allIdentitiferFields.get(entity.getClass().getSimpleName());

		Map<String, Object> paramValues = new HashMap<>();

		for (String s : list) {
			paramValues.put(s, getValue(getField(entity, s), entity));
		}

		List<IndentEntity> indents = namedParameterJdbcTemplate.query(
				getByIdQuery.get(entity.getClass().getSimpleName()).toString(), paramValues,
				new BeanPropertyRowMapper(IndentEntity.class));
		if (indents.isEmpty()) {
			return null;
		} else {
			return indents.get(0);
		}

	}

	public Pagination<Indent> search(IndentSearch indentSearch) {

		String searchQuery = "select :selectfields from :tablename :condition  :orderby   ";

		Map<String, Object> paramValues = new HashMap<>();
		StringBuffer params = new StringBuffer();

		if (indentSearch.getSortBy() != null && !indentSearch.getSortBy().isEmpty()) {
			validateSortByOrder(indentSearch.getSortBy());
			validateEntityFieldName(indentSearch.getSortBy(), IndentEntity.class);
		}

		String orderBy = "order by indent.indentNumber";
		if (indentSearch.getSortBy() != null && !indentSearch.getSortBy().isEmpty()) {
			orderBy = "order by " + indentSearch.getSortBy();
		}

		searchQuery = searchQuery.replace(":tablename",
				"indent indent inner join indentdetail details on details.indentnumber=indent.indentnumber inner join Store indentStore on indentStore.code=indent.indentStore left join Store issueStore on issueStore.code=indent.issueStore");

		searchQuery = searchQuery.replace(":selectfields",
				" distinct indent.*,issueStore.code as \"issueStore.code\" ,issueStore.name as \"issueStore.name\",indentStore.code as \"indentStore.code\" ,indentStore.name as \"indentStore.name\"  ");

		// String conditions=" and issueStore.code=indent.issueStore and
		// indentStore.code=indent.indentStore and
		// details.indentnumber=indent.indentnumber";
		String conditions = "";

		// implement jdbc specfic search

		if (indentSearch.getTenantId() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("indent.tenantId =:tenantId");
			paramValues.put("tenantId", indentSearch.getTenantId());
		}

		if (indentSearch.getIds() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("indent.id in (:ids)");
			paramValues.put("ids", indentSearch.getIds());
		}

		if (indentSearch.getIssueStore() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("issueStore =:issueStore");
			paramValues.put("issueStore", indentSearch.getIssueStore());
		}

		if (indentSearch.getIndentStore() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("indentStore =:indentStore");
			paramValues.put("indentStore", indentSearch.getIndentStore());
		}

		if (indentSearch.getIndentDate() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("indentDate =:indentDate");
			paramValues.put("indentDate", indentSearch.getIndentDate());
		}
		if (indentSearch.getIndentNumber() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("indent.indentNumber =:indentNumber");
			paramValues.put("indentNumber", indentSearch.getIndentNumber());
		}
		if (indentSearch.getIndentType() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("indentType =:indentType");
			paramValues.put("indentType", indentSearch.getIndentType());
		}
		if (indentSearch.getIndentPurpose() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("indentPurpose =:indentPurpose");
			paramValues.put("indentPurpose", indentSearch.getIndentPurpose());
		}
		if (indentSearch.getInventoryType() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("inventoryType =:inventoryType");
			paramValues.put("inventoryType", indentSearch.getInventoryType());
		}

		if (indentSearch.getIndentStatus() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("indentStatus =:indentStatus");
			paramValues.put("indentStatus", indentSearch.getIndentStatus());
		}

		if (indentSearch.getDepartmentId() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("department =:department");
			paramValues.put("department", indentSearch.getDepartmentId());
		}
		if (indentSearch.getTotalIndentValue() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("totalIndentValue =:totalIndentValue");
			paramValues.put("totalIndentValue", indentSearch.getTotalIndentValue());
		}

		if (indentSearch.getIndentRaisedBy() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("indentCreatedBy =:indentCreatedBy");
			paramValues.put("indentCreatedBy", indentSearch.getIndentRaisedBy());
		}

		if (indentSearch.getStateId() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("stateId =:stateId");
			paramValues.put("stateId", indentSearch.getStateId());
		}

		// TODO : Handle the status for these
		if (indentSearch.getSearchPurpose() != null
				&& indentSearch.getSearchPurpose().equalsIgnoreCase("PurchaseOrder")) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("indentStatus =:indentStatus");
			paramValues.put("indentStatus", IndentStatusEnum.APPROVED.name());
			if (params.length() > 0)
				params.append(" and  ");
			params.append(
					" (details.poOrderedQuantity is null or details.indentQuantity - details.poOrderedQuantity > :value)");
			paramValues.put("value", Integer.valueOf(0));
		}

		if (indentSearch.getSearchPurpose() != null
				&& indentSearch.getSearchPurpose().equalsIgnoreCase("IssueMaterial")) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("indentStatus !=:indentStatus");
			paramValues.put("indentStatus", IndentStatusEnum.ISSUED.name());

		}
		if (indentSearch.getIndentFromDate() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("TO_DATE(TO_CHAR(TO_TIMESTAMP(indentDate / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') >= TO_DATE(TO_CHAR(TO_TIMESTAMP(:fromDate / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD')");
			paramValues.put("fromDate", indentSearch.getIndentFromDate());
		}

		if (indentSearch.getIndentToDate() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("TO_DATE(TO_CHAR(TO_TIMESTAMP(indentDate / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <= TO_DATE(TO_CHAR(TO_TIMESTAMP(:toDate / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD')");
			paramValues.put("toDate", indentSearch.getIndentToDate());
		}

		Pagination<Indent> page = new Pagination<>();
		if (indentSearch.getPageNumber() != null) {
			page.setOffset(indentSearch.getPageNumber() - 1);
		}
		if (indentSearch.getPageSize() != null) {
			page.setPageSize(indentSearch.getPageSize());
		}

		if (params.length() > 0) {

			searchQuery = searchQuery.replace(":condition",
					" where indent.isdeleted is not true and " + params.toString() + " " + conditions);

		} else

			searchQuery = searchQuery.replace(":condition", "  " + conditions);

		searchQuery = searchQuery.replace(":orderby", orderBy);// orderBy
		System.out.println(searchQuery);
		page = (Pagination<Indent>) getPagination(searchQuery, page, paramValues);
		searchQuery = searchQuery + " :pagination";

		searchQuery = searchQuery.replace(":pagination",
				"limit " + page.getPageSize() + " offset " + page.getOffset() * page.getPageSize());

		// BeanPropertyRowMapper row = new BeanPropertyRowMapper(Indent.class);
		IndentRowMapper row = new IndentRowMapper();
		List<Indent> indents = namedParameterJdbcTemplate.query(searchQuery.toString(), paramValues, row);

		page.setTotalResults(indents.size());

		List<String> indentNumbers = new ArrayList<>();

		// List<IndentDetailEntity> find =
		// indentDetailJdbcRepository.find(indentNumbers,indentSearch.getTenantId());

		page.setPagedData(indents);

		return page;
	}

	public List<String> searchcreatorlist(String tenantId) {

		String searchQuery = "select distinct indentcreatedby from indent :condition ";

		Map<String, Object> paramValues = new HashMap<>();
		StringBuffer params = new StringBuffer();
		String conditions = "";
		if (tenantId != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("indent.tenantId =:tenantId");
			paramValues.put("tenantId", tenantId);
		}

		if (params.length() > 0) {

			searchQuery = searchQuery.replace(":condition",
					" where indent.isdeleted is not true and " + params.toString() + " " + conditions);

		} else
			searchQuery = searchQuery.replace(":condition", "  " + conditions);

		System.out.println(searchQuery);
		List<String> users = namedParameterJdbcTemplate.query(searchQuery.toString(), paramValues,
				new RowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum) throws SQLException {
						return rs.getString(1);
					}
				});
		return users.stream().filter(t -> t != null).collect(Collectors.toList());

	}

}