package com.job.interview.blog.controller;


import com.job.interview.blog.model.dto.UserDto;
import com.job.interview.blog.service.impl.auth.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Log4j2
@Tag(name = "User details")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "user")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserDetailsServiceImpl userService;

    @Operation(
            description = "Endpoint for getting authenticated user details",
            summary = "Get User Details"
    )
    @PreAuthorize("hasAnyRole('READER','WRITER')")
    @GetMapping
    public ResponseEntity<?> getUserDetails() {
        return ResponseEntity.ok(userService.getCurrentUserDetails());
    }
    @Operation(
            description = "Endpoint for updating authenticated user details",
            summary = "Update User Details"
    )
    @PreAuthorize("hasAnyRole('READER','WRITER')")
    @PutMapping
    public ResponseEntity<?> updateUserInfo(@RequestBody UserDto userDto){
        return ResponseEntity.ok(userService.updateCurrentUserInfo(userDto));
    }
}