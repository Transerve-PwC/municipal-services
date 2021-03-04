package org.egov.integration.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ColumnsRowMapper implements ResultSetExtractor<JSONArray> {
	@Override
	public JSONArray extractData(ResultSet rs) throws SQLException {

		JSONArray json = new JSONArray();
		ResultSetMetaData rsmd = rs.getMetaData();
		while (rs.next()) {
			int numColumns = rsmd.getColumnCount();
			JSONObject obj = new JSONObject();
			for (int i = 1; i <= numColumns; i++) {
				String column_name = rsmd.getColumnName(i);
				obj.put(column_name, (rs.getObject(column_name)==null?"":rs.getObject(column_name).toString()));
			}
			json.add(obj);
			
		}

		log.debug("converting map to list object ::: " + json);
		return json;
	}
}
