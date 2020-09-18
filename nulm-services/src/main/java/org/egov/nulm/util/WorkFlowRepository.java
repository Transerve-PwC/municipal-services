package org.egov.nulm.util;


import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.workflow.model.ProcessInstanceRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class WorkFlowRepository {



    private RestTemplate restTemplate;

    private NULMConfiguration config;
    
    private ObjectMapper objectMapper;

    @Autowired
    public WorkFlowRepository(RestTemplate restTemplate, NULMConfiguration config,ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.objectMapper=objectMapper;
    }


	public String createWorkflowRequest(ProcessInstanceRequest workflowRequest) throws IOException {
		String url = config.getWorkFlowHost() + config.getWorkFlowPath();
		String response=null;
		ResponseInfo responseInfo = null;
		try {
			 response = restTemplate.postForObject(url, workflowRequest, String.class);
		} catch (HttpClientErrorException e) {

			/*
			 * extracting message from client error exception
			 */
			DocumentContext responseContext = JsonPath.parse(e.getResponseBodyAsString());
			List<Object> errros = null;
			try {
				errros = responseContext.read("$.Errors");
			} catch (PathNotFoundException pnfe) {
				log.error("EG_NUL,_WF_ERROR_KEY_NOT_FOUND",
						" Unable to read the json path in error object : " + pnfe.getMessage());
				throw new CustomException("EG_NULM_WF_ERROR_KEY_NOT_FOUND",
						" Unable to read the json path in error object : " + pnfe.getMessage());
			}
			throw new CustomException("EG_WF_ERROR", errros.toString());
		} catch (Exception e) {
			throw new CustomException("EG_WF_ERROR",
					" Exception occured while integrating with workflow : " + e.getMessage());
		}

		return response;
	}


}
