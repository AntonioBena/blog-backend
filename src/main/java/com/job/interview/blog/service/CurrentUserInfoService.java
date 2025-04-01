package com.job.interview.blog.service;

import com.job.interview.blog.model.dto.UserDto;
import com.job.interview.blog.model.user.UserEntity;

public interface CurrentUserInfoService {
    UserDto getCurrentUserDetails();
    UserEntity getAuthenticatedUserEntity();
    UserDto updateCurrentUserInfo(UserDto userDto);
    void verifyUserPermission(UserEntity targetUser, String errorMessage);
}