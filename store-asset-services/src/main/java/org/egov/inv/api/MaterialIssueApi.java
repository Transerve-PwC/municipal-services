/**
 * NOTE: This class is auto generated by the swagger code generator program (2.2.3).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.egov.inv.api;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.egov.inv.model.ErrorRes;
import org.egov.inv.model.MaterialIssueRequest;
import org.egov.inv.model.MaterialIssueResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "org.egov.inv.codegen.languages.SpringCodegen", date = "2017-11-08T13:51:07.770Z")

@Api(value = "materailIssues", description = "the materailIssues API")
public interface MaterialIssueApi {

			@ApiOperation(value = "Create new indent issues.", notes = "This API is invoked when the issuing store issues materials against the indent raised in the system.", response = MaterialIssueResponse.class, tags = {"Indent Issue", })
			@ApiResponses(value = {
			@ApiResponse(code = 200, message = "IndentIssue created Successfully", response = MaterialIssueResponse.class),
			@ApiResponse(code = 400, message = "Invalid Input", response = ErrorRes.class) })
	@RequestMapping(value = "/materialissues/_create", produces = {
			"application/json" }, consumes = {"application/json" }, method = RequestMethod.POST)
	ResponseEntity<MaterialIssueResponse> materialIssueCreatePost(
			@NotNull @ApiParam(value = "Unique id for a tenant.", required = true) 
			@RequestParam(value = "tenantId", required = true) String tenantId,
			@ApiParam(value = "Create  new") @Valid @RequestBody MaterialIssueRequest indentIssueRequest);
			@ApiOperation(value = "Prepare Material Issues From Indents.", notes = "This API is invoked when we create material issues from indent.", response = MaterialIssueResponse.class, tags = {
			"Indent Issue", })
			@ApiResponses(value = {
			@ApiResponse(code = 200, message = "IndentIssue created Successfully", response = MaterialIssueResponse.class),
			@ApiResponse(code = 400, message = "Invalid Input", response = ErrorRes.class) })

	@RequestMapping(value = "/materialissues/_preparemifromindents", produces = {
			"application/json" }, consumes = {
					"application/json" }, method = RequestMethod.POST)
	ResponseEntity<MaterialIssueResponse> materiallIssuesPreparemiFromIndents(
			@NotNull @ApiParam(value = "Unique id for a tenant.", required = true) 
			@RequestParam(value = "tenantId", required = true) String tenantId,
			@ApiParam(value = "Create  new") 
			@Valid @RequestBody MaterialIssueRequest indentIssueRequest);
			@ApiOperation(value = "Get the list of indent issues.", notes = "This API is used to search the issues incurred for the indents raised in the system.", response = MaterialIssueResponse.class, tags = {"Indent Issue", })
			@ApiResponses(value = {
			@ApiResponse(code = 200, message = "IndentIssue retrieved Successfully", response = MaterialIssueResponse.class),
			@ApiResponse(code = 400, message = "Invalid Input", response = ErrorRes.class) })

	 @RequestMapping(value = "/materialissues/_search",
     produces = { "application/json" }, 
     consumes = { "application/json" },
     method = RequestMethod.POST)
 ResponseEntity<MaterialIssueResponse> materialIssueSearchPost( @NotNull@ApiParam(value = "Unique id for a tenant.", required = true) @RequestParam(value = "tenantId", required = true) String tenantId,@ApiParam(value = "Parameter to carry Request metadata in the request body"  )  @Valid @RequestBody org.egov.common.contract.request.RequestInfo requestInfo, @Size(max=50)@ApiParam(value = "comma seperated list of Ids") @RequestParam(value = "ids", required = false) List<String> ids, @ApiParam(value = "issuing store of the MaterialIssue ") @RequestParam(value = "fromStore", required = false) String fromStore,
	        @ApiParam(value = "receiving store of the MaterialIssue ") @RequestParam(value = "toStore", required = false) String toStore,@ApiParam(value = "issueNoteNumber  Auto generated number, read only ") @RequestParam(value = "issueNoteNumber", required = false) String issueNoteNumber,@ApiParam(value = "issue date of the MaterialIssue ") @RequestParam(value = "issueDate", required = false) Long issueDate,@ApiParam(value = "material issue status of the MaterialIssue ", allowableValues = "CREATED, APPROVED, REJECTED, CANCELED") @RequestParam(value = "materialIssueStatus", required = false) String materialIssueStatus,@ApiParam(value = "description of the MaterialIssue ") @RequestParam(value = "description", required = false) String description,			@ApiParam(value = "issue purpose status of the materialissue ") @RequestParam(value = "issuePurpose", required = false) String issuePurpose,
	        @ApiParam(value = "total issue value of the MaterialIssue ") @RequestParam(value = "totalIssueValue", required = false) BigDecimal totalIssueValue, @Min(0) @Max(100)@ApiParam(value = "Number of records returned.", defaultValue = "20") @RequestParam(value = "pageSize", required = false, defaultValue="20") Integer pageSize,@ApiParam(value = "Page number", defaultValue = "1") @RequestParam(value = "pageNumber", required = false, defaultValue="1") Integer pageNumber,@ApiParam(value = "This takes any field from the Object seperated by comma and asc,desc keywords. example name asc,code desc or name,code or name,code desc", defaultValue = "id") @RequestParam(value = "sortBy", required = false) String sortBy, @ApiParam(value = "This takes purpose of issuesearch") @RequestParam(value = "purpose", required = false) String purpose);
			@ApiOperation(value = "Update any of the indent issues.", notes = "This API is invoked to update the indent material issue information and during the workflow as well.", response = MaterialIssueResponse.class, tags = {
			"Indent Issue", })
			@ApiResponses(value = {
			@ApiResponse(code = 200, message = "IndentIssue updated Successfully", response = MaterialIssueResponse.class),
			@ApiResponse(code = 400, message = "Invalid Input", response = ErrorRes.class) })

	@RequestMapping(value = "/materialissues/_update", produces = {
			"application/json" }, consumes = {
					"application/json" }, method = RequestMethod.POST)
	ResponseEntity<MaterialIssueResponse> materialIssueUpdatePost(
			@NotNull @ApiParam(value = "Unique id for a tenant.", required = true) @RequestParam(value = "tenantId", required = true) String tenantId,
			@ApiParam(value = "common Request info") @Valid @RequestBody MaterialIssueRequest indentIssueRequest);

}
