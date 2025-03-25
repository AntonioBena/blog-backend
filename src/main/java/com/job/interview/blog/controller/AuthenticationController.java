package com.job.interview.blog.controller;

import com.job.interview.blog.model.dto.request.AuthenticationRequest;
import com.job.interview.blog.model.dto.request.RegistrationRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authorization")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "auth")
public class AuthenticationController { //every one are registered as reader, writer will be assign from ui

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register(@RequestBody @Valid RegistrationRequest request) {
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody @Valid AuthenticationRequest request) {
    }

    @GetMapping("/activate-account")
    public void activateAccount(@RequestParam String activationCode) {
    }
}