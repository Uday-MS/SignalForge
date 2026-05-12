package com.uptimerobot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    Map<String ,String> otpStore= new ConcurrentHashMap<>();

    public void sendOtp(String email){
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(email,otp);
        SimpleMailMessage simpleMailMessage= new SimpleMailMessage();
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("Your Uptime robot OTP");
        simpleMailMessage.setText("Your OTP for registration is :"+otp+" "+"This code is only valid for 10 mins !");
        mailSender.send(simpleMailMessage);
    }
    public boolean verifyOtp(String email ,String otp){
        if(!otpStore.get(email).equals(otp)){
            return false;
        }
        otpStore.remove(email);
        return true;
    }
}
