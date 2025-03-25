package com.job.interview.blog.service;


import com.job.interview.blog.model.dto.request.AuthenticationRequest;
import com.job.interview.blog.model.dto.request.RegistrationRequest;
import com.job.interview.blog.model.dto.response.AuthenticationResponse;

public interface AuthenticationService {
    void registerUser(RegistrationRequest request);
    AuthenticationResponse authenticate(AuthenticationRequest request); //returns jwt
    void activateAccount(String activationCode);
}