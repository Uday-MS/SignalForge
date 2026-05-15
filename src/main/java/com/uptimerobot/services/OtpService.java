package com.uptimerobot.services;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    Map<String, String> otpStore = new ConcurrentHashMap<>();

    public void sendOtp(String email) throws ResendException {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(email, otp);

        Resend resend = new Resend(resendApiKey);

        CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                .from("UptimeRobot <onboarding@resend.dev>")
                .to(email)
                .subject("Your UptimeRobot OTP")
                .text("Your OTP for registration is: " + otp + ". This code is only valid for 10 mins!")
                .build();

        resend.emails().send(emailOptions);
    }

    public boolean verifyOtp(String email, String otp) {
        String stored = otpStore.get(email);
        if (stored == null || !stored.equals(otp)) {
            return false;
        }
        otpStore.remove(email);
        return true;
    }
}