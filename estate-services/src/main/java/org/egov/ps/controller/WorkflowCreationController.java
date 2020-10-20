package org.egov.ps.controller;

import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.service.WorkflowCreationService;
import org.egov.ps.web.contracts.WorkFlowResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workflowcreation")
public class WorkflowCreationController {
	
	@Autowired
	private WorkflowCreationService workflowCreationService;
	
	@PostMapping("/_create")
	public List<WorkFlowResponseDetails> create(@Valid @RequestBody RequestInfo requestInfo) throws Exception {
		return workflowCreationService.createWorkflows(requestInfo);
	}

}