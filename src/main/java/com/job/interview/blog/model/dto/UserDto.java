package com.job.interview.blog.model.dto;

import com.job.interview.blog.model.user.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString //TODO change this
public class UserDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
}