package com.signalforge.controller;

import com.signalforge.dto.LoginRequest;
import com.signalforge.dto.OtpRequest;
import com.signalforge.dto.RegisterRequest;
import com.signalforge.entity.User;
import com.signalforge.jwt.JwtUtil;
import com.signalforge.repository.UserRepository;
import com.signalforge.services.OtpService;
import com.signalforge.services.SessionService;
import com.signalforge.util.CookieUtil;
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
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private OtpService otpService;
    @Autowired private SessionService sessionService;

    @Value("${COOKIE_SECURE}")
    private boolean secureCookie;

    private final Map<String, String> pendingRequests = new ConcurrentHashMap<>();

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody RegisterRequest request, BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMessage));
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already registered"));
        }
        pendingRequests.put(request.getEmail(), request.getPassword());

        try {
            otpService.sendOtp(request.getEmail());
        } catch (Exception e) {
            logger.severe("OTP send failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Could not send OTP: " + e.getMessage()));
        }
        return ResponseEntity.ok().body(Map.of("message", "OTP sent to " + request.getEmail()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest otpRequest, HttpServletResponse response) {
        boolean valid = otpService.verifyOtp(otpRequest.getEmail(), otpRequest.getOtp());
        if (!valid) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired OTP"));
        }
        String rawPassword = pendingRequests.remove(otpRequest.getEmail());
        User user = new User();
        user.setEmail(otpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);

        String sessionId = jwtUtil.generateSessionId();
        String token = jwtUtil.generateToken(otpRequest.getEmail(), sessionId);
        sessionService.createSession(sessionId, String.valueOf(user.getId()));

        String refreshToken = jwtUtil.generateRefreshToken();
        sessionService.storeRefreshToken(refreshToken, String.valueOf(user.getId()), otpRequest.getEmail());

        ResponseCookie cookie = CookieUtil.buildRefreshCookie(refreshToken, 7L * 24 * 60 * 60, secureCookie);
        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        String sessionId = jwtUtil.generateSessionId();
        String token = jwtUtil.generateToken(request.getEmail(), sessionId);
        sessionService.createSession(sessionId, String.valueOf(user.getId()));

        String refreshToken = jwtUtil.generateRefreshToken();
        sessionService.storeRefreshToken(refreshToken, String.valueOf(user.getId()), request.getEmail());

        ResponseCookie cookie = CookieUtil.buildRefreshCookie(refreshToken, 7L * 24 * 60 * 60, secureCookie);
        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok().body(Map.of("token", token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("AUTHORIZATION") String authHeader,
                                    HttpServletResponse response, HttpServletRequest request) {
        String token = authHeader.substring(7);
        String sessionId = jwtUtil.extractSessionId(token);

        String userId = sessionService.getUserIdFromSession(sessionId);
        sessionService.deleteSession(sessionId);

        if (userId != null) {
            sessionService.deleteRefreshTokenByUserId(userId);
        }

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals("refreshToken")) {
                    sessionService.deleteRefreshToken(c.getValue());
                    break;
                }
            }
        }

        ResponseCookie expiredCookie = CookieUtil.buildRefreshCookie("", 0, secureCookie);
        response.addHeader("Set-Cookie", expiredCookie.toString());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
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
            try {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String expiredToken = authHeader.substring(7);
                    String session = jwtUtil.extractSessionIdIgnoreExpiry(expiredToken);
                    if (session != null) {
                        String userId = sessionService.getUserIdFromSession(session);
                        if (userId != null) {
                            refreshToken = sessionService.getRefreshTokenByUserId(userId);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Please login again"));
        }
        String value = sessionService.getUserIdFromRefreshToken(refreshToken);
        if (value == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Session expired, please login again"));
        }
        String userId = value.split(":")[0];
        String email = value.split(":")[1];
        String newSessionId = jwtUtil.generateSessionId();
        String newAccessToken = jwtUtil.generateToken(email, newSessionId);
        sessionService.createSession(newSessionId, userId);
        return ResponseEntity.ok(Map.of("token", newAccessToken));
    }
}
