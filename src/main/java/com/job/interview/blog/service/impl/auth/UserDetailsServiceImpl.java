package com.job.interview.blog.service.impl.auth;

import com.job.interview.blog.exception.auth.UnauthorizedException;
import com.job.interview.blog.exception.auth.UserNotFoundException;
import com.job.interview.blog.model.dto.UserDto;
import com.job.interview.blog.model.user.UserEntity;
import com.job.interview.blog.model.user.UserRole;
import com.job.interview.blog.repository.UserRepository;
import com.job.interview.blog.service.CurrentUserInfoService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService, CurrentUserInfoService {
    private final UserRepository userRepository;
    private final ModelMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException{
        UserEntity user = userRepository.findUserEntityByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User " + userEmail + " not found"));

        return new User(
                user.getUsername(),
                user.getPassword(),
                getAuthorities(user.getRole()));
    }

    private Collection<GrantedAuthority> getAuthorities(UserRole role) {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public UserDto getCurrentUserDetails() {
        return mapper.map(getAuthenticatedUserEntity(), UserDto.class);
    }

    @Override
    public UserEntity getAuthenticatedUserEntity() {
        var authUser = getCachedAuthenticatedUser();
        validateUserAccount(authUser);
        return authUser;
    }

    private UserEntity getCachedAuthenticatedUser() {
        return userRepository.findUserEntityByEmail(getAuthenticatedUsername())
                .orElseThrow(() -> new UserNotFoundException("User does not exist!"));
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("User is not authenticated!");
        }
        return authentication.getName();
    }

    private void validateUserAccount(UserEntity user) {
        if (user.isAccountLocked() || !user.isEnabled() ||
                !user.isAccountNonExpired() || !user.isCredentialsNonExpired()) {
            throw new UnauthorizedException("Cannot return invalid user!");
        }
    }

    @Override
    public UserDto updateCurrentUserInfo(UserDto userDto) {
        var userToUpdate = validateUserForUpdate(userDto);
        updateUserFields(userToUpdate, userDto);
        return mapper.map(userRepository.save(userToUpdate), UserDto.class);
    }

    private UserEntity validateUserForUpdate(UserDto userDto) {
        var foundUser = userRepository.findUserEntityByEmail(userDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        verifyUserPermission(foundUser, "Cannot update different user!");
        return foundUser;
    }

    private void updateUserFields(UserEntity user, UserDto userDto) {
        user.setRole(userDto.getRole());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    public void verifyUserPermission(UserEntity targetUser, String errorMessage) {
        if (!Objects.equals(targetUser.getId(), getCachedAuthenticatedUser().getId())) {
            throw new UnauthorizedException(errorMessage);
        }
    }
}