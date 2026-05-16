package com.uptimerobot.services;

import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    Map<String, String> otpStore = new ConcurrentHashMap<>();

    public void sendOtp(String email) throws Exception {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(email, otp);

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey(brevoApiKey);

        TransactionalEmailsApi api = new TransactionalEmailsApi();

        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail("uptimerobot.auth@gmail.com");  // your verified sender
        sender.setName("UptimeRobot");

        SendSmtpEmailTo recipient = new SendSmtpEmailTo();
        recipient.setEmail(email);

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.setSender(sender);
        sendSmtpEmail.setTo(List.of(recipient));
        sendSmtpEmail.setSubject("Your UptimeRobot OTP");
        sendSmtpEmail.setTextContent("Your OTP is: " + otp + ". Valid for 10 mins!");

        api.sendTransacEmail(sendSmtpEmail);
    }

    public boolean verifyOtp(String email, String otp) {
        String stored = otpStore.get(email);
        if (stored == null || !stored.equals(otp)) return false;
        otpStore.remove(email);
        return true;
    }
}