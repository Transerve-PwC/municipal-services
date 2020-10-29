package org.egov.assets.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.assets.common.JdbcRepository;
import org.egov.assets.common.Pagination;
import org.egov.assets.model.Scrap;
import org.egov.assets.model.ScrapSearch;
import org.egov.assets.repository.entity.ScrapEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;
@Repository
public class ScrapJdbcRepository extends JdbcRepository{
	
	
	 public Pagination<Scrap> search(ScrapSearch scrapSearch) {
	        String searchQuery = "select * from scrap" +
	                " :condition :orderby";
	        StringBuffer params = new StringBuffer();
	        Map<String, Object> paramValues = new HashMap<>();

	        if (scrapSearch.getSortBy() != null && !scrapSearch.getSortBy().isEmpty()) {
	            validateSortByOrder(scrapSearch.getSortBy());
	            validateEntityFieldName(scrapSearch.getSortBy(), ScrapSearch.class);
	        }

	        String orderBy = "order by scrapNumber";

	        if (scrapSearch.getSortBy() != null && !scrapSearch.getSortBy().isEmpty()) {
	            orderBy = "order by " + scrapSearch.getSortBy();
	        }

	        if (scrapSearch.getIds() != null) {
	            if (params.length() > 0)
	                params.append(" and ");
	            params.append("id in (:ids)");
	            paramValues.put("ids", scrapSearch.getIds());
	        }

	        if (scrapSearch.getScrapNumber() != null) {
	            if (params.length() > 0)
	                params.append(" and ");
	            params.append("scrapNumber in (:scrapNumber)");
	            paramValues.put("scrapNumber", scrapSearch.getScrapNumber());
	        }

	        if (scrapSearch.getScrapStatus() != null) {
	            if (params.length() > 0)
	                params.append(" and ");
	            params.append("status = :scrapStatus");
	            paramValues.put("scrapStatus", scrapSearch.getScrapStatus());
	        }

	        
	        if (scrapSearch.getIssueNumber() != null) {
	            if (params.length() > 0)
	                params.append(" and ");
	            params.append("issuenumber = :issuenumber");
	            paramValues.put("issuenumber", scrapSearch.getIssueNumber());
	        }

	        if (scrapSearch.getTenantId() != null) {
	            if (params.length() > 0)
	                params.append(" and ");
	            params.append("tenantId = :tenantId");
	            paramValues.put("tenantId", scrapSearch.getTenantId());
	        }

	        Pagination<Scrap> page = new Pagination<>();
	        if (scrapSearch.getPageSize() != null)
	            page.setPageSize(scrapSearch.getPageSize());
	        if (scrapSearch.getOffset() != null)
	            page.setOffset(scrapSearch.getOffset());
	        if (params.length() > 0)
	            searchQuery = searchQuery.replace(":condition", " where " + params.toString());
	        else
	            searchQuery = searchQuery.replace(":condition", "");

	        searchQuery = searchQuery.replace(":orderby", orderBy);
	        page = (Pagination<Scrap>) getPagination(searchQuery, page, paramValues);

	        searchQuery = searchQuery + " :pagination";
	        searchQuery = searchQuery.replace(":pagination", "limit " + page.getPageSize() + " offset " + page.getOffset() * page.getPageSize());
	        BeanPropertyRowMapper row = new BeanPropertyRowMapper(ScrapEntity.class);

	        List<Scrap> scrap = new ArrayList<>();

	        List<ScrapEntity> scrapEntities = namedParameterJdbcTemplate
	                .query(searchQuery.toString(), paramValues, row);

	        for (ScrapEntity scrapEntity : scrapEntities) {

	        	scrap.add(scrapEntity.toDomain());
	        }

	        page.setTotalResults(scrap.size());

	        page.setPagedData(scrap);

	        return page;
	    }

}
