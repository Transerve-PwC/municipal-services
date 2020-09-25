package org.egov.pm.service.notification.consumer;

import java.io.IOException;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.pm.model.EmailRequest;
import org.egov.pm.model.EmailTemplateModel;
import org.egov.pm.model.NOCApplication;
import org.egov.pm.model.NOCRequestData;
import org.egov.pm.model.SMSRequest;
import org.egov.pm.model.User;
import org.egov.pm.producer.Producer;
import org.egov.pm.repository.NocRepository;
import org.egov.pm.util.CommonConstants;
import org.egov.pm.util.UserUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailSmsNotificationListener {

	@Value("${kafka.topics.notification.mail.name}")
	private String emailtopic;

	@Value("${kafka.topics.notification.sms}")
	private String smstopic;

	private ObjectMapper objectMapper;
	private NocRepository nocRepository;
	private UserUtil userUtil;
	private Producer producer;

	@Autowired
	public EmailSmsNotificationListener(ObjectMapper objectMapper, NocRepository nocRepository, UserUtil userUtil,
			Producer producer) {
		this.objectMapper = objectMapper;
		this.nocRepository = nocRepository;
		this.userUtil = userUtil;
		this.producer = producer;
	}

	@KafkaListener(topics = "${persister.notification.noc.topic}")
	public void invitationsNew(ConsumerRecord<String, Object> data) throws IOException {
		try {
			log.info("Send Notofication Kafka Topic :{} ", data);
			NOCRequestData app1 = objectMapper.convertValue(data.value(), NOCRequestData.class);
			if (app1 == null || app1.getNocApplication().isEmpty())
				throw new CustomException("BAD REQUEST", "Email data nor found");

			NOCApplication application = app1.getNocApplication().get(0);

			Map<String, EmailTemplateModel> map = null;
			map = nocRepository.findTemplate(application.getApplicationStatus(), application.getTenantId(),
					application.getApplicationType());
			if (map != null) {
				log.info("roles are {}", map.keySet().toString());
				for (String role : map.keySet()) {

					if (role.equals("CITIZEN")) {
						enrichCitizenNotification(map, role, application, app1);
					} else {
						enrichEmployeeNotification(map, role, application, app1);
					}
				}
			}

		} catch (Exception e) {
			log.info("Exception In Sending Mail:{}", e.getMessage());
		}
	}

	private void enrichEmployeeNotification(Map<String, EmailTemplateModel> map, String role,
			NOCApplication application, NOCRequestData app1) throws IOException {
		log.info("Users will be fetched for role {}", role);
		JsonNode userResponse = userUtil.getUserByRole(app1.getRequestInfo(), role, application.getTenantId());
		log.info("Response {}", userResponse);

		if (userResponse != null) {
			for (JsonNode employee : userResponse.get("Employees")) {

				// JsonNode userInfo = userResponse.get("Employees") != null
				// ? userResponse.get("Employees").get(0).get("user")
				// : null;
				JsonNode userInfo = employee.get("user");
				if (userInfo != null) {

					log.info("userInfo found{}", userInfo);
					User user = objectMapper.readValue(objectMapper.writeValueAsString(userInfo), User.class);
					log.info("user found{}", user);
					String queryString = map.get(role).getTemplate().replace(CommonConstants.EMAILAPPID,
							application.getNocNumber());
					log.info("queryString :{} ", queryString);
					if (user != null && user.getEmailId() != null && !user.getEmailId().isEmpty()) {
						log.info("Email Sent To Id :{} ", user.getEmailId());

						EmailRequest email = EmailRequest.builder().email(user.getEmailId())
								.subject(map.get(role).getEmailSubject()).isHTML(true).body(queryString).build();
						producer.push(emailtopic, email);
						log.info("email sent to kafka");
					}
				}
			}
		}

	}

	private void enrichCitizenNotification(Map<String, EmailTemplateModel> map, String role, NOCApplication application,
			NOCRequestData app1) throws IOException {

		log.info("Calling user service for uuid {}", application.getCreatedBy());
		JsonNode userInfo = userUtil.getUser(app1.getRequestInfo(), application.getCreatedBy());
		log.info("Response from user service {}", userInfo);

		User user = null;
		if (userInfo != null && userInfo.get("user") != null)
			user = objectMapper.readValue(objectMapper.writeValueAsString(userInfo.get("user").get(0)), User.class);

		log.info("UserInfo {}", user);
		String queryString = map.get(role).getTemplate().replace(CommonConstants.EMAILAPPID, application.getNocNumber())
				.replace("[:fees:]", application.getTotalamount().toString());
		log.info("emailTemplate is {}", queryString);
		if (user != null && user.getEmailId() != null && !user.getEmailId().isEmpty()) {
			log.info("emailId is {}", user.getEmailId());
			EmailRequest email = EmailRequest.builder().email(user.getEmailId())
					.subject(map.get(role).getEmailSubject()).isHTML(true).body(queryString).build();

			producer.push(emailtopic, email);
			log.info("email data pushed to kafka");
		}
		if (user != null && user.getMobileNumber() != null && !user.getMobileNumber().isEmpty()) {
			String smsTemplate = map.get(role).getSmsTemplate().replace(CommonConstants.EMAILAPPID,
					application.getNocNumber()).replace("[:fees:]", application.getTotalamount().toString());
			log.info(smsTemplate);
			SMSRequest smsRequest = new SMSRequest(user.getMobileNumber(), smsTemplate);
			producer.push(smstopic, smsRequest);
			log.info("sms data pushed to kafka");
		}
	}
}
