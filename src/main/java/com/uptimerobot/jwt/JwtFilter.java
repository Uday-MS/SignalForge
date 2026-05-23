package com.uptimerobot.jwt;

import com.uptimerobot.services.SessionService;
import com.uptimerobot.services.UserDetailsImpl;
import io.jsonwebtoken.Jwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();

        if (path.startsWith("/oauth2") ||
                path.startsWith("/login") ||
                path.startsWith("/error")) {

            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");
        if(authHeader==null||!authHeader.startsWith("Bearer ")){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"No token provided\"}");
            return;
        }
        String token=authHeader.substring(7);
        try{
            String sessionId= jwtUtil.extractSessionId(token);
            if(!sessionService.isSessionValid(sessionId)){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Session expired\"}");
                return;
            }
            if(jwtUtil.isTokenExpired(token)){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Token expired\"}");
                return;
            }
            String username= jwtUtil.extractUsername(token);

            org.springframework.security.core.userdetails.User principal =
                    new org.springframework.security.core.userdetails.User(username, "", List.of());

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(principal, null, List.of());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        catch (Exception e){
            System.out.println("JWT FILTER ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        filterChain.doFilter(request,response);
    }
}
