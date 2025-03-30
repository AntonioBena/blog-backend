package com.job.interview.blog.controller;

import com.job.interview.blog.model.dto.UserDto;
import com.job.interview.blog.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@Tag(name = "User details")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "user")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping
    public ResponseEntity<?> uploadAndPublishBlogPost() {
        return ResponseEntity.ok(userService.getUserDetails());
    }
    @PutMapping
    public ResponseEntity<?> updateUserInfo(@RequestBody UserDto userDto){
        return ResponseEntity.ok(userService.updateUserInfo(userDto));
    }
}