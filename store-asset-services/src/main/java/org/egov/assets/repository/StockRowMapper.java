package org.egov.assets.repository;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.commons.lang3.Range;
import org.egov.assets.model.StockModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StockRowMapper implements RowMapper<JSONArray> {
	JSONArray mainOutput = new JSONArray();

	@Autowired
	private ObjectMapper mapper;
	@Override
	public JSONArray mapRow(ResultSet resultSet, int i) throws SQLException {
		JSONObject sunMain = new JSONObject();
		sunMain.put("srNo", i + 1);
		sunMain.put("unitrate", new BigDecimal(resultSet.getString("unitrate")));
		
		sunMain.put("0-90daysQty", new BigDecimal(resultSet.getString("below90Days")));
		sunMain.put("0-90daysStockvalue",new BigDecimal(resultSet.getString("below90Days")).multiply(new BigDecimal(resultSet.getString("unitrate"))));
		 
		sunMain.put("90-180daysQty", new BigDecimal(resultSet.getString("between90to180Days")));
	    sunMain.put("90-180daysStockvalue",new BigDecimal(resultSet.getString("between90to180Days")).multiply(new BigDecimal(resultSet.getString("unitrate"))));
			
		sunMain.put("181 and aboveQty",new BigDecimal( resultSet.getString("above180Days")));
	 	sunMain.put("181 and aboveStockvalue",new BigDecimal(resultSet.getString("above180Days")).multiply(new BigDecimal(resultSet.getString("unitrate"))));

	 	sunMain.put("totalQty",  new BigDecimal(resultSet.getString("below90Days")).add(new BigDecimal(resultSet.getString("between90to180Days"))).add(new BigDecimal(resultSet.getString("above180Days"))));
		sunMain.put("totalStackValue",new BigDecimal(resultSet.getString("below90Days")).multiply(new BigDecimal(resultSet.getString("unitrate"))).add(new BigDecimal(resultSet.getString("between90to180Days")).multiply(new BigDecimal(resultSet.getString("unitrate"))))
				.add(new BigDecimal(resultSet.getString("above180Days")).multiply(new BigDecimal(resultSet.getString("unitrate")))));
		mainOutput.add(sunMain);
		return mainOutput;
	}
	
}
