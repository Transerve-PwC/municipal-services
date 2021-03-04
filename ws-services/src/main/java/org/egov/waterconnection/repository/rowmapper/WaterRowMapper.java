package org.egov.waterconnection.repository.rowmapper;

import org.apache.commons.lang3.StringUtils;
import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.model.*;
import org.egov.waterconnection.model.Connection.StatusEnum;
import org.egov.waterconnection.model.enums.Status;
import org.egov.waterconnection.model.workflow.ProcessInstance;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WaterRowMapper implements ResultSetExtractor<List<WaterConnection>> {

	@Override
	public List<WaterConnection> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, WaterConnection> connectionListMap = new HashMap<>();
		WaterConnection currentWaterConnection = new WaterConnection();
		while (rs.next()) {
			String applicationNo = rs.getString("app_applicationno");
			if (connectionListMap.getOrDefault(applicationNo, null) == null) {
				currentWaterConnection = new WaterConnection();
				currentWaterConnection.setTenantId(rs.getString("tenantid"));
				currentWaterConnection.setConnectionCategory(rs.getString("connectionCategory"));
				currentWaterConnection.setConnectionType(rs.getString("connectionType"));
				currentWaterConnection.setWaterSource(rs.getString("waterSource"));
				currentWaterConnection.setMeterId(rs.getString("meterId"));
				currentWaterConnection.setMeterInstallationDate(rs.getLong("meterInstallationDate"));
				currentWaterConnection.setId(rs.getString("connection_Id"));
				
				/*currentWaterConnection.setApplicationNo(rs.getString("applicationNo"));
				currentWaterConnection.setApplicationStatus(rs.getString("applicationstatus"));
				currentWaterConnection.processInstance(ProcessInstance.builder().action((rs.getString("action"))).build());*/
				
				currentWaterConnection.setApplicationNo(rs.getString("app_applicationno"));
				currentWaterConnection.setApplicationStatus(rs.getString("app_applicationstatus"));
				currentWaterConnection.processInstance(ProcessInstance.builder().action((rs.getString("app_action"))).build());
				
				currentWaterConnection.setStatus(StatusEnum.fromValue(rs.getString("status")));
				currentWaterConnection.setConnectionNo(rs.getString("connectionNo"));
				currentWaterConnection.setOldConnectionNo(rs.getString("oldConnectionNo"));
				currentWaterConnection.setPipeSize(rs.getString("pipeSize"));
				currentWaterConnection.setNoOfTaps(rs.getInt("noOfTaps"));
				currentWaterConnection.setProposedPipeSize(rs.getString("proposedPipeSize"));
				currentWaterConnection.setProposedTaps(rs.getInt("proposedTaps"));
				currentWaterConnection.setWaterApplicationType(rs.getString("waterApplicationType"));
				currentWaterConnection.setSecurityCharge(rs.getDouble("securityCharge"));
				currentWaterConnection.setConnectionUsagesType(rs.getString("connectionusagestype"));
				currentWaterConnection.setInWorkflow(rs.getBoolean("inWorkflow"));
				currentWaterConnection.setActivityType(rs.getString("app_activitytype"));
				currentWaterConnection.setRoadCuttingArea(rs.getFloat("roadcuttingarea"));
				currentWaterConnection.setRoadType(rs.getString("roadtype"));
				currentWaterConnection.setPropertyId(rs.getString("property_id"));
				currentWaterConnection.setConnectionExecutionDate(rs.getLong("connectionExecutionDate"));
				currentWaterConnection.setApplicationType(rs.getString("applicationType"));
                currentWaterConnection.setDateEffectiveFrom(rs.getLong("dateEffectiveFrom"));
                currentWaterConnection.setLedgerNo(rs.getString("ledger_no"));
                currentWaterConnection.setDiv(rs.getString("div"));
                currentWaterConnection.setSubdiv(rs.getString("subdiv"));
                currentWaterConnection.setCcCode(rs.getString("cccode"));
                currentWaterConnection.setBillGroup(rs.getString("billGroup"));
                currentWaterConnection.setContractValue(rs.getString("contract_value"));
                currentWaterConnection.setProposedUsageCategory(rs.getString("proposedUsage_category"));

                currentWaterConnection.setLedgerGroup(rs.getString("ledgerGroup"));
                currentWaterConnection.setMeterCount(rs.getString("meterCount"));
                currentWaterConnection.setMeterRentCode(rs.getString("meterRentCode"));
                currentWaterConnection.setMfrCode(rs.getString("mfrCode"));
                currentWaterConnection.setMeterDigits(rs.getString("meterDigits"));
                currentWaterConnection.setMeterUnit(rs.getString("meterUnit"));
                currentWaterConnection.setSanctionedCapacity(rs.getString("sanctionedCapacity"));
                
				HashMap<String, Object> additionalDetails = new HashMap<>();
				additionalDetails.put(WCConstants.ADHOC_PENALTY, rs.getBigDecimal("adhocpenalty"));
				additionalDetails.put(WCConstants.ADHOC_REBATE, rs.getBigDecimal("adhocrebate"));
				additionalDetails.put(WCConstants.ADHOC_PENALTY_REASON, rs.getString("adhocpenaltyreason"));
				additionalDetails.put(WCConstants.ADHOC_PENALTY_COMMENT, rs.getString("adhocpenaltycomment"));
				additionalDetails.put(WCConstants.ADHOC_REBATE_REASON, rs.getString("adhocrebatereason"));
				additionalDetails.put(WCConstants.ADHOC_REBATE_COMMENT, rs.getString("adhocrebatecomment"));
				additionalDetails.put(WCConstants.INITIAL_METER_READING_CONST, rs.getBigDecimal("initialmeterreading"));
				additionalDetails.put(WCConstants.APP_CREATED_DATE, rs.getBigDecimal("appCreatedDate"));
				additionalDetails.put(WCConstants.DETAILS_PROVIDED_BY, rs.getString("detailsprovidedby"));
				additionalDetails.put(WCConstants.ESTIMATION_FILESTORE_ID, rs.getString("estimationfileStoreId"));
				additionalDetails.put(WCConstants.SANCTION_LETTER_FILESTORE_ID, rs.getString("sanctionfileStoreId"));
				additionalDetails.put(WCConstants.ESTIMATION_DATE_CONST, rs.getBigDecimal("estimationLetterDate"));
				currentWaterConnection.setAdditionalDetails(additionalDetails);
				
				AuditDetails auditdetails = AuditDetails.builder()
	                        .createdBy(rs.getString("ws_createdBy"))
	                        .createdTime(rs.getLong("ws_createdTime"))
	                        .lastModifiedBy(rs.getString("ws_lastModifiedBy"))
	                        .lastModifiedTime(rs.getLong("ws_lastModifiedTime"))
	                        .build();
				 currentWaterConnection.setAuditDetails(auditdetails);
				 
				 
				 String applicationId=rs.getString("application_id");
				if (!StringUtils.isEmpty(applicationId)) {
					WaterApplication app = new WaterApplication();
					app.setId(rs.getString("application_id"));
					app.setApplicationNo(rs.getString("app_applicationno"));
					app.setActivityType(rs.getString("app_activitytype"));
					app.setApplicationStatus(rs.getString("app_applicationstatus"));
					app.setAction(rs.getString("app_action"));
					app.setComments(rs.getString("app_comments"));
					app.setIsFerruleApplicable(rs.getBoolean("app_ferrule"));
					app.setSecurityCharge(rs.getDouble("app_securitycharge"));
					
					AuditDetails auditdetails1 = AuditDetails.builder()
		                    .createdBy(rs.getString("app_createdBy"))
		                    .createdTime(rs.getLong("app_createdTime"))
		                    .lastModifiedBy(rs.getString("app_lastModifiedBy"))
		                    .lastModifiedTime(rs.getLong("app_lastModifiedTime"))
		                    .build();
					app.setAuditDetails(auditdetails1);
				 
					currentWaterConnection.setWaterApplication(app);
				}
					
				String waterpropertyid=rs.getString("waterpropertyid");
				if (!StringUtils.isEmpty(waterpropertyid)) {
					WaterProperty property = new WaterProperty();
					property.setId(rs.getString("waterpropertyid"));
					property.setUsageCategory(rs.getString("usagecategory"));
					property.setUsageSubCategory(rs.getString("usagesubcategory"));
					currentWaterConnection.setWaterProperty(property);
				}
				 
				connectionListMap.put(applicationNo, currentWaterConnection);
			}
			addChildrenToProperty(rs, currentWaterConnection);
		}
		return new ArrayList<>(connectionListMap.values());
	}


    private void addChildrenToProperty(ResultSet rs, WaterConnection waterConnection) throws SQLException {
        addDocumentToWaterConnection(rs, waterConnection);
        addPlumberInfoToWaterConnection(rs, waterConnection);
        addHoldersDeatilsToWaterConnection(rs, waterConnection);
    }

    private void addDocumentToWaterConnection(ResultSet rs, WaterConnection waterConnection) throws SQLException {
        String document_Id = rs.getString("doc_Id");
        String isActive = rs.getString("doc_active");
        boolean documentActive = false;
        if (!StringUtils.isEmpty(isActive)) {
            documentActive = Status.ACTIVE.name().equalsIgnoreCase(isActive);
        }
        if (!StringUtils.isEmpty(document_Id) && documentActive) {
            Document applicationDocument = new Document();
            applicationDocument.setId(document_Id);
            applicationDocument.setDocumentType(rs.getString("documenttype"));
            applicationDocument.setFileStoreId(rs.getString("filestoreid"));
            applicationDocument.setDocumentUid(rs.getString("doc_Id"));
            applicationDocument.setStatus(org.egov.waterconnection.model.Status.fromValue(isActive));
            waterConnection.addDocumentsItem(applicationDocument);
        }
    }

    private void addPlumberInfoToWaterConnection(ResultSet rs, WaterConnection waterConnection) throws SQLException {
        String plumber_id = rs.getString("plumber_id");
        if (!StringUtils.isEmpty(plumber_id)) {
            PlumberInfo plumber = new PlumberInfo();
            plumber.setId(plumber_id);
            plumber.setName(rs.getString("plumber_name"));
            plumber.setGender(rs.getString("plumber_gender"));
            plumber.setLicenseNo(rs.getString("licenseno"));
            plumber.setMobileNumber(rs.getString("plumber_mobileNumber"));
            plumber.setRelationship(rs.getString("relationship"));
            plumber.setCorrespondenceAddress(rs.getString("correspondenceaddress"));
            plumber.setFatherOrHusbandName(rs.getString("fatherorhusbandname"));
            waterConnection.addPlumberInfoItem(plumber);
        }
    }

    private void addHoldersDeatilsToWaterConnection(ResultSet rs, WaterConnection waterConnection) throws SQLException {
        String uuid = rs.getString("userid");
        List<ConnectionHolderInfo> connectionHolders = waterConnection.getConnectionHolders();
        if (!CollectionUtils.isEmpty(connectionHolders)) {
            for (ConnectionHolderInfo connectionHolderInfo : connectionHolders) {
                if (!StringUtils.isEmpty(connectionHolderInfo.getUuid()) && !StringUtils.isEmpty(uuid) && connectionHolderInfo.getUuid().equals(uuid))
                    return;
            }
        }
        if(!StringUtils.isEmpty(uuid)){
            Double holderShipPercentage = rs.getDouble("holdershippercentage");
            if (rs.wasNull()) {
                holderShipPercentage = null;
            }
            Boolean isPrimaryOwner = rs.getBoolean("isprimaryholder");
            if (rs.wasNull()) {
                isPrimaryOwner = null;
            }
            ConnectionHolderInfo connectionHolderInfo = ConnectionHolderInfo.builder()
                    .relationship(Relationship.fromValue(rs.getString("holderrelationship")))
                    .status(org.egov.waterconnection.model.Status.fromValue(rs.getString("holderstatus")))
                    .tenantId(rs.getString("holdertenantid")).ownerType(rs.getString("connectionholdertype"))
                    .isPrimaryOwner(isPrimaryOwner).uuid(uuid).name(rs.getString("holdername")).correspondenceAddress(rs.getString("holdercorrepondanceaddress")).build();
            waterConnection.addConnectionHolderInfo(connectionHolderInfo);
        }
    }
}
