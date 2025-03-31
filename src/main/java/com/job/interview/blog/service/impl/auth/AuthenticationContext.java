package com.job.interview.blog.service.impl.auth;

import com.job.interview.blog.model.user.UserEntity;
import com.job.interview.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationContext {
    private final UserRepository userRepository;

    public UserEntity getAuthenticatedUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        var authUser = userRepository.findUserEntityByEmail(currentUserName)
                .orElseThrow(() -> new RuntimeException("User does not exists!"));
        if(authUser.isAccountLocked() || !authUser.isEnabled() ||
                !authUser.isAccountNonExpired() || !authUser.isCredentialsNonExpired()){
            throw new RuntimeException("Can not return invalid user!");
        }
        return authUser;
    }
}