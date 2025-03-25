package com.job.interview.blog.service.impl;

import com.job.interview.blog.configuration.ApplicationProperties;
import com.job.interview.blog.service.JwtUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtUtilsImpl implements JwtUtils {

    private final ApplicationProperties appProperties;

    @Override
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.debug("Authorization Header: {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    public String generateTokenFromUsername(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(
                        new Date((new Date()).getTime() +
                                appProperties
                                        .getSecurity()
                                        .getJwt()
                                        .expiration()
                        )
                )
                .signWith(key())
                .compact();
    }

    @Override
    public String getUserNameFromJwtToken(String token) {
        return Jwts
                .parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    @Override
    public boolean validateJwtToken(String authToken) {
        return false; //TODO add validation logic
    }

    private Key key() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(
                        appProperties
                                .getSecurity()
                                .getJwt()
                                .secret()
                )
        );
    }
}