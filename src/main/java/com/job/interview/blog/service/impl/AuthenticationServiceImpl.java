package com.job.interview.blog.service.impl;

import com.job.interview.blog.configuration.ApplicationProperties;
import com.job.interview.blog.model.dto.request.AuthenticationRequest;
import com.job.interview.blog.model.dto.request.RegistrationRequest;
import com.job.interview.blog.model.dto.response.AuthenticationResponse;
import com.job.interview.blog.model.user.Token;
import com.job.interview.blog.model.user.UserEntity;
import com.job.interview.blog.repository.TokenRepository;
import com.job.interview.blog.repository.UserRepository;
import com.job.interview.blog.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import static com.job.interview.blog.model.user.UserRole.READER;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final ApplicationProperties appProperties;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Override
    public void registerUser(RegistrationRequest request) { //TODO add exception handling
        log.info("Registration request email {}, lastName {}, firstName {}",
                request.getEmail(), request.getLastName(), request.getFirstName());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("User with email {} already exists", request.getEmail());
            throw new IllegalStateException("User already registered!");
        }

        var user = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                //TODO encode password
                .accountLocked(false)
                .enabled(
                        appProperties
                                .getSecurity()
                                .isCreateEnabledUsers()
                )
                .role(READER)
                .build();
        userRepository.save(user);
        sendValidationEmail(user);
        log.info("User registered successfully! {}", user);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Authenticate request: {}", request);

        //TODO implement authentication manager and check existance of the user

        return AuthenticationResponse.builder() //TODO build the jwt token
                .token("test token 123")
                .build();
    }

    @Override
    public void activateAccount(String activationCode) {
        var savedToken = tokenRepository.findByToken(activationCode)
                .orElseThrow(() -> new IllegalStateException("Token not found!"));

        var user = savedToken.getUser();
        log.info("Activating account for user: {}", user);

        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(user);
            throw new IllegalStateException("Token expired!");
        }
        var foundUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        foundUser.setEnabled(true);
        userRepository.save(foundUser);
        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
        log.info("Activated account for user: {}", user);
    }

    private void sendValidationEmail(UserEntity user) {
        String generatedToken = generateAndSaveActivationToken(user);
        if (!appProperties.getSecurity().getEmail().verification()) {
            log.info("Email verification is disabled, Verification token is: {}", generatedToken);
            return;
        }
        //TODO send email to user
        log.info("Email verification is sent to {}", user.getEmail());
    }

    private String generateAndSaveActivationToken(UserEntity user) {
        var generatedActivationToken = generateActivationToken();
        var token = Token.builder()
                .token(generatedActivationToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now()
                        .plusMinutes(
                                appProperties
                                        .getSecurity()
                                        .getActivationToken()
                                        .expiration()
                        ))
                .user(user)
                .build();
        tokenRepository.save(token);
        log.info("Generated activation token for user: {}", user.getEmail());
        return generatedActivationToken;
    }

    private String generateActivationToken() {
        String chars = appProperties
                .getSecurity()
                .getActivationToken()
                .chars();

        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int index = 0; index < appProperties
                .getSecurity()
                .getActivationToken()
                .length();
             index++) {
            int randomIndex = secureRandom.nextInt(chars.length());
            codeBuilder.append(chars.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }
}