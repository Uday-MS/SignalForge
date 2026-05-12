package com.uptimerobot.dto;

public class OtpRequest {
    private String email ;
    private String otp ;
    private String password ;

    public OtpRequest(String email, String otp, String password) {
        this.email = email;
        this.otp = otp;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getOtp() {
        return otp;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
