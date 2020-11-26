package org.egov.ps.consumer;

import java.util.HashMap;

import org.egov.ps.service.PaymentNotificationService;
import org.egov.ps.service.PaymentUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReceiptConsumer {

	@Autowired
	private PaymentUpdateService paymentUpdateService;
	
	@Autowired
	private PaymentNotificationService paymentNotificationService;

	@KafkaListener(topics = { "${kafka.topics.receipt.create}" })
	public void listenPayments(final HashMap<String, Object> record) {
		paymentUpdateService.process(record);
		paymentNotificationService.process(record);
	}
}
