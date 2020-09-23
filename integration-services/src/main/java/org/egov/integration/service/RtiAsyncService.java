
package org.egov.integration.service;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.egov.integration.common.CommonConstants;
import org.egov.integration.model.RtiIndividualResponse;
import org.egov.integration.model.RtiRequest;
import org.egov.integration.model.RtiResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RtiAsyncService {

	@Autowired
	private RtiRestService rtiRestService;
	
	public RtiResponse get(RtiRequest rti, String token) {	
		CompletableFuture<ResponseEntity<RtiIndividualResponse>> cpiores = CompletableFuture.supplyAsync(() -> {
			return rtiRestService.getCpioResponse(rti, token);
		}).handle((res, ex) -> {
		    if(ex != null) {
		    	log.error("integration-services logs :: cpiores :: " + ex.getMessage());
		        return null;
		    }
		    return res;
		}).thenApplyAsync(result -> {
			if(!StringUtils.isEmpty(result.getBody().getTransactionNo())){
				RtiIndividualResponse res=RtiIndividualResponse.builder().transactionNo(result.getBody().getTransactionNo()).build();
				rtiRestService.confirmTransaction(res,token);
			}
		    return result;
		});
		
		CompletableFuture<ResponseEntity<RtiIndividualResponse>> nodalres = CompletableFuture.supplyAsync(() -> {
			return rtiRestService.getNodalResponse(rti, token);
		}).handle((res, ex) -> {
		    if(ex != null) {
		    	log.error("integration-services logs :: nodalres :: " + ex.getMessage());
		        return null;
		    }
		    return res;
		}).thenApplyAsync(result -> {
			if(!StringUtils.isEmpty(result.getBody().getTransactionNo())){
				RtiIndividualResponse res=RtiIndividualResponse.builder().transactionNo(result.getBody().getTransactionNo()).build();
				rtiRestService.confirmTransaction(res,token);
			}
		    return result;
		});
		
		CompletableFuture<ResponseEntity<RtiIndividualResponse>> appellateres = CompletableFuture.supplyAsync(() -> {
			return rtiRestService.getNodalResponse(rti, token);
		}).handle((res, ex) -> {
		    if(ex != null) {
		    	log.error("integration-services logs :: appellateres :: " + ex.getMessage());
		        return null;
		    }
		    return res;
		}).thenApplyAsync(result -> {
			if(!StringUtils.isEmpty(result.getBody().getTransactionNo())){
				RtiIndividualResponse res=RtiIndividualResponse.builder().transactionNo(result.getBody().getTransactionNo()).build();
				rtiRestService.confirmTransaction(res,token);
			}
		    return result;
		});
		
        CompletableFuture.allOf(cpiores, nodalres, appellateres).join();
        
        RtiResponse response = new RtiResponse();
		try {
			if(null!=cpiores)
				response.setCpio(cpiores.get().getBody());
			if(null!=nodalres)
				response.setNodal(nodalres.get().getBody());
			if(null!=appellateres)
				response.setAppellate(appellateres.get().getBody());
		} catch (InterruptedException | ExecutionException e) {					
			throw new CustomException(CommonConstants.RTI_EXCEPTION_CODE, e.getMessage());
		}
		
		return response;
	}
}