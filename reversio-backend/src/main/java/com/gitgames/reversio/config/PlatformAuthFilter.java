package com.gitgames.reversio.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class PlatformAuthFilter extends OncePerRequestFilter {

    @Value("${platform.jwt.secret:}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ") && !jwtSecret.isBlank()) {
            String token = authHeader.substring(7);
            try {
                Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
                JWTVerifier verifier = JWT.require(algorithm).withIssuer("game-platform").build();
                DecodedJWT jwt = verifier.verify(token);

                request.setAttribute("platformUserId", jwt.getSubject());
                request.setAttribute("platformUserRole", jwt.getClaim("role").asString());
                request.setAttribute("platformUsername", jwt.getClaim("username").asString());
            } catch (JWTVerificationException e) {
                // Invalid token — continue anonymously
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/platform/");
    }
}
