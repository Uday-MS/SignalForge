package com.uptimerobot.controller;

import com.uptimerobot.dto.LoginRequest;
import com.uptimerobot.dto.RegisterRequest;
import com.uptimerobot.entity.User;
import com.uptimerobot.jwt.JwtUtil;
import com.uptimerobot.repository.userRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/uptimerobot/auth")
public class AuthController {

    @Autowired private JwtUtil jwtUtil;
    @Autowired private userRepo userRepo;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already registered"));
        }
        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()));
        userRepo.save(user);
        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body("User Created Successful !!");
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
