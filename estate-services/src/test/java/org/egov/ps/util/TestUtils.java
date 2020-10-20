package org.egov.ps.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.egov.common.contract.request.RequestInfo;

public class TestUtils {
    public static RequestInfo getRequestInfo() {
        ObjectMapper mapper = new ObjectMapper();
        String json = getFileContents("test/requestinfo.json");
        try {
            RequestInfo requestInfo;
            requestInfo = mapper.readValue(json, RequestInfo.class);
            return requestInfo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFileContents(String fileName) {

        try {
            return IOUtils.toString(TestUtils.class.getClassLoader().getResourceAsStream(fileName), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
