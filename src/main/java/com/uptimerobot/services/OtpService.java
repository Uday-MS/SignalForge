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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    record OtpEntry(String Otp, Instant expiresAt){}

    Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();


    public void sendOtp(String email) throws Exception {
        String otp = String.format("%06d", new Random().nextInt(99999));

        Instant expiresAt=Instant.now().plusSeconds(180);
        otpStore.put(email,new OtpEntry(otp,expiresAt));
        ApiClient defaultClient=Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth=(ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(brevoApiKey);

        SendSmtpEmailSender sender=new SendSmtpEmailSender();
        sender.setEmail("uptimerobot.auth@gmail.com");
        sender.setName("UpTimeRobot");

        SendSmtpEmailTo recipent= new SendSmtpEmailTo();
        recipent.setEmail(email);

        TransactionalEmailsApi transactionalEmailsApi= new TransactionalEmailsApi();

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.sender(sender);
        sendSmtpEmail.setTo(List.of(recipent));
        sendSmtpEmail.setSubject("Your OTP for registration is : ");
        sendSmtpEmail.setTextContent(""+otp+ "\n\nValid for 3 minutes !");
         transactionalEmailsApi.sendTransacEmail(sendSmtpEmail);
    }

    public boolean verifyOtp(String email, String otp) {
        OtpEntry entry=otpStore.get(email);
        if(entry==null)return false;
        Instant expiry= entry.expiresAt();
        if(Instant.now().isAfter(expiry)){
            otpStore.remove(email);
            return false;
        }
         if(!entry.Otp().equals(otp))return false;
         return true;
    }
}