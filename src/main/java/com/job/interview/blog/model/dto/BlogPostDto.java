package com.job.interview.blog.model.dto;

import com.job.interview.blog.model.BlogCategory;
import com.job.interview.blog.model.BlogPostComment;
import com.job.interview.blog.model.user.UserEntity;
import lombok.*;

import java.util.Set;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString //TODO remove this
@AllArgsConstructor
@RequiredArgsConstructor
public class BlogPostDto {
    private Long id;
    private String title;
    private String shortContent;
    private String shortContentImageUrl;
    private BlogCategory category;
    private String htmlContentPath;
    private long likeCount;
    private long commentCount;
    private long viewCount;
    private Set<BlogPostComment> comments;
    private Set<String> likedBy;
    private UserEntity postOwner;
}