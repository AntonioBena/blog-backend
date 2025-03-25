package com.job.interview.blog.service;

import jakarta.servlet.http.HttpServletRequest;

public interface JwtUtils {
    String getJwtFromHeader(HttpServletRequest request);
    String generateTokenFromUsername();//TODO userdetails
    String getUserNameFromJwtToken(String token);
    boolean validateJwtToken(String authToken);
}