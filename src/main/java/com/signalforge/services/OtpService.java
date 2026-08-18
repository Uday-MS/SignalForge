package com.signalforge.services;

import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class OtpService {

    private static final Logger logger = Logger.getLogger(OtpService.class.getName());

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${SENDER_EMAIL:signalforge@gmail.com}")
    private String senderEmail;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String OTP_PREFIX = "otp:";
    private static final String OTP_COOLDOWN_PREFIX = "otp_cooldown:";
    private static final long OTP_TTL_SECONDS = 180;       // 3 minutes
    private static final long COOLDOWN_SECONDS = 60;        // 1 minute between resends

    /**
     * Generate a cryptographically secure 6-digit OTP, store in Redis with TTL,
     * and send to the user's email via Brevo.
     *
     * @return true if sent, false if rate-limited
     */
    public boolean sendOtp(String email) throws Exception {
        String normalizedEmail = email.toLowerCase().trim();

        // Rate limiting: check cooldown
        String cooldownKey = OTP_COOLDOWN_PREFIX + normalizedEmail;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            return false; // Rate limited
        }

        // Generate cryptographically secure 6-digit OTP
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));

        // Store in Redis (overwrites any previous OTP for this email)
        String otpKey = OTP_PREFIX + normalizedEmail;
        redisTemplate.opsForValue().set(otpKey, otp, OTP_TTL_SECONDS, TimeUnit.SECONDS);

        // Set cooldown
        redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);

        // Send email via Brevo
        sendOtpEmail(normalizedEmail, otp);

        return true;
    }

    /**
     * Verify OTP. Single-use: removed from Redis after successful verification.
     */
    public boolean verifyOtp(String email, String otp) {
        String normalizedEmail = email.toLowerCase().trim();
        String otpKey = OTP_PREFIX + normalizedEmail;

        String storedOtp = redisTemplate.opsForValue().get(otpKey);
        if (storedOtp == null) return false;                // Expired or never sent
        if (!storedOtp.equals(otp)) return false;           // Wrong OTP

        // Single-use: delete after successful verification
        redisTemplate.delete(otpKey);
        return true;
    }

    private void sendOtpEmail(String recipientEmail, String otp) throws Exception {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(brevoApiKey);

        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(senderEmail);
        sender.setName("SignalForge");

        SendSmtpEmailTo recipient = new SendSmtpEmailTo();
        recipient.setEmail(recipientEmail);

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
}
