package org.egov.ps.service;

import org.egov.ps.model.notification.EmailRequest;
import org.egov.ps.model.notification.SMSRequest;
import org.egov.ps.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    Producer producer;

    @Value("${kafka.topics.notification.email}")
    private String notificationEmailTopic;

    @Value("${kafka.topics.notification.sms}")
    private String notificationSMSTopic;

    public void sendSMS(String mobileNumber, String content) {
        log.debug("Sending sms to '{}' with body '{}'", mobileNumber, content);
        this.producer.push(this.notificationSMSTopic, new SMSRequest(mobileNumber, content));
    }

    public void sendEmail(String email, String subject, String body) {
        this.sendEmail(email, subject, body, false);
    }

    public void sendEmail(String email, String subject, String body, boolean isHTML) {
        log.debug("Sending email to '{}' with subject '{}' and body '{}', html {}", email, subject, body, isHTML);
        this.producer.push(this.notificationEmailTopic,
                EmailRequest.builder().subject(subject).email(email).body(body).isHTML(isHTML).build());
    }
}
