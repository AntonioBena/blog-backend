package com.job.interview.blog.service;

import com.job.interview.blog.model.dto.UserDto;

public interface UserInfoService {
    UserDto getUserDetails();
    UserDto updateUserInfo(UserDto userDto);
}