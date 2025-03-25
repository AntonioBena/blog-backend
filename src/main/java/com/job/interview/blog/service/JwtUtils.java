package com.job.interview.blog.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtUtils {
    String getJwtFromHeader(HttpServletRequest request);
    String generateTokenFromUsername(UserDetails userDetails);
    String getUserNameFromJwtToken(String token);
    boolean validateJwtToken(String authToken);
}