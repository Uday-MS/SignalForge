package com.uptimerobot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RegisterRequest {
    private String email;
    private String password;

    public RegisterRequest() {}

    @NotBlank(message="Email cannot be blank !")
    @Email(message = "Please enter a valid Email address")
    public String getEmail() {
        return email;
    }
    @NotBlank(message = "Password cannot be empty !")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
            message = "Password must be at least 8 characters, include 1 uppercase letter, 1 number, and 1 special character"
    )

    public String getPassword() {
        return password;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
