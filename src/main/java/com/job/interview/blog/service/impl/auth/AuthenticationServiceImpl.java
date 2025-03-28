package com.job.interview.blog.service.impl.auth;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import static com.job.interview.blog.model.user.UserRole.READER;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final ApplicationProperties appProperties;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtilsImpl jwtUtils;

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
                .password(
                        passwordEncoder.encode(request.getPassword())
                )
                .accountLocked(false)
                .enabled(
                        appProperties
                                .getSecurity()
                                .isCreateEnabledUsers()
                )
                .role(READER)
                .build();
        var createdUser = userRepository.save(user);
        sendValidationEmail(createdUser);
        log.info("User registered successfully! {}", user);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Authenticate request: {}", request);
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException exception) {
            log.error("Authentication failed, bad credentials: {}", exception.getMessage());
            throw new BadCredentialsException("Bad credentials", exception);
        }

        log.info("User authenticated successfully! {}", authentication.getPrincipal());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        log.info("logged in as: {}, roles: {}, jwtToken: {}", userDetails.getUsername(), roles, jwtToken);

        return AuthenticationResponse.builder()
                .token(jwtToken)
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