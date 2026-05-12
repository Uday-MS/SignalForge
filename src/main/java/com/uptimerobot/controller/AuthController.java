package com.uptimerobot.controller;

import com.uptimerobot.dto.LoginRequest;
import com.uptimerobot.dto.OtpRequest;
import com.uptimerobot.dto.RegisterRequest;
import com.uptimerobot.entity.User;
import com.uptimerobot.jwt.JwtUtil;
import com.uptimerobot.repository.userRepo;
import com.uptimerobot.services.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/uptimerobot/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private userRepo userRepo;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OtpService otpService;

    Map <String,String> pendingRequests = new ConcurrentHashMap<>();

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody RegisterRequest request, BindingResult result) {
        if(result.hasErrors()){
            String errorMessage= result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("Message",errorMessage));
        }
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already registered"));
        }
        pendingRequests.put(request.getEmail(), request.getPassword());

        try {
            otpService.sendOtp(request.getEmail());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("Error","Could not send the otp , enter valid email address"));
        }
        return ResponseEntity.ok().body("Sent the otp to the "+request.getEmail());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity verifyOtp(@RequestBody OtpRequest otpRequest){
        boolean valid= otpService.verifyOtp(otpRequest.getEmail(),otpRequest.getOtp());
        if(!valid){
            return ResponseEntity
                    .badRequest().body(Map.of("Error","Invalid OTP , please enter a valid OTP"));
        }
        String rawPassword= pendingRequests.remove(otpRequest.getEmail());
        User user= new User();
        user.setEmail(otpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepo.save(user);

        String token = jwtUtil.generateToken(otpRequest.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token",token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtUtil.generateToken(request.getEmail());
        return ResponseEntity.ok(token);
    }
}
