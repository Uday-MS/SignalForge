package com.signalforge.controller;

import com.signalforge.entity.User;
import com.signalforge.jwt.JwtUtil;
import com.signalforge.repository.UserRepository;
import com.signalforge.services.SessionService;
import com.signalforge.util.CookieUtil;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class OAuthController {

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;
    @Autowired private SessionService sessionService;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @Value("${COOKIE_SECURE}")
    private boolean secureCookie;

    @GetMapping("/oauth-success")
    public Object success(OAuth2AuthenticationToken oAuth2AuthenticationToken, HttpServletResponse response) {
        Map<String, Object> attributes = oAuth2AuthenticationToken.getPrincipal().getAttributes();
        String email = attributes.get("email").toString().toLowerCase().trim();
        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            user = new User();
            user.setEmail(email);
            user.setPassword(null); // OAuth users have no password
            userRepository.save(user);
        }

        String sessionId = jwtUtil.generateSessionId();
        sessionService.createSession(sessionId, String.valueOf(user.getId()));
        String token = jwtUtil.generateToken(email, sessionId);

        String refreshToken = jwtUtil.generateRefreshToken();
        sessionService.storeRefreshToken(refreshToken, String.valueOf(user.getId()), email);

        ResponseCookie cookie = CookieUtil.buildRefreshCookie(refreshToken, 7L * 24 * 60 * 60, secureCookie);
        response.addHeader("Set-Cookie", cookie.toString());
        return new RedirectView(frontendUrl + "/index.html?oauth=success");
    }
}
