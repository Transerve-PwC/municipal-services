package org.egov.integration.service;


import org.egov.common.contract.response.ResponseInfo;
import org.egov.integration.common.CommonConstants;
import org.egov.integration.config.ApiConfiguration;
import org.egov.integration.model.HrmsRequestInfoWrapper;
import org.egov.integration.model.ResponseInfoWrapper;
import org.egov.integration.util.AES128Bit;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.tempuri.Payslip;
import org.tempuri.PayslipSoap;

@Component
public class PayslipClient extends WebServiceGatewaySupport {

	
	@Autowired
	private ApiConfiguration config;

	public ResponseEntity<ResponseInfoWrapper> fetchPayslip(HrmsRequestInfoWrapper request){

		String empCode = AES128Bit.doEncryptedAES(request.getHrmsRequest().getEmpCode(), config.getEncrptionKey());

		String month = AES128Bit.doEncryptedAES(request.getHrmsRequest().getMonth(), config.getEncrptionKey());

		String year = AES128Bit.doEncryptedAES(request.getHrmsRequest().getYear(), config.getEncrptionKey());
		
        Payslip payslip = new Payslip();
        PayslipSoap payslipsoap =  payslip.getPayslipSoap();
        String response  = payslipsoap.getPaySlip(empCode, month, year);
        JSONObject xmlJSONObj = XML.toJSONObject(AES128Bit.doDecryptedAES(response, config.getEncrptionKey()));
        return new ResponseEntity<>(ResponseInfoWrapper.builder()
				.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
				.responseBody(xmlJSONObj.toMap()).build(), HttpStatus.OK);
    }
	
}
