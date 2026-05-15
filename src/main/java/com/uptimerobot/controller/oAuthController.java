package com.uptimerobot.controller;

import com.uptimerobot.entity.User;
import com.uptimerobot.jwt.JwtUtil;
import com.uptimerobot.repository.userRepo;
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
@RequestMapping("/uptimerobot")
public class oAuthController {

    @Autowired
    public JwtUtil jwtUtil;

    @Autowired
    public userRepo userRepo;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @GetMapping("/oauth-success")
    public Object success(OAuth2AuthenticationToken oAuth2AuthenticationToken){
        Map<String , Object> attributes=oAuth2AuthenticationToken.getPrincipal().getAttributes();
        String email= attributes.get("email").toString();
        String name =attributes.get("name").toString();
        Optional<User>existingUser=userRepo.findByEmail(email);

        User user;

        if (existingUser.isPresent()){
            user=existingUser.get();
        }else{
            user=new User();
            user.setEmail(email);
            user.setPassword("");
            userRepo.save(user);
        }

        String token = jwtUtil.generateToken(email);

        return new RedirectView(frontendUrl + "/index.html?token=" + token + "&email=" + email);
    }
}