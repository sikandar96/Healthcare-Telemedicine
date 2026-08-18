package com.health.care.services;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OtpDeliveryService {
    private static final Logger logger = LoggerFactory.getLogger(OtpDeliveryService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final RestClient restClient = RestClient.create();

    @Value("${app.otp.channel:email}")
    private String defaultChannel;
    @Value("${app.otp.email.from:}")
    private String fromAddress;
    @Value("${app.otp.sms.webhook-url:}")
    private String smsWebhookUrl;
    @Value("${app.otp.expose-in-response:false}")
    private boolean exposeOtpInResponse;

    public OtpDeliveryService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public DeliveryResult deliver(String email, String phone, String requestedChannel, String otp) {
        String channel = requestedChannel == null || requestedChannel.isBlank()
                ? defaultChannel.trim().toLowerCase()
                : requestedChannel.trim().toLowerCase();
        if ("sms".equals(channel)) {
            String destination = maskPhone(phone);
            if (phone == null || phone.isBlank()) throw new IllegalArgumentException("No phone number is registered for this account");
            if (smsWebhookUrl == null || smsWebhookUrl.isBlank()) {
                logger.info("SMS OTP delivery is not configured. destination={}, otp={}", destination, otp);
            } else {
                restClient.post().uri(smsWebhookUrl).contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("to", phone, "message", "Your healthcare-telemedicine password reset OTP is " + otp + ". It expires in 10 minutes."))
                        .retrieve().toBodilessEntity();
            }
            return new DeliveryResult("sms", destination, exposeOtpInResponse ? otp : null);
        }

        String destination = maskEmail(email);
        if (email == null || email.isBlank()) throw new IllegalArgumentException("No email address is registered for this account");
        Optional<JavaMailSender> sender = Optional.ofNullable(mailSenderProvider.getIfAvailable());
        if (sender.isEmpty() || fromAddress == null || fromAddress.isBlank()) {
            logger.info("Email OTP delivery is not configured. destination={}, otp={}", destination, otp);
        } else {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setFrom(fromAddress);
            message.setSubject("Your healthcare-telemedicine password reset code");
            message.setText("Your one-time password is " + otp + ". It expires in 10 minutes. If you did not request this, ignore this message.");
            sender.get().send(message);
        }
        return new DeliveryResult("email", destination, exposeOtpInResponse ? otp : null);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "email";
        int at = email.indexOf('@');
        if (at < 2) return "***" + email.substring(Math.max(at, 0));
        return email.charAt(0) + "***" + email.substring(at - 1);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) return "phone";
        String normalized = phone.replaceAll("\\s+", "");
        return normalized.length() <= 4 ? "****" : "****" + normalized.substring(normalized.length() - 4);
    }

    public record DeliveryResult(String channel, String destination, String developmentOtp) {}
}
