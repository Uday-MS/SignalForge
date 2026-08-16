package com.signalforge.services;

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

    @Value("${SENDER_EMAIL:signalforge@gmail.com}")
    private String senderEmail;

    record OtpEntry(String otp, Instant expiresAt) {}

    Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    public void sendOtp(String email) throws Exception {
        String otp = String.format("%06d", new Random().nextInt(999999));

        Instant expiresAt = Instant.now().plusSeconds(180);
        otpStore.put(email, new OtpEntry(otp, expiresAt));

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(brevoApiKey);

        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(senderEmail);
        sender.setName("SignalForge");

        SendSmtpEmailTo recipient = new SendSmtpEmailTo();
        recipient.setEmail(email);

        TransactionalEmailsApi transactionalEmailsApi = new TransactionalEmailsApi();

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.sender(sender);
        sendSmtpEmail.setTo(List.of(recipient));
        sendSmtpEmail.setSubject("Your SignalForge verification code");
        sendSmtpEmail.setHtmlContent(
                "<div style='font-family: Inter, sans-serif; max-width: 480px; margin: 0 auto; padding: 40px 20px;'>" +
                "<h2 style='color: #635BFF; margin-bottom: 8px;'>SignalForge</h2>" +
                "<p style='color: #6b7280; margin-bottom: 24px;'>Enter this code to verify your email:</p>" +
                "<div style='background: #f3f4f6; border-radius: 12px; padding: 24px; text-align: center; margin-bottom: 24px;'>" +
                "<span style='font-size: 36px; font-weight: 700; letter-spacing: 8px; color: #111827;'>" + otp + "</span>" +
                "</div>" +
                "<p style='color: #9ca3af; font-size: 14px;'>This code expires in 3 minutes.</p>" +
                "</div>"
        );

        transactionalEmailsApi.sendTransacEmail(sendSmtpEmail);
    }

    public boolean verifyOtp(String email, String otp) {
        OtpEntry entry = otpStore.get(email);
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiresAt())) {
            otpStore.remove(email);
            return false;
        }
        if (!entry.otp().equals(otp)) return false;
        otpStore.remove(email);
        return true;
    }
}
