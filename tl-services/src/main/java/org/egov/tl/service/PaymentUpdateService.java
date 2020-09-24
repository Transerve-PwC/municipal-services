package org.egov.tl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tl.config.TLConfiguration;
import org.egov.tl.repository.TLRepository;
import org.egov.tl.util.TradeUtil;
import org.egov.tl.util.CTLConstants;
import org.egov.tl.util.NotificationUtil;
import org.egov.tl.web.models.EmailRequest;
import org.egov.tl.web.models.OwnerInfo;
import org.egov.tl.web.models.SMSRequest;
import org.egov.tl.web.models.TradeLicense;
import org.egov.tl.web.models.TradeLicenseRequest;
import org.egov.tl.web.models.TradeLicenseSearchCriteria;
import org.egov.tl.web.models.collection.PaymentDetail;
import org.egov.tl.web.models.collection.PaymentRequest;
import org.egov.tl.web.models.workflow.BusinessService;
import org.egov.tl.workflow.WorkflowIntegrator;
import org.egov.tl.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.egov.tl.util.TLConstants.*;
import static org.egov.tl.util.CTLConstants.*;


@Service
@Slf4j
public class PaymentUpdateService {

	private TradeLicenseService tradeLicenseService;

	private TLConfiguration config;

	private TLRepository repository;

	private WorkflowIntegrator wfIntegrator;

	private EnrichmentService enrichmentService;

	private ObjectMapper mapper;

	private WorkflowService workflowService;

	private TradeUtil util;
	
	private NotificationUtil notificationUtil;

	@Value("${workflow.bpa.businessServiceCode.fallback_enabled}")
	private Boolean pickWFServiceNameFromTradeTypeOnly;
	
	@Value("${egov.allowed.businessServices}")
	private String allowedBusinessServices;

	@Autowired
	public PaymentUpdateService(TradeLicenseService tradeLicenseService, TLConfiguration config, TLRepository repository,
								WorkflowIntegrator wfIntegrator, EnrichmentService enrichmentService, ObjectMapper mapper,
								WorkflowService workflowService,TradeUtil util, NotificationUtil notificationUtil) {
		this.tradeLicenseService = tradeLicenseService;
		this.config = config;
		this.repository = repository;
		this.wfIntegrator = wfIntegrator;
		this.enrichmentService = enrichmentService;
		this.mapper = mapper;
		this.workflowService = workflowService;
		this.util = util;
		this.notificationUtil = notificationUtil;
	}




	final String tenantId = "tenantId";

	final String businessService = "businessService";

	final String consumerCode = "consumerCode";

	/**
	 * Process the message from kafka and updates the status to paid
	 * 
	 * @param record The incoming message from receipt create consumer
	 */
	public void process(HashMap<String, Object> record) {

		try {
			PaymentRequest paymentRequest = mapper.convertValue(record,PaymentRequest.class);
			RequestInfo requestInfo = paymentRequest.getRequestInfo();
			List<PaymentDetail> paymentDetails = paymentRequest.getPayment().getPaymentDetails();
			String tenantId = paymentRequest.getPayment().getTenantId();
			List<String> allowedservices = Arrays.asList(allowedBusinessServices.split(","));
			for(PaymentDetail paymentDetail : paymentDetails){
		        if (allowedservices.contains(paymentDetail.getBusinessService())) {
					TradeLicenseSearchCriteria searchCriteria = new TradeLicenseSearchCriteria();
					searchCriteria.setTenantId(tenantId);
					searchCriteria.setApplicationNumber(paymentDetail.getBill().getConsumerCode());
					searchCriteria.setBusinessService(paymentDetail.getBusinessService());
					List<TradeLicense> licenses = tradeLicenseService.getLicensesWithOwnerInfo(searchCriteria, requestInfo);
					String wfbusinessServiceName = null;
					switch (paymentDetail.getBusinessService()) {
						case businessService_REHRI_RC:
						case businessService_REHRI_DL:
						case businessService_DHOBI_GHAT:
						case businessService_BOOK_SHOP:
						case businessService_TL:
							wfbusinessServiceName = config.getTlBusinessServiceValue();
							break;

						case businessService_BPA:
							String tradeType = licenses.get(0).getTradeLicenseDetail().getTradeUnits().get(0).getTradeType();
							if (pickWFServiceNameFromTradeTypeOnly)
								tradeType = tradeType.split("\\.")[0];
							wfbusinessServiceName = tradeType;
							break;
					}
				BusinessService businessService = workflowService.getBusinessService(licenses.get(0).getTenantId(), requestInfo,wfbusinessServiceName);


					if (CollectionUtils.isEmpty(licenses))
						throw new CustomException("INVALID RECEIPT",
								"No tradeLicense found for the comsumerCode " + searchCriteria.getApplicationNumber());

					licenses.forEach(license -> license.setAction(ACTION_PAY));

					// FIXME check if the update call to repository can be avoided
					// FIXME check why aniket is not using request info from consumer
					// REMOVE SYSTEM HARDCODING AFTER ALTERING THE CONFIG IN WF FOR TL

					Role role = Role.builder().code("SYSTEM_PAYMENT").tenantId(licenses.get(0).getTenantId()).build();
					requestInfo.getUserInfo().getRoles().add(role);
					TradeLicenseRequest updateRequest = TradeLicenseRequest.builder().requestInfo(requestInfo)
							.licenses(licenses).build();

					/*
					 * calling workflow to update status
					 */
					wfIntegrator.callWorkFlow(updateRequest);

					updateRequest.getLicenses()
							.forEach(obj -> log.info(" the status of the application is : " + obj.getStatus()));

					List<String> endStates = Collections.nCopies(updateRequest.getLicenses().size(), STATUS_APPROVED);
					switch (paymentDetail.getBusinessService()) {
						case businessService_BPA:
							endStates = util.getBPAEndState(updateRequest);
							break;
					}
					enrichmentService.postStatusEnrichment(updateRequest,endStates);

					/*
					 * calling repository to update the object in TL tables
					 */
					Map<String,Boolean> idToIsStateUpdatableMap = util.getIdToIsStateUpdatableMap(businessService,licenses);
					repository.update(updateRequest,idToIsStateUpdatableMap);
					
					try {
						notifyUser(updateRequest);						
					} catch (Exception exception) {
						log.error("Failed to notify users after with the certificate number.");
					}
			}
		 }
		} catch (Exception e) {
			log.error("Error while processing the payment ",e);
		}

	}
	 
	private void notifyUser(TradeLicenseRequest updateRequest) {
		List<TradeLicense> licenses = updateRequest.getLicenses();
		licenses.parallelStream().forEach(license -> {
			Optional<OwnerInfo> filteredOwnerInfo = license.getTradeLicenseDetail().getOwners().stream()
	        		.filter(o -> o!=null && o.getMobileNumber() != null && o.getName() != null)
	        		.findFirst();
	        
	        if (!filteredOwnerInfo.isPresent()) {
	        	return;
	        }
	        
	        OwnerInfo owner = filteredOwnerInfo.get();	
	        String ctlLocalizationMessages = notificationUtil.getLocalizationMessages(license.getTenantId(), updateRequest.getRequestInfo());
	        
	        //Post an SMS request.
	        SMSRequest smsRequest = getCTLOwnerSMSRequest(license, owner, ctlLocalizationMessages);
	        notificationUtil.sendSMS(Arrays.asList(smsRequest), config.getIsTLSMSEnabled());
	        
	        if (owner.getEmailId() != null) {
	        	EmailRequest emailRequest = getCTLOwnerEmailRequest(license, owner, ctlLocalizationMessages);
	        	String emailSignature = notificationUtil.getMessageTemplate(CTLConstants.EMAIL_SIGNATURE, ctlLocalizationMessages);
	        	notificationUtil.sendEMAIL(Arrays.asList(emailRequest),true, emailSignature);
	        }
		});
		

	}
    /**
     * Creates SMSRequest for the owners
     * @param license The tradeLicense for which the receipt is created
     * @param owner The owner who we will be receiving SMS notifications
     * @param localizationMessages The localization message to be sent
     * @return The list of the SMS Requests
     */
    private SMSRequest getCTLOwnerSMSRequest(TradeLicense license, OwnerInfo owner, String localizationMessages){
        String message = notificationUtil.getCTLOwnerPaymentMsg(license, new HashMap<String, String>(), localizationMessages,CTLConstants.SMS_NOTIFICATION);
        String customizedMsg = message.replace("<1>", owner.getName());
        SMSRequest smsRequest = new SMSRequest(owner.getMobileNumber(),customizedMsg);
        return smsRequest;
    }


    /**
     * Creates SMSRequest for the owners
     * @param license The tradeLicense for which the receipt is created
     * @param owner The owner who will be receiving email notifications
     * @param localizationMessages The localization message to be sent
     * @return The list of the SMS Requests
     */
    private EmailRequest getCTLOwnerEmailRequest(TradeLicense license, OwnerInfo owner,String localizationMessages){
        String message = notificationUtil.getCTLOwnerPaymentMsg(license, new HashMap<String, String>(), localizationMessages,CTLConstants.MAIL_NOTIFICATION);
        
        String customizedMsg = message.replace("<1>", owner.getName());
        EmailRequest emailRequest = EmailRequest.builder()
        								.subject(EMAIL_SUBJECT)
        								.isHTML(true)
        								.email(owner.getEmailId())
        								.body(customizedMsg)
        								.build();
        return emailRequest;
    }
    
	/**
	 * Extracts the required fields as map
	 * 
	 * @param context The documentcontext of the incoming receipt
	 * @return Map containing values of required fields
	 */
	private Map<String, String> enrichValMap(DocumentContext context) {
		Map<String, String> valMap = new HashMap<>();
		try {
			valMap.put(businessService, context.read("$.Payments.*.paymentDetails[?(@.businessService=='TL')].businessService"));
			valMap.put(consumerCode, context.read("$.Payments.*.paymentDetails[?(@.businessService=='TL')].bill.consumerCode"));
			valMap.put(tenantId, context.read("$.Payments[0].tenantId"));
		} catch (Exception e) {
			log.error("Error while reading document context ",e);
			throw new CustomException("PAYMENT ERROR", "Unable to fetch values from payment");
		}
		return valMap;
	}

}
