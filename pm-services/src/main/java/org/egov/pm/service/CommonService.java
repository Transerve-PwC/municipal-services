package org.egov.pm.service;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.pm.model.Errors;
import org.egov.pm.model.IdGenModel;
import org.egov.pm.model.IdGenRequestModel;
import org.egov.pm.wf.model.ProcessInstanceRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CommonService {

	@Autowired
	RestTemplate restTemplate;

	@Value("${egov.idgen.hostname}")
	private String host;

	@Value("${egov.idgen.uri}")
	private String path;

	@Value("${egov.wf.hostname}")
	private String workflowHost;

	@Value("${egov.wf.uri}")
	private String workflowPath;


	public String generateApplicationId(String idName, String tenantId) {

		String url = host + path;
		ObjectMapper objectMapper = new ObjectMapper();
		RequestInfo requestInfo = new RequestInfo();
		IdGenModel generatedValue = null;
		String applicationId = null;
		List<IdGenModel> idList = Arrays
				.asList(IdGenModel.builder().count(1).idName(idName).tenantId(tenantId).build());
		IdGenRequestModel mcq = new IdGenRequestModel();
		mcq.setRequestInfo(requestInfo);
		mcq.setIdRequests(idList);

		JsonNode response = restTemplate.postForObject(url, mcq, JsonNode.class).findValue("idResponses");

		if (!isNull(response) && response.isArray()) {

			for (JsonNode objNode : response) {
				try {
					generatedValue = objectMapper.treeToValue(objNode, IdGenModel.class);
					applicationId = generatedValue.getId();
				} catch (JsonProcessingException e) {
					log.error("Failed to fetch Id from Idegn", e);
					throw new CustomException("IDGEN_CREATION_FAILED", "Failed to fetch Id from Idegn");
				}
			}
		}

		return applicationId;
	}

	public ResponseInfo createWorkflowRequest(ProcessInstanceRequest workflowRequest) throws IOException {
		String url = workflowHost + workflowPath;
		ResponseInfo responseInfo = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode response = restTemplate.postForObject(url, workflowRequest, JsonNode.class);

			if (!isNull(response)) {
				responseInfo = objectMapper.convertValue(response.get("ResponseInfo"), ResponseInfo.class);
				log.info("Workflow Created Success : " + responseInfo.getMsgId());
			} else {
				log.info("Workflow Creation Failed : Reason " + response);
				throw new CustomException("WORKFLOW_EXCEPTION", "Server Down");
			}
		} catch (HttpStatusCodeException e) {
			log.debug(e.getResponseBodyAsString());
			Errors errors = objectMapper.readValue(e.getResponseBodyAsString(), Errors.class);
			throw new CustomException("WORKFLOW_EXCEPTION",
					errors == null ? "Server Down" : errors.getErrorList().get(0).getMessage());
		}
		return responseInfo;
	}
}
