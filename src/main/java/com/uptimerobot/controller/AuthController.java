package com.uptimerobot.controller;

import com.uptimerobot.dto.LoginRequest;
import com.uptimerobot.dto.OtpRequest;
import com.uptimerobot.dto.RegisterRequest;
import com.uptimerobot.entity.User;
import com.uptimerobot.jwt.JwtUtil;
import com.uptimerobot.repository.userRepo;
import com.uptimerobot.services.OtpService;
import com.uptimerobot.services.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
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
import java.util.logging.Logger;

@RestController
@RequestMapping("/uptimerobot/auth")
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

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
    @Autowired
    private SessionService sessionService;

    @Value("${COOKIE_SECURE}")
    private boolean secureCookie;

    Map <String,String> pendingRequests = new ConcurrentHashMap<>();

    private ResponseCookie buildRefreshCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite(secureCookie ? "None" : "Lax")
                .build();
    }

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
        } catch (Exception e) {
            logger.severe("OTP send failed: " + e.getMessage() + " | Cause: " + e.getCause());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("Error", "Could not send OTP: " + e.getMessage()));
        }
        return ResponseEntity.ok().body("Sent the otp to the " + request.getEmail());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity verifyOtp(@RequestBody OtpRequest otpRequest,HttpServletResponse response){
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
        String sessionId= jwtUtil.generateSessionId();
        String token= jwtUtil.generateToken(otpRequest.getEmail(),sessionId);
         sessionService.createSession(sessionId,String.valueOf(user.getId()));
        String refreshToken= jwtUtil.generateRefreshToken();
        sessionService.storeRefreshToken(refreshToken,String.valueOf(user.getId()), otpRequest.getEmail());

        ResponseCookie cookie = buildRefreshCookie(refreshToken, 7L * 24 * 60 * 60);
        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token",token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
      authenticationManager
              .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail()
                      ,request.getPassword()));
      User user=userRepo.findByEmail(request.getEmail()).orElseThrow();

      String sessionId= jwtUtil.generateSessionId();
      String token= jwtUtil.generateToken(request.getEmail(),sessionId);
      sessionService.createSession(sessionId,String.valueOf(user.getId()));

      String refreshToken= jwtUtil.generateRefreshToken();
      sessionService.storeRefreshToken(refreshToken,String.valueOf(user.getId()),request.getEmail());

        ResponseCookie cookie = buildRefreshCookie(refreshToken, 7L * 24 * 60 * 60);
        response.addHeader("Set-Cookie", cookie.toString());
      return ResponseEntity.ok().body(Map.of("token",token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("AUTHORIZATION") String authHeader,
                                    HttpServletResponse response, HttpServletRequest request) {
        String token = authHeader.substring(7);
        String sessionId = jwtUtil.extractSessionId(token);

        // Get userId from session BEFORE deleting it
        String userId = sessionService.getUserIdFromSession(sessionId);
        sessionService.deleteSession(sessionId);

        // Primary: delete refresh token via reverse mapping (userId -> refreshToken)
        // This works even when the browser doesn't send the cookie (third-party cookie blocking)
        if (userId != null) {
            sessionService.deleteRefreshTokenByUserId(userId);
        }

        // Fallback: also try cookie-based deletion in case reverse mapping was missing
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals("refreshToken")) {
                    sessionService.deleteRefreshToken(c.getValue());
                    break;
                }
            }
        }

        ResponseCookie expiredCookie = buildRefreshCookie("", 0);
        response.addHeader("Set-Cookie", expiredCookie.toString());

        return ResponseEntity.ok(Map.of("Message", "Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No refresh token"));
        }
        String value = sessionService.getUserIdFromRefreshToken(refreshToken);
        if (value == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Session expired, please login again"));
        }
        String userId = value.split(":")[0];
        String email = value.split(":")[1];


        String sessionId = jwtUtil.generateSessionId();
        sessionService.createSession(sessionId, userId);
        String newAccessToken = jwtUtil.generateToken(email, sessionId);

        return ResponseEntity.ok(Map.of("token", newAccessToken));
    }
}