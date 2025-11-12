package com.furryhub.petservices.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @Value("${twilio.messaging.service.sid}")
    private String messagingServiceSid;

    public void sendSms(String toPhoneNumber, String messageBody) {
        Twilio.init(accountSid, authToken);

        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(toPhoneNumber), // To number
                (PhoneNumber) null, //  remove explicit From when using messaging service
                messageBody // Message body
        ).setMessagingServiceSid(messagingServiceSid).create();

        System.out.println("Message sent! SID: " + message.getSid());
    }
}