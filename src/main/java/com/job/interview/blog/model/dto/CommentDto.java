package com.job.interview.blog.model.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString //TODO remove this
@AllArgsConstructor
@RequiredArgsConstructor
public class CommentDto {
    private Long id;
    private String comment;
    private LocalDateTime createdAt;
    private UserDto user;
}