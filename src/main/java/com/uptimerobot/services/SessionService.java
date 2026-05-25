package com.uptimerobot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

    @Autowired
    RedisTemplate<String ,String >redisTemplate;

    private final static String SESSION_PREFIX="session:";

    private final static long SESSION_TTL=180;

    private final static String REFRESH_PREFIX="refresh:";

    private final static String USER_REFRESH_PREFIX="user_refresh:";

    private final static long REFRESH_TTL=7;

     public void createSession(String sessionId,String userId){
         String key= SESSION_PREFIX+sessionId;
         redisTemplate.opsForValue().set(key,userId,SESSION_TTL, TimeUnit.SECONDS);
     }
     public boolean isSessionValid(String sessionId){
         return Boolean.TRUE.equals(redisTemplate.hasKey(SESSION_PREFIX+sessionId));
     }
     public void deleteSession(String sessionId){

         redisTemplate.delete(SESSION_PREFIX+sessionId);
     }
     public String getUserIdFromSession(String sessionId){
         return redisTemplate.opsForValue().get(SESSION_PREFIX+sessionId);
     }
     public void storeRefreshToken(String refreshToken,String userId,String email){
         redisTemplate.opsForValue()
                 .set(REFRESH_PREFIX+refreshToken,userId+":"+email,REFRESH_TTL,TimeUnit.DAYS);
         // Reverse mapping: userId -> refreshToken (so we can delete by userId on logout)
         redisTemplate.opsForValue()
                 .set(USER_REFRESH_PREFIX+userId,refreshToken,REFRESH_TTL,TimeUnit.DAYS);
     }
     public String getUserIdFromRefreshToken(String refreshToken){
         return redisTemplate.opsForValue().get(REFRESH_PREFIX+refreshToken);
     }
     public String getEmailFromtRefreshToken(String refreshToken){
         String value=redisTemplate.opsForValue().get(REFRESH_PREFIX+refreshToken);
         if(value==null)return null;
         return value.split(":")[1];
     }
     public void deleteRefreshToken(String refreshToken){
         redisTemplate.delete(REFRESH_PREFIX+refreshToken);
     }

     public void deleteRefreshTokenByUserId(String userId){
         String refreshToken = redisTemplate.opsForValue().get(USER_REFRESH_PREFIX+userId);
         if(refreshToken != null){
             redisTemplate.delete(REFRESH_PREFIX+refreshToken);
         }
         redisTemplate.delete(USER_REFRESH_PREFIX+userId);
     }

}
