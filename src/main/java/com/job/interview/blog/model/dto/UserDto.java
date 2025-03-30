package com.job.interview.blog.model.dto;

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
}