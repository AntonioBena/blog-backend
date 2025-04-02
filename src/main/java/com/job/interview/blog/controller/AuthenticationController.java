package com.job.interview.blog.controller;

import com.job.interview.blog.model.dto.request.AuthenticationRequest;
import com.job.interview.blog.model.dto.request.RegistrationRequest;
import com.job.interview.blog.model.dto.response.AuthenticationResponse;
import com.job.interview.blog.service.impl.auth.AuthenticationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authorization")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "auth")
public class AuthenticationController {
    private final AuthenticationServiceImpl authenticationService;

    @Operation(
            description = "Endpoint for registering new user",
            summary = "Creates new user - user is not active"
    )
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<HttpStatus> register(
            @RequestBody @Valid RegistrationRequest request) throws MessagingException {
        authenticationService.registerUser(request);
        return ResponseEntity.accepted().build();
    }

    @Operation(
            description = "Endpoint for user authentication",
            summary = "Authenticates user in to the application (user must be enabled)"
    )
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request){
        return ResponseEntity
                .ok(authenticationService.authenticate(request));
    }

    @Operation(
            description = "Endpoint for activating registered user",
            summary = "Activates user by activation token"
    )
    @GetMapping("/activate-account")
    public void activateAccount(@RequestParam String activationCode) throws MessagingException {
        authenticationService.activateAccount(activationCode);
    }
}