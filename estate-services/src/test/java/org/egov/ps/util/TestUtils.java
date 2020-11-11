package org.egov.ps.util;

import java.io.IOException;
import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.http.MediaType;

public class TestUtils {

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

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
