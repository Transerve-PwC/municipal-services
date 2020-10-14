package org.egov.integration.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.egov.integration.common.CommonConstants;
import org.egov.integration.model.PtMapping;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class PtMappingRowMapper implements ResultSetExtractor<JSONObject>{

	@Override
	public JSONObject extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, PtMapping> map = new HashMap<>();		
		JSONObject result = new JSONObject();
		JSONArray list = new JSONArray();		
		try {
			while (rs.next()) {
				String uuid = rs.getString("uuid");				
				if (!map.containsKey(uuid)) {									
					PtMapping ptMapping = PtMapping.builder()
							.userId(rs.getInt("user_id"))
							.propertyTaxId(rs.getString("property_tax_id"))
							.isActive(rs.getBoolean("is_active"))
							.build();
					map.put(uuid, ptMapping);					
					JSONObject pt = new JSONObject();
					pt.put("userId", ptMapping.getUserId());
					pt.put("propertyTaxId", ptMapping.getPropertyTaxId());
					pt.put("isActive", ptMapping.getIsActive());					
					list.add(pt);
				}
			}
		} catch (Exception e) {
			throw new CustomException(CommonConstants.PT_MAPPING_EXCEPTION_CODE, e.getMessage());
		}
		result.put("result", list);
		return result;
	}
}
