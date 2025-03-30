package com.job.interview.blog.service.impl;

import com.job.interview.blog.model.dto.UserDto;
import com.job.interview.blog.model.user.UserEntity;
import com.job.interview.blog.repository.UserRepository;
import com.job.interview.blog.service.UserInfoService;
import com.job.interview.blog.service.impl.auth.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserInfoService {

    private final AuthenticationContext authContext;
    private final ModelMapper mapper;
    private final UserRepository userRepository;

    @Override
    public UserDto getUserDetails(){
        var currentUser = authContext.getAuthenticatedUserEntity();
        return mapper.map(currentUser, UserDto.class);
    }

    @Override
    public UserDto updateUserInfo(UserDto userDto) {
        var foundUser = checkUserIntegrity(userDto);

        foundUser.setRole(userDto.getRole());
        foundUser.setFirstName(userDto.getFirstName());
        foundUser.setLastName(userDto.getLastName());;
        foundUser.setUpdatedAt(LocalDateTime.now());
        //TODO implement reset password request

        var updatedUser = userRepository.save(foundUser);
        return mapper.map(updatedUser, UserDto.class);
    }

    private UserEntity checkUserIntegrity(UserDto userDto){
        var authUser = authContext.getAuthenticatedUserEntity();

        var foundUser = userRepository.findUserEntityByEmail(userDto.getEmail())
                .orElseThrow(()-> new RuntimeException("User not found"));

        if(!Objects.equals(authUser.getId(), foundUser.getId()) ||
                !Objects.equals(authUser.getEmail(), foundUser.getEmail())){
            throw new RuntimeException("Can not update different user!");
        }
        return foundUser;
    }
}