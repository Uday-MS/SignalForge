package com.uptimerobot.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret_key;

    private Key key;

    @PostConstruct
    public void init(){
        this.key=new SecretKeySpec(
                secret_key.getBytes(StandardCharsets.UTF_8)
                ,0
                ,secret_key.getBytes(StandardCharsets.UTF_8).length
                ,"HmacSHA256"
        );
    }
    public String generateToken(String email){
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+1000*60*60))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    public Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public String extractUsername(String token){
         return extractAllClaims(token).getSubject();
    }
    public boolean isTokenExpired(String token){
        return extractAllClaims(token).getExpiration().before(new Date());
    }
    public boolean isTokenValid(String token,String username){
        return extractAllClaims(token).getSubject().equals(username)&&!isTokenExpired(token);
    }
}
