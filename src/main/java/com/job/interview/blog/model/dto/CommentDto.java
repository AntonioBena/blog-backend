package com.job.interview.blog.model.dto;

import com.job.interview.blog.model.user.UserEntity;
import lombok.*;

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
    private UserEntity user;
}